FROM dedis/lleap

EXPOSE 7003 7005 7007

COPY co1/private.toml co1/private.toml
COPY co2/private.toml co2/private.toml
COPY co3/private.toml co3/private.toml

COPY co1/public.toml co1/public.toml
COPY co2/public.toml co2/public.toml
COPY co3/public.toml co3/public.toml

ENV DEBUG=1
ENV GODEBUG=gctrace=0

# if you prefer different debug level use docker command line -e DEBUG=3 -e GODEBUG=gctrace=1

# local - run this as a set of local nodes in the docker
# 3 - number of nodes to run
# 2 - debug-level: 0 - none .. 5 - a lot
# -wait - don't return from script when all nodes are started
CMD conode/run_conode.sh local 3 ${DEBUG} -wait
