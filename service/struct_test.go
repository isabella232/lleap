package service

import (
	"bytes"
	"fmt"
	"io/ioutil"
	"os"
	"testing"

	bolt "github.com/coreos/bbolt"
	"github.com/dedis/cothority"
	"github.com/dedis/lleap"
	"github.com/dedis/onet"
	"github.com/dedis/onet/log"
	"github.com/stretchr/testify/require"
)

func TestCollectionDBStrange(t *testing.T) {
	tmpDB, err := ioutil.TempFile("", "tmpDB")
	require.Nil(t, err)
	tmpDB.Close()
	defer os.Remove(tmpDB.Name())

	db, err := bolt.Open(tmpDB.Name(), 0600, nil)
	require.Nil(t, err)

	cdb := newCollectionDB(db, []byte("coll1"))
	err = cdb.Store([]byte("first"), 10, []byte("value"), []byte("mysig"))
	require.Nil(t, err)
	idx, value, sig, err := cdb.GetValue([]byte("first"))
	require.Nil(t, err)
	require.Equal(t, uint64(10), idx)
	require.Equal(t, []byte("value"), value)
	require.Equal(t, []byte("mysig"), sig)
}

func TestCollectionDB(t *testing.T) {
	kvPairs := 16

	tmpDB, err := ioutil.TempFile("", "tmpDB")
	require.Nil(t, err)
	tmpDB.Close()
	defer os.Remove(tmpDB.Name())

	db, err := bolt.Open(tmpDB.Name(), 0600, nil)
	require.Nil(t, err)

	cdb := newCollectionDB(db, []byte("coll1"))
	pairs := map[string]struct {
		v   string
		idx uint64
	}{}
	mysig := []byte("mysignature")
	for i := 0; i < kvPairs; i++ {
		pairs[fmt.Sprintf("Key%d", i)] = struct {
			v   string
			idx uint64
		}{
			v:   fmt.Sprintf("value%d", i),
			idx: uint64(i),
		}
	}

	// Store all key/value pairs
	for k, v := range pairs {
		require.Nil(t, cdb.Store([]byte(k), v.idx, []byte(v.v), mysig))
	}

	// Verify it's all there
	for k, v := range pairs {
		idx, stored, sig, err := cdb.GetValue([]byte(k))
		require.Nil(t, err)
		require.Equal(t, v.idx, idx)
		require.Equal(t, v.v, string(stored))
		require.Equal(t, mysig, sig)
		idx++
	}

	// Get a new db handler
	cdb2 := newCollectionDB(db, []byte("coll1"))

	// Verify it's all there
	for k, v := range pairs {
		_, stored, _, err := cdb2.GetValue([]byte(k))
		require.Nil(t, err)
		require.Equal(t, v.v, string(stored))
	}
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
		require.Equal(t, 0, bytes.Compare(value, *gvResp.Value))
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
		require.Equal(t, 0, bytes.Compare(value, *gvResp.Value))
	}
}
