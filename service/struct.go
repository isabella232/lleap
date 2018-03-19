package service

import (
	"bytes"
	"encoding/binary"
	"errors"
	"fmt"
	"strconv"
	"strings"

	bolt "github.com/coreos/bbolt"
	"github.com/dedis/cothority"
	"github.com/dedis/cothority/identity"
	"github.com/dedis/cothority/skipchain"
	"github.com/dedis/lleap/collection"
	"github.com/dedis/onet/network"
)

const keyMerkleRoot = "merkleroot"
const keyNewKey = "newkey"
const keyNewValue = "newvalue"
const keyTimestamp = "timestamp"

type collectionDB struct {
	db         *bolt.DB
	bucketName []byte
	coll       collection.Collection
}

// newCollectionDB initialises a structure and reads all key/value pairs to store
// it in the collection.
func newCollectionDB(db *bolt.DB, name []byte) *collectionDB {
	c := &collectionDB{
		db:         db,
		bucketName: name,
		coll:       collection.New(collection.Data{}, collection.Data{}, collection.Data{}),
	}
	c.db.Update(func(tx *bolt.Tx) error {
		_, err := tx.CreateBucket([]byte(name))
		if err != nil {
			return fmt.Errorf("create bucket: %s", err)
		}
		return nil
	})
	c.loadAll()
	// TODO: Check the merkle tree root.
	return c
}

func (c *collectionDB) loadAll() {
	c.db.View(func(tx *bolt.Tx) error {
		// Assume bucket exists and has keys
		b := tx.Bucket([]byte(c.bucketName))
		cur := b.Cursor()

		for k, v := cur.First(); k != nil; k, v = cur.Next() {
			if strings.HasSuffix(string(k), "sig") ||
				strings.HasSuffix(string(k), "ind") {
				continue
			}
			kSig := make([]byte, len(k)+3)
			copy(kSig, k)
			sig := b.Get(append(kSig, []byte("sig")...))
			kIndex := make([]byte, len(k)+3)
			copy(kIndex, k)
			indexBytes := b.Get(append(kIndex, []byte("ind")...))
			c.coll.Add(k, v, sig, indexBytes)
		}

		return nil
	})
}

func (c *collectionDB) Store(key, value, sig []byte, index int64) error {
	indexBuf := &bytes.Buffer{}
	binary.Write(indexBuf, binary.LittleEndian, index)
	indexBytes := indexBuf.Bytes()
	c.coll.Add(key, value, sig, indexBytes)
	err := c.db.Update(func(tx *bolt.Tx) error {
		bucket := tx.Bucket([]byte(c.bucketName))
		if err := bucket.Put(key, value); err != nil {
			return err
		}
		keysig := make([]byte, len(key)+3)
		copy(keysig, key)
		keysig = append(keysig, []byte("sig")...)
		if err := bucket.Put(keysig, sig); err != nil {
			return err
		}
		keyindex := make([]byte, len(key)+3)
		copy(keyindex, key)
		keyindex = append(keyindex, []byte("ind")...)
		if err := bucket.Put(keyindex, indexBytes); err != nil {
			return err
		}
		return nil
	})
	return err
}

func (c *collectionDB) GetValue(key []byte) (value, sig []byte, index int64, err error) {
	// TODO: make it so that the collection only stores the hashes, not the values
	proof, err := c.coll.Get(key).Record()
	if err != nil {
		return
	}
	hashes, err := proof.Values()
	if err != nil {
		return
	}
	if len(hashes) == 0 {
		err = errors.New("nothing stored under that key")
		return
	}
	value, ok := hashes[0].([]byte)
	if !ok {
		err = errors.New("the value is not of type []byte")
		return
	}
	sig, ok = hashes[1].([]byte)
	if !ok {
		err = errors.New("the signature is not of type []byte")
		return
	}
	indexBuf, ok := hashes[2].([]byte)
	if !ok {
		err = errors.New("the index is not of type []byte")
		return
	}
	err = binary.Read(bytes.NewReader(indexBuf), binary.LittleEndian, &index)
	return
}

// RootHash returns the hash of the root node in the merkle tree.
func (c *collectionDB) RootHash() []byte {
	return c.coll.GetRoot()
}

// GetSBData returns the cisc-interpreted data in the given skipblock.
func GetSBData(sb *skipchain.SkipBlock) (key, value, sig []byte, timestamp int64, err error) {
	_, ciscInt, err := network.Unmarshal(sb.Data, cothority.Suite)
	if err != nil {
		return
	}
	cisc, ok := ciscInt.(*identity.Data)
	if !ok {
		err = errors.New("didn't find data inside of skipblock")
	}
	s := cisc.Storage
	ts, err := strconv.Atoi(s[keyTimestamp])
	if err != nil {
		return
	}
	return []byte(s[keyNewKey]), []byte(s[keyNewValue]), nil, int64(ts), nil
}
