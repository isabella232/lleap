package main

import (
	collections "github.com/dedis/student_17_collections"
	"fmt"
)

func main() {

	c := collections.EmptyCollection(collections.Data{})

	/*
	 * CRUD
	 */

	// Creates a record
	c.Add([]byte("record"), []byte("data"))

	// Remove throws an error on non-existing keys
	err := c.Remove([]byte("record-nonexisting"))
	if err != nil {
		fmt.Println("Expected error (key not found):", err)
	}

	// Creates a record with "data2", then changes to "data3"
	c.Set([]byte("record2"), []byte("data2"))
	c.SetField([]byte("record2"), 0, []byte("data3"))

	// You cannot overwrite records
	err = c.Add([]byte("record2"), []byte("data4"))
	if err != nil {
		fmt.Println("Expected error (collision):", err)
	}

	fmt.Println("-------------")
	fmt.Println("Now fetching some existing data :")
	fmt.Println("")

	// Fetching existing data

	record, record_fetching_error := c.Get([]byte("record")).Record()
	recordFound := record.Match()
	data, recordNotFoundError := record.Values()

	fmt.Println("Record fetching error (doesn't indicate the record exists or not): ", record_fetching_error)
	fmt.Println("Record found:", recordFound) // can also test record == nil
	fmt.Println("Data retrieved:", string(data[0].([]byte)))
	fmt.Println("Error while fetching the record:", recordNotFoundError)

	// Fetching non-existing data

	fmt.Println("-------------")
	fmt.Println("Now fetching some non-existing data :")
	fmt.Println("")

	record, record_fetching_error = c.Get([]byte("nonexisting-record")).Record()
	recordFound = record.Match()
	data, recordNotFoundError = record.Values()

	fmt.Println("Record fetching error (doesn't indicate the record exists or not): ", record_fetching_error)
	fmt.Println("Record found:", recordFound) // can also test record == nil
	fmt.Println("Error while fetching the record:", recordNotFoundError)

	// the "record fetching error" happens only when the collection is able to tell whether the record exists or not;
	// this only happens when the collection is a verifier

	/*
	 * Verification
	 */

	fmt.Println("-------------")

	// Verifier needs to have the same type (collections.Data{}) as the collection
	v := collections.EmptyVerifier(collections.Data{})

	// a verifier (who does not already have "record") does not accept updates that aren't part of a Proof
	err = v.Add([]byte("record"), []byte("somedata"))
	if err != nil {
		fmt.Println("Expected error (unknown subtree):", err)
	}

	fmt.Println("-------------")
	fmt.Println("Now fetching some data in the verifier (who does not have any data):")

	record, record_fetching_error = v.Get([]byte("nonexisting-record")).Record()
	fmt.Println("Record fetching error (doesn't indicate the record exists or not): ", record_fetching_error)

	// you see the difference with c/v, now record_fetching_error is set since v has no idea whether "nonexisting-record"
	// exists or not.

	// let's transfer some data to the verifier

	proof, err := c.Get([]byte("record")).Proof() // "record" doesn't have to exist, you can prove absence

	// The proof can be send over the network:
	// buffer := c.Serialize(proof) // A []byte that contains a representation of proof.
	// proofagain, deserialize_err := c.Deserialize(buffer)

	v.Begin()

	if v.Verify(proof) {
		fmt.Println("Verifier accepted the proof about \"record\".")
	}

	proof, err = v.Get([]byte("record")).Proof()
	fmt.Println(proof)
	fmt.Println()
	fmt.Println(err) // err is non-nil !!

	fmt.Println("-------------")
	fmt.Println("Now fetching some data in the verifier (who has been updated with a proof):")

	err = v.Add([]byte("record"), []byte("data"))
	if err != nil {
		fmt.Println("Expected error (unknown subtree):", err) // err is non-nil !!
	}

	v.End()
}