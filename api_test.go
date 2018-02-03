package sicpa_test

import (
	"testing"

	// We need to include the service so it is started.
	"github.com/dedis/kyber/suites"
	"github.com/dedis/onet/log"
	_ "github.com/dedis/sicpa/service"
)

var tSuite = suites.MustFind("Ed25519")

func TestMain(m *testing.M) {
	log.MainTest(m)
}
