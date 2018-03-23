package service

import (
	"fmt"
	"testing"

	"github.com/dedis/cothority"
	"github.com/dedis/cothority/skipchain"
	"github.com/dedis/kyber"
	"github.com/dedis/kyber/sign/cosi"
	"github.com/dedis/kyber/suites"
	"github.com/dedis/lleap"
	"github.com/dedis/onet"
	"github.com/dedis/onet/log"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

var tSuite = suites.MustFind("Ed25519")

func TestMain(m *testing.M) {
	log.MainTest(m)
}

func TestService_CreateSkipchain(t *testing.T) {
	s := newSer(t, 0)
	defer s.local.CloseAll()
	resp, err := s.service.CreateSkipchain(&lleap.CreateSkipchain{
		Version: 0,
		Roster:  *s.roster,
	})
	require.NotNil(t, err)

	resp, err = s.service.CreateSkipchain(&lleap.CreateSkipchain{
		Version: lleap.CurrentVersion,
		Roster:  *s.roster,
	})
	require.Nil(t, err)
	assert.Equal(t, lleap.CurrentVersion, resp.Version)
	assert.NotNil(t, resp.Skipblock)
}

func TestService_AddKeyValue(t *testing.T) {
	s := newSer(t, 1)
	defer s.local.CloseAll()

	akvresp, err := s.service.SetKeyValue(&lleap.SetKeyValue{
		Version: 0,
	})
	require.NotNil(t, err)
	akvresp, err = s.service.SetKeyValue(&lleap.SetKeyValue{
		Version:     lleap.CurrentVersion,
		SkipchainID: s.sb.SkipChainID(),
		Key:         s.key,
		Value:       s.value,
	})
	require.Nil(t, err)
	require.NotNil(t, akvresp)
	require.Equal(t, lleap.CurrentVersion, akvresp.Version)
	require.NotNil(t, akvresp.Timestamp)
	require.NotNil(t, akvresp.SkipblockID)
}

func TestService_GetValue(t *testing.T) {
	s := newSer(t, 2)
	defer s.local.CloseAll()

	rep, err := s.service.GetValue(&lleap.GetValue{
		Version:     lleap.CurrentVersion,
		SkipchainID: s.sb.SkipChainID(),
		Key:         s.key,
	})
	require.Nil(t, err)

	checkValueBlock(t, rep, s.value, s.roster.Publics())
}

func TestService_Store(t *testing.T) {
	kvPairs := 2
	pairs := map[string][]byte{}

	// First create a roster to attach the data to it
	local := onet.NewLocalTest(cothority.Suite)
	defer local.CloseAll()
	var genService onet.Service
	_, roster, genService := local.MakeSRS(cothority.Suite, 4, lleapID)
	service := genService.(*Service)

	// Create a new skipchain
	resp, err := service.CreateSkipchain(&lleap.CreateSkipchain{
		Version: lleap.CurrentVersion,
		Roster:  *roster,
	})
	require.Nil(t, err)
	genesis := resp.Skipblock

	// Store some keypairs
	for i := 0; i < kvPairs; i++ {
		key := []byte(fmt.Sprintf("Key%d", i))
		value := []byte(fmt.Sprintf("value%d", i))
		pairs[string(key)] = value
		_, err := service.SetKeyValue(&lleap.SetKeyValue{
			Version:     lleap.CurrentVersion,
			SkipchainID: genesis.Hash,
			Key:         key,
			Value:       value,
		})
		require.Nil(t, err)
	}

	// Retrieve the keypairs
	for key, value := range pairs {
		gvResp, err := service.GetValue(&lleap.GetValue{
			Version:     lleap.CurrentVersion,
			SkipchainID: genesis.Hash,
			Key:         []byte(key),
		})
		require.Nil(t, err)
		checkValueBlock(t, gvResp, value, roster.Publics())
	}

	// Now read the key/values from a new service
	// First create a roster to attach the data to it
	log.Lvl1("Recreate services and fetch keys again")
	service.tryLoad()

	// Retrieve the keypairs
	for key, value := range pairs {
		gvResp, err := service.GetValue(&lleap.GetValue{
			Version:     lleap.CurrentVersion,
			SkipchainID: genesis.Hash,
			Key:         []byte(key),
		})
		require.Nil(t, err)
		checkValueBlock(t, gvResp, value, roster.Publics())
	}
}

func checkValueBlock(t *testing.T, rep *lleap.GetValueResponse, value []byte, publics []kyber.Point) {
	d, err := getDataFromBlock(&rep.SkipBlock)
	require.Equal(t, value, []byte(d.Storage[keyNewValue]))

	// check the link
	require.Equal(t, rep.ForwardLink.To, rep.SkipBlock.CalculateHash())

	// check msg in the signature
	require.Equal(t, []byte(rep.ForwardLink.Hash()), rep.ForwardLink.Signature.Msg)

	// check the signature
	err = cosi.Verify(cothority.Suite, publics, rep.ForwardLink.Signature.Msg, rep.ForwardLink.Signature.Sig, nil)
	require.Nil(t, err)
}

type ser struct {
	local   *onet.LocalTest
	hosts   []*onet.Server
	roster  *onet.Roster
	service *Service
	sb      *skipchain.SkipBlock
	key     []byte
	value   []byte
}

func newSer(t *testing.T, step int) *ser {
	s := &ser{
		local: onet.NewTCPTest(tSuite),
		key:   []byte("anykey"),
		value: []byte("anyvalue"),
	}
	s.hosts, s.roster, _ = s.local.GenTree(5, true)
	s.service = s.local.GetServices(s.hosts, lleapID)[0].(*Service)

	for i := 0; i < step; i++ {
		switch i {
		case 0:
			resp, err := s.service.CreateSkipchain(&lleap.CreateSkipchain{
				Version: lleap.CurrentVersion,
				Roster:  *s.roster,
			})
			assert.Nil(t, err)
			s.sb = resp.Skipblock
		case 1:
			_, err := s.service.SetKeyValue(&lleap.SetKeyValue{
				Version:     lleap.CurrentVersion,
				SkipchainID: s.sb.SkipChainID(),
				Key:         s.key,
				Value:       s.value,
			})
			assert.Nil(t, err)
		}
	}
	return s
}
