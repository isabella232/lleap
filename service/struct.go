package service

import (
	"errors"
	"fmt"

	bolt "github.com/coreos/bbolt"
	"github.com/dedis/lleap/collection"
)

type collectionDB struct {
	*bolt.DB
	bucketName string
	coll       collection.Collection
}

// newCollectionDB initialises a structure and reads all key/value pairs to store
// it in the collection.
func newCollectionDB(db *bolt.DB, name string) *collectionDB {
	c := &collectionDB{
		DB:         db,
		bucketName: name,
		coll:       collection.New(collection.Data{}),
	}
	c.Update(func(tx *bolt.Tx) error {
		_, err := tx.CreateBucket([]byte(name))
		if err != nil {
			return fmt.Errorf("create bucket: %s", err)
		}
		return nil
	})
	c.loadAll()
	return c
}

func (c *collectionDB) loadAll() {
	c.View(func(tx *bolt.Tx) error {
		// Assume bucket exists and has keys
		b := tx.Bucket([]byte(c.bucketName))
		cur := b.Cursor()

		for k, v := cur.First(); k != nil; k, v = cur.Next() {
			c.coll.Add(k, v)
		}

		return nil
	})
}

func (c *collectionDB) store(key, value []byte) error {
	c.coll.Add(key, value)
	err := c.Update(func(tx *bolt.Tx) error {
		return tx.Bucket([]byte(c.bucketName)).Put(key, value)
	})
	return err
}

func (c *collectionDB) getValue(key []byte) (value []byte, err error) {
	proof, err := c.coll.Get(key).Record()
	if err != nil {
		return
	}
	// TODO: make it so that the collection only stores the hashes, not the values
	// err = c.Update(func(tx *bolt.Tx) error {
	// 	value = tx.Bucket([]byte(c.bucketName)).Get(key)
	// 	return nil
	// })
	// if err != nil{
	//   return
	// }
	hashes, err := proof.Values()
	if err != nil {
		return nil, err
	}
	if len(hashes) == 0 {
		return nil, errors.New("nothing stored under that key")
	}
	value, ok := hashes[0].([]byte)
	if !ok {
		return nil, errors.New("the value is not of type []byte")
	}
	return
}

func (c *collectionDB) storeTransaction() error {
	return nil
}
