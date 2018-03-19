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
	err = cdb.Store([]byte("first"), []byte("value"), []byte("mysig"), int64(1234))
	require.Nil(t, err)
	value, sig, ts, err := cdb.GetValue([]byte("first"))
	require.Nil(t, err)
	require.Equal(t, []byte("value"), value)
	require.Equal(t, []byte("mysig"), sig)
	require.Equal(t, int64(1234), ts)
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
	pairs := map[string]string{}
	mysig := []byte("mysignature")
	for i := 0; i < kvPairs; i++ {
		pairs[fmt.Sprintf("Key%d", i)] = fmt.Sprintf("value%d", i)
	}

	// Store all key/value pairs
	for k, v := range pairs {
		require.Nil(t, cdb.Store([]byte(k), []byte(v), mysig, int64(1234)))
	}

	// Verify it's all there
	for k, v := range pairs {
		stored, sig, ts, err := cdb.GetValue([]byte(k))
		require.Nil(t, err)
		require.Equal(t, v, string(stored))
		require.Equal(t, mysig, sig)
		require.Equal(t, ts, int64(1234))
	}

	// Get a new db handler
	cdb2 := newCollectionDB(db, []byte("coll1"))

	// Verify it's all there
	for k, v := range pairs {
		stored, sig, ts, err := cdb2.GetValue([]byte(k))
		require.Nil(t, err)
		require.Equal(t, v, string(stored))
		require.Equal(t, mysig, sig)
		require.Equal(t, ts, int64(1234))
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
		gvResp, err := service.GetKeyBlock(&lleap.GetKeyBlock{
			Version:     lleap.CurrentVersion,
			SkipchainID: genesis.Hash,
			Key:         []byte(key),
		})
		require.Nil(t, err)
		_, v, _, _, err := GetSBData(&gvResp.SkipBlock)
		require.Nil(t, err)
		require.Equal(t, 0, bytes.Compare(value, v))
	}

	// Now read the key/values from a new service
	// First create a roster to attach the data to it
	log.Lvl1("Recreate services and fetch keys again")
	service.tryLoad()

	// Retrieve the keypairs
	for key, value := range pairs {
		gvResp, err := service.GetKeyBlock(&lleap.GetKeyBlock{
			Version:     lleap.CurrentVersion,
			SkipchainID: genesis.Hash,
			Key:         []byte(key),
		})
		require.Nil(t, err)
		_, v, _, _, err := GetSBData(&gvResp.SkipBlock)
		require.Nil(t, err)
		require.Equal(t, 0, bytes.Compare(value, v))
	}
}
