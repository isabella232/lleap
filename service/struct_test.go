package service

import (
	"fmt"
	"io/ioutil"
	"os"
	"testing"

	bolt "github.com/coreos/bbolt"
	"github.com/stretchr/testify/require"
)

func TestCollectionDB(t *testing.T) {
	kvPairs := 16

	tmpDB, err := ioutil.TempFile("", "tmpDB")
	require.Nil(t, err)
	tmpDB.Close()
	defer os.Remove(tmpDB.Name())

	db, err := bolt.Open(tmpDB.Name(), 0600, nil)
	require.Nil(t, err)

	cdb := newCollectionDB(db, "coll1")
	pairs := map[string]string{}
	for i := 0; i < kvPairs; i++ {
		pairs[fmt.Sprintf("Key%d", i)] = fmt.Sprintf("Value%d", i)
	}

	// Store all key/value pairs
	for k, v := range pairs {
		require.Nil(t, cdb.store([]byte(k), []byte(v)))
	}

	// Verify it's all there
	for k, v := range pairs {
		stored, err := cdb.getValue([]byte(k))
		require.Nil(t, err)
		require.Equal(t, v, string(stored))
	}

	// Get a new db handler
	cdb2 := newCollectionDB(db, "coll1")

	// Verify it's all there
	for k, v := range pairs {
		stored, err := cdb2.getValue([]byte(k))
		require.Nil(t, err)
		require.Equal(t, v, string(stored))
	}
}
