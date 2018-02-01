package service

/*
The service.go defines what to do for each API-call. This part of the service
runs on the node.
*/

import (
	"errors"
	"sync"

	"github.com/dedis/cothority"
	"github.com/dedis/cothority/identity"
	"github.com/dedis/kyber"
	"github.com/dedis/kyber/sign/schnorr"
	"github.com/dedis/kyber/util/key"
	"github.com/dedis/onet"
	"github.com/dedis/onet/log"
	"github.com/dedis/onet/network"
	"github.com/dedis/sicpa"
)

// Used for tests
var sicpaID onet.ServiceID

func init() {
	var err error
	sicpaID, err = onet.RegisterNewService(sicpa.ServiceName, newService)
	log.ErrFatal(err)
	network.RegisterMessage(&storage{})
}

// Service is our sicpa-service
type Service struct {
	// We need to embed the ServiceProcessor, so that incoming messages
	// are correctly handled.
	*onet.ServiceProcessor

	storage *storage
}

// storageID reflects the data we're storing - we could store more
// than one structure.
const storageID = "main"

// storage is used to save our data.
type storage struct {
	Identities map[string]*identity.IDBlock
	Private    map[string]kyber.Scalar
	sync.Mutex
}

// CreateSkipchain asks the cisc-service to create a new skipchain ready to store
// key/value pairs.
func (s *Service) CreateSkipchain(req *sicpa.CreateSkipchain) (*sicpa.CreateSkipchainResponse, error) {
	if req.Version != sicpa.CurrentVersion {
		return nil, errors.New("version mismatch")
	}

	kp := key.NewKeyPair(cothority.Suite)
	data := &identity.Data{
		Threshold: 2,
		Device:    map[string]*identity.Device{"service": &identity.Device{Point: kp.Public}},
		Roster:    req.Roster,
	}

	cir, err := s.idService().CreateIdentityInternal(&identity.CreateIdentity{
		Data: data,
	}, "", "")
	if err != nil {
		return nil, err
	}
	gid := string(cir.Genesis.SkipChainID())
	s.storage.Identities[gid] = &identity.IDBlock{
		Latest:          data,
		LatestSkipblock: cir.Genesis,
	}
	s.storage.Private[gid] = kp.Private
	s.save()
	return &sicpa.CreateSkipchainResponse{
		Version:   sicpa.CurrentVersion,
		Skipblock: cir.Genesis,
	}, nil
}

// AddKeyValue asks cisc to add a new key/value pair.
func (s *Service) AddKeyValue(req *sicpa.AddKeyValue) (*sicpa.AddKeyValueResponse, error) {
	if req.Version != sicpa.CurrentVersion {
		return nil, errors.New("version mismatch")
	}
	gid := string(req.SkipchainID)
	idb := s.storage.Identities[gid]
	priv := s.storage.Private[gid]
	if idb == nil || priv == nil {
		return nil, errors.New("don't have this identity stored")
	}

	prop := idb.Latest.Copy()
	prop.Storage[string(req.Key)] = string(req.Value)
	_, err := s.idService().ProposeSend(&identity.ProposeSend{
		ID:      identity.ID(req.SkipchainID),
		Propose: prop,
	})
	if err != nil {
		return nil, err
	}

	hash, err := prop.Hash(cothority.Suite)
	if err != nil {
		return nil, err
	}
	sig, err := schnorr.Sign(cothority.Suite, priv, hash)
	if err != nil {
		return nil, err
	}
	resp, err := s.idService().ProposeVote(&identity.ProposeVote{
		ID:        identity.ID(req.SkipchainID),
		Signer:    "service",
		Signature: sig,
	})
	if err != nil {
		return nil, err
	}
	timestamp := int64(resp.Data.Index)
	return &sicpa.AddKeyValueResponse{
		Version:     sicpa.CurrentVersion,
		Timestamp:   &timestamp,
		SkipblockID: &resp.Data.Hash,
	}, nil
}

// GetValue looks up the key in the given skipchain and returns the corresponding value.
func (s *Service) GetValue(req *sicpa.GetValue) (*sicpa.GetValueResponse, error) {
	if req.Version != sicpa.CurrentVersion {
		return nil, errors.New("version mismatch")
	}

	dur, err := s.idService().DataUpdate(&identity.DataUpdate{
		ID: identity.ID(req.SkipchainID),
	})
	if err != nil {
		return nil, err
	}
	value, exists := dur.Data.Storage[string(req.Key)]
	if !exists {
		return nil, errors.New("this value doesn't exist")
	}
	valueB := []byte(value)
	return &sicpa.GetValueResponse{
		Version: sicpa.CurrentVersion,
		Value:   &valueB,
	}, nil
}

func (s *Service) idService() *identity.Service {
	return s.Service(identity.ServiceName).(*identity.Service)
}

// saves all skipblocks.
func (s *Service) save() {
	s.storage.Lock()
	defer s.storage.Unlock()
	err := s.Save(storageID, s.storage)
	if err != nil {
		log.Error("Couldn't save file:", err)
	}
}

// Tries to load the configuration and updates the data in the service
// if it finds a valid config-file.
func (s *Service) tryLoad() error {
	s.storage = &storage{}
	msg, err := s.Load(storageID)
	if err != nil {
		return err
	}
	if msg != nil {
		var ok bool
		s.storage, ok = msg.(*storage)
		if !ok {
			return errors.New("Data of wrong type")
		}
	}
	if s.storage == nil {
		s.storage = &storage{}
	}
	if s.storage.Identities == nil {
		s.storage.Identities = map[string]*identity.IDBlock{}
	}
	if s.storage.Private == nil {
		s.storage.Private = map[string]kyber.Scalar{}
	}
	return nil
}

// newService receives the context that holds information about the node it's
// running on. Saving and loading can be done using the context. The data will
// be stored in memory for tests and simulations, and on disk for real deployments.
func newService(c *onet.Context) (onet.Service, error) {
	s := &Service{
		ServiceProcessor: onet.NewServiceProcessor(c),
	}
	if err := s.RegisterHandlers(s.CreateSkipchain); err != nil {
		log.ErrFatal(err, "Couldn't register messages")
	}
	if err := s.tryLoad(); err != nil {
		log.Error(err)
		return nil, err
	}
	return s, nil
}
