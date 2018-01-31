package service

/*
The service.go defines what to do for each API-call. This part of the service
runs on the node.
*/

import (
	"errors"
	"sync"

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
	Count int
	sync.Mutex
}

// CreateSkipchain asks the cisc-service to create a new skipchain ready to store
// key/value pairs.
func (s *Service) CreateSkipchain(req *sicpa.CreateSkipchain) (*sicpa.CreateSkipchainResponse, error) {
	return nil, nil
}

// AddKeyValue asks cisc to add a new key/value pair.
func (s *Service) AddKeyValue(req *sicpa.AddKeyValue) (*sicpa.AddKeyValueResponse, error) {
	return nil, nil
}

// GetValue looks up the key in the given skipchain and returns the corresponding value.
func (s *Service) GetValue(req *sicpa.GetValue) (*sicpa.GetValueResponse, error) {
	return nil, nil
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
	if msg == nil {
		return nil
	}
	var ok bool
	s.storage, ok = msg.(*storage)
	if !ok {
		return errors.New("Data of wrong type")
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
