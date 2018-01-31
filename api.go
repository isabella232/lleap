package sicpa

/*
* The Sicpa service uses a CISC (https://github.com/dedis/cothority/cisc) to store
* key/value pairs on a skipchain.
 */

import (
	"github.com/dedis/cothority"
	"github.com/dedis/kyber"
	"github.com/dedis/onet"
)

// ServiceName is used for registration on the onet.
const ServiceName = "Sicpa"

// Client is a structure to communicate with the CoSi
// service
type Client struct {
	*onet.Client
}

// NewClient instantiates a new cosi.Client
func NewClient() *Client {
	return &Client{Client: onet.NewClient(ServiceName, cothority.Suite)}
}

// CreateSkipchain sets up a new skipchain to hold the key/value pairs. If
// a key is given, it is used to authenticate towards the cothority.
func (c *Client) CreateSkipchain(r *onet.Roster, key kyber.Scalar) (*CreateSkipchainResponse, error) {
	reply := &CreateSkipchainResponse{}
	err := c.SendProtobuf(r.List[0], &CreateSkipchain{Roster: r}, reply)
	if err != nil {
		return nil, err
	}
	return reply, nil
}
