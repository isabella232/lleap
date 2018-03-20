package service

import (
	"encoding/binary"
	"errors"
	"fmt"

	bolt "github.com/coreos/bbolt"
	"github.com/dedis/student_18_omniledger/lleap/collection"
)

type collectionDB struct {
	db            *bolt.DB
	kvBucketName  []byte
	idxBucketName []byte
	sigBucketName []byte
	coll          collection.Collection
}

// newCollectionDB initialises a structure and reads all key/value pairs to store
// it in the collection.
func newCollectionDB(db *bolt.DB, name []byte) *collectionDB {
	c := &collectionDB{
		db:            db,
		kvBucketName:  name,
		idxBucketName: append(name, []byte("_idx")...),
		sigBucketName: append(name, []byte("_sig")...),
		coll:          collection.New(collection.Data{}, collection.Data{}, collection.Data{}),
	}
	c.db.Update(func(tx *bolt.Tx) error {
		if _, err := tx.CreateBucket(c.idxBucketName); err != nil {
			return fmt.Errorf("create bucket %s: %s", c.idxBucketName, err)
		}
		if _, err := tx.CreateBucket(c.kvBucketName); err != nil {
			return fmt.Errorf("create bucket %s: %s", c.kvBucketName, err)
		}
		if _, err := tx.CreateBucket(c.sigBucketName); err != nil {
			return fmt.Errorf("create bucket %s: %s", c.sigBucketName, err)
		}
		return nil
	})
	c.loadAll()
	// TODO: Check the merkle tree root.
	return c
}

func (c *collectionDB) loadAll() error {
	return c.db.View(func(tx *bolt.Tx) error {
		// Assume bucket exists and has keys
		b := tx.Bucket(c.kvBucketName)
		if b == nil {
			return fmt.Errorf("bucket %s does not exist", string(c.kvBucketName))
		}
		bIdx := tx.Bucket(c.idxBucketName)
		if bIdx == nil {
			return fmt.Errorf("bucket %s does not exist", string(c.idxBucketName))
		}
		bSig := tx.Bucket(c.sigBucketName)
		if bSig == nil {
			return fmt.Errorf("bucket %s does not exist", string(c.sigBucketName))
		}

		cur := b.Cursor()

		for k, v := cur.First(); k != nil; k, v = cur.Next() {
			idx := bIdx.Get(k)
			if idx == nil {
				return fmt.Errorf("index for key %x cannot be nil", k)
			}
			sig := bSig.Get(k)
			if sig == nil {
				return fmt.Errorf("signature for key %x cannot be nil", k)
			}
			if err := c.coll.Add(k, idx, v, sig); err != nil {
				return err
			}
		}
		return nil
	})
}

func (c *collectionDB) Store(key []byte, idx uint64, value, sig []byte) error {
	return c.db.Update(func(tx *bolt.Tx) error {
		rawU64 := make([]byte, 8)
		binary.LittleEndian.PutUint64(rawU64, idx)

		if err := tx.Bucket(c.idxBucketName).Put(key, rawU64); err != nil {
			return err
		}

		if err := tx.Bucket(c.kvBucketName).Put(key, value); err != nil {
			return err
		}

		if err := tx.Bucket(c.sigBucketName).Put(key, sig); err != nil {
			return err
		}

		// we do the final add to the collection inside the transaction
		// so if it fails the previously performed transactions will
		// roll-back
		if err := c.coll.Add(key, rawU64, value, sig); err != nil {
			return err
		}
		return nil
	})
}

func (c *collectionDB) GetValue(key []byte) (idx uint64, value, sig []byte, err error) {
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
	if len(hashes) != 3 {
		err = fmt.Errorf("incorrect number of values, got %v but need 3", len(hashes))
	}

	idxRaw, ok := hashes[0].([]byte)
	if !ok {
		err = errors.New("the value is not of type []byte")
		return
	}
	idx = binary.LittleEndian.Uint64(idxRaw)

	value, ok = hashes[1].([]byte)
	if !ok {
		err = errors.New("the value is not of type []byte")
		return
	}
	sig, ok = hashes[2].([]byte)
	if !ok {
		err = errors.New("the signature is not of type []byte")
		return
	}
	return
}

// RootHash returns the hash of the root node in the merkle tree.
func (c *collectionDB) RootHash() []byte {
	return c.coll.GetRoot()
}
