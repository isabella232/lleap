package service

import (
	"errors"
	"fmt"
	"io/ioutil"
	"os"
	"testing"

	bolt "github.com/coreos/bbolt"
	"github.com/dedis/cothority"
	"github.com/dedis/cothority/identity"
	"github.com/dedis/cothority/skipchain"
	"github.com/dedis/onet/network"
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

func getDataFromBlock(sb *skipchain.SkipBlock) (*identity.Data, error) {
	_, msg, err := network.Unmarshal(sb.Data, cothority.Suite)
	if err != nil {
		return nil, err
	}

	d, ok := msg.(*identity.Data)
	if !ok {
		return nil, errors.New("failed to cast to *identity.Data")
	}
	return d, nil
}
