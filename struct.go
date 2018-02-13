package lleap

/*
This holds the messages used to communicate with the service over the network.
*/

import (
	"github.com/dedis/cothority/skipchain"
	"github.com/dedis/onet"
	"github.com/dedis/onet/network"
)

// We need to register all messages so the network knows how to handle them.
func init() {
	network.RegisterMessages(
		&CreateSkipchain{}, &CreateSkipchainResponse{},
		&SetKeyValue{}, &SetKeyValueResponse{},
		&GetValue{}, &GetValueResponse{},
	)
}

const (
	// ErrorParse indicates an error while parsing the protobuf-file.
	ErrorParse = iota + 4000
)

/*
// IdentityDB holds the database to the skipblocks.
// This is used for verification, so that all links can be followed.
// It is a wrapper to embed bolt.DB.
type IdentityDB struct {
	*bolt.DB
	bucketName string
}

// NewIdentityDB returns an initialized IdentityDB structure.
func NewIdentityDB(db *bolt.DB, bn string) *IdentityDB {
	return &IdentityDB{
		DB:         db,
		bucketName: bn,
	}
}

// GetByID returns a new copy of the skip-block or nil if it doesn't exist
func (db *IdentityDB) GetByID(id *identity.Identity) *identity.Identity {
	var result *identity.Identity
	err := db.View(func(tx *bolt.Tx) error {
		sb, err := db.getFromTx(tx, id)
		if err != nil {
			return err
		}
		result = sb
		return nil
	})

	if err != nil {
		log.Error(err)
	}
	return result
}

// Store stores the given SkipBlock in the service-list
func (db *IdentityDB) Store(id *identity.Identity) skipchain.SkipBlockID {
	var result skipchain.SkipBlockID
	err := db.Update(func(tx *bolt.Tx) error {
		sbOld, err := db.getFromTx(tx, id.Hash)
		if err != nil {
			return errors.New("failed to get skipblock with error: " + err.Error())
		}
		err = db.storeToTx(tx, id)
		if err != nil {
			return err
		}
		result = id.Hash
		return nil
	})

	if err != nil {
		log.Error(err.Error())
		return nil
	}

	return result
}

// getFromTx returns the skipblock identified by sbID.
// nil is returned if the key does not exist.
// An error is thrown if marshalling fails.
// The caller must ensure that this function is called from within a valid transaction.
func (db *IdentityDB) getFromTx(tx *bolt.Tx, sbID skipchain.SkipBlockID) (*identity.Identity, error) {
	val := tx.Bucket([]byte(db.bucketName)).Get(sbID)
	if val == nil {
		return nil, nil
	}

	_, sbMsg, err := network.Unmarshal(val, cothority.Suite)
	if err != nil {
		return nil, err
	}

	return sbMsg.(*identity.Identity), nil
}

// storeToTx stores the skipblock into the database.
// An error is returned on failure.
// The caller must ensure that this function is called from within a valid transaction.
func (db *IdentityDB) storeToTx(tx *bolt.Tx, id *identity.Identity) error {
	key := id.Hash
	val, err := network.Marshal(id)
	if err != nil {
		return err
	}
	return tx.Bucket([]byte(db.bucketName)).Put(key, val)
}
*/

// Version indicates what version this client runs. In the first development
// phase, each next version will break the preceeding versions. Later on,
// new versions might correctly interpret earlier versions.
type Version int

// CurrentVersion is what we're running now
const CurrentVersion Version = 1

// PROTOSTART
// import "skipblock.proto";
// import "roster.proto";
//
// option java_package = "ch.epfl.dedis.proto";
// option java_outer_classname = "SicpaProto";

// ***
// These are the messages used in the API-calls
// ***

// CreateSkipchain asks the cisc-service to set up a new skipchain.
type CreateSkipchain struct {
	// Version of the protocol
	Version Version
	// Roster defines which nodes participate in the skipchain.
	Roster onet.Roster
	// Writers represent keys that are allowed to add new key/value pairs to the skipchain.
	Writers *[][]byte
	// Signature, if available, will have to include the nonce sent by cisc.
	Signature *[]byte
}

// CreateSkipchainResponse holds the genesis-block of the new skipchain.
type CreateSkipchainResponse struct {
	// Version of the protocol
	Version Version
	// Skipblock of the created skipchain or empty if there was an error.
	Skipblock *skipchain.SkipBlock
}

// SetKeyValue asks for inclusion for a new key/value pair. The value needs
// to be signed by one of the Writers from the createSkipchain call.
type SetKeyValue struct {
	// Version of the protocol
	Version Version
	// SkipchainID is the hash of the first skipblock
	SkipchainID skipchain.SkipBlockID
	// Key where the value is stored
	Key []byte
	// Value, if Writers were present in CreateSkipchain, the value should be
	// signed by one of the keys.
	Value []byte
	// Signature is an RSA-sha384 signature on the key/value pair concatenated
	Signature []byte
}

// SetKeyValueResponse gives the timestamp and the skipblock-id
type SetKeyValueResponse struct {
	// Version of the protocol
	Version Version
	// Timestamp is milliseconds since the unix epoch (1/1/1970, 12am UTC)
	Timestamp *int64
	// Skipblock ID is the hash of the block where the value is stored
	SkipblockID *skipchain.SkipBlockID
}

// GetValue looks up the value in the given skipchain and returns the
// stored value, or an error if either the skipchain or the key doesn't exist.
type GetValue struct {
	// Version of the protocol
	Version Version
	// SkipchainID represents the skipchain where the value is stored
	SkipchainID skipchain.SkipBlockID
	// Key to retrieve
	Key []byte
}

// GetValueResponse returns the value or an error if the key hasn't been found.
type GetValueResponse struct {
	//Version of the protocol
	Version Version
	// Value of the key
	Value *[]byte
	// Signature as sent when the value was stored
	Signature *[]byte
	// Proof the value is correct
	Proof *[]byte
}
