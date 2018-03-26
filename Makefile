all: test_fmt test_lint test_local

# gopkg fits all v1.1, v1.2, ... in v1
gopath=$(shell go env GOPATH)
include $(gopath)/src/github.com/dedis/Coding/bin/Makefile.base
EXCLUDE_LINT = "should be.*UI|_test.go"

# You can use `test_playground` to run any test or part of cothority
# for more than once in Travis. Change `make test` in .travis.yml
# to `make test_playground`.
test_playground:
	cd skipchain; \
	for a in $$( seq 100 ); do \
	  go test -v -race -short || exit 1 ; \
	done;

# Other targets are:
# make create_stable

IMAGE_NAME = dedis/lleap
TEST_IMAGE_NAME = dedis/lleap-test

docker:
	docker build -t $(IMAGE_NAME) .

docker_test:
	docker build -t $(TEST_IMAGE_NAME) testImage

# We run docker in detatched mode, remember to clean it up. Ideally we'd like
# the java test to manage it.
docker_run:
	docker run -d -it --rm -p 7003:7003 -p 7005:7005 -p 7007:7007 --name lleap_test \
	 -v $(shell pwd)/data:/root/.local/share/conode $(TEST_IMAGE_NAME)

proto:
	awk -f proto.awk struct.go > external/proto/sicpa.proto
	cd external; make
