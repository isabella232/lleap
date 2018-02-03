/*
* This is a template for creating an app. It only has one command which
* prints out the name of the app.
 */
package main

import (
	"encoding/hex"
	"errors"
	"os"

	"github.com/dedis/onet/app"
	"github.com/dedis/sicpa"

	"github.com/dedis/onet/log"
	"gopkg.in/urfave/cli.v1"
)

func main() {
	cliApp := cli.NewApp()
	cliApp.Name = "Sicpa kv"
	cliApp.Usage = "Used for building other apps."
	cliApp.Version = "0.1"
	groupsDef := "the group-definition-file"
	cliApp.Commands = []cli.Command{
		{
			Name:      "create",
			Usage:     "creates a new skipchain",
			Aliases:   []string{"c"},
			ArgsUsage: groupsDef,
			Action:    create,
		},
		{
			Name:    "set",
			Usage:   "sets a key/value pair",
			Aliases: []string{"s"},
			Action:  set,
		},
		{
			Name:    "get",
			Usage:   "gets a value",
			Aliases: []string{"g"},
			Action:  get,
		},
	}
	cliApp.Flags = []cli.Flag{
		cli.IntFlag{
			Name:  "debug, d",
			Value: 0,
			Usage: "debug-level: 1 for terse, 5 for maximal",
		},
	}
	cliApp.Before = func(c *cli.Context) error {
		log.SetDebugVisible(c.Int("debug"))
		return nil
	}
	log.ErrFatal(cliApp.Run(os.Args))
}

// Creates a new skipchain
func create(c *cli.Context) error {
	log.Info("Create a new skipchain")

	if c.NArg() != 1 {
		return errors.New("please give: group.toml")
	}
	group := readGroup(c)
	client := sicpa.NewClient()
	resp, err := client.CreateSkipchain(group.Roster, nil)
	if err != nil {
		return errors.New("during creation of skipchain: " + err.Error())
	}
	log.Infof("Created new skipchain on roster %s with ID: %x", group.Roster.List, resp.Skipblock.Hash)
	return nil
}

// Returns the number of calls.
func set(c *cli.Context) error {
	log.Info("Set key/value pair")

	if c.NArg() != 4 {
		return errors.New("please give: group.toml skipchain-ID key value")
	}
	group := readGroup(c)
	scid, err := hex.DecodeString(c.Args().Get(1))
	if err != nil {
		return err
	}
	key := c.Args().Get(2)
	value := c.Args().Get(3)
	resp, err := sicpa.NewClient().SetKeyValue(group.Roster, scid, []byte(key), []byte(value))
	if err != nil {
		return errors.New("couldn't set new key/value pair: " + err.Error())
	}
	log.Infof("Successfully set new key/value pair in block: %x", resp.SkipblockID)
	return nil
}

// Returns the value of the key
func get(c *cli.Context) error {
	log.Info("Get value")

	if c.NArg() != 3 {
		return errors.New("please give: group.toml skipchain-ID key")
	}
	group := readGroup(c)
	scid, err := hex.DecodeString(c.Args().Get(1))
	if err != nil {
		return err
	}
	key := c.Args().Get(2)
	resp, err := sicpa.NewClient().GetValue(group.Roster, scid, []byte(key))
	if err != nil {
		return errors.New("couldn't get value: " + err.Error())
	}
	log.Infof("Read value: %x = %x", key, *resp.Value)
	return nil
}

func readGroup(c *cli.Context) *app.Group {
	name := c.Args().First()
	f, err := os.Open(name)
	log.ErrFatal(err, "Couldn't open group definition file")
	group, err := app.ReadGroupDescToml(f)
	log.ErrFatal(err, "Error while reading group definition file", err)
	if len(group.Roster.List) == 0 {
		log.ErrFatalf(err, "Empty entity or invalid group defintion in: %s",
			name)
	}
	return group
}
