package sicpa

import (
	collections "github.com/dedis/student_17_collections"
)

/*
 * PSEUDO-CODE
 */

var role string
var v interface{}
var c interface{}

func ServiceStart() {
	if role == "verifier" {
		v = collections.NewVerifierWithRoot(root, collections.Data{}) //TODO
	} else if role == "collectionManager" {
		// load collection from drive
		var proofs = make([]collections.Proof, 0)
		var data []byte
		proofs[0], _ = collections.Deserialize(data)

		// restore collection from all proofs
		c = collections.EmptyCollection(collections.Data{})
		for _, p := range proofs {
			c.Verify(p)
		}
	}
}

func ServiceShutDown() {
	var role string
	if role == "collectionManager" {
		// serialize all the proofs
		proofs := c.GetAllProofs() //TODO

		for _, p := range proofs {
			b := c.Serialize(p)
			_ = b
			// write b to disk
		}
	}
}

func ServiceAcceptUpdate(newData []byte) {

	var timestamp []byte
	c.Add(timestamp, newData)
	proof, _ := c.Get(timestamp).Proof()

	newSkipBlock(proof) // broadcast this to all verifiers
}

// call on reception of new skipblock
func ServiceVerifySkipBlock(skipblock interface{}) {

	proof := skipblock.proof

	validBlock := false
	if v.Verify(proof) {
		validBlock = true
	}
	return validBlock
}
