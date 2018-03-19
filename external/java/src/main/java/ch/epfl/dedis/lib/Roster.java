package ch.epfl.dedis.lib;

import ch.epfl.dedis.lib.crypto.Ed25519;
import ch.epfl.dedis.lib.crypto.Point;
import ch.epfl.dedis.lib.exception.CothorityCommunicationException;
import ch.epfl.dedis.proto.RosterProto;
import ch.epfl.dedis.proto.ServerIdentityProto;
import com.google.protobuf.ByteString;
import com.moandjiezana.toml.Toml;

import javax.xml.bind.DatatypeConverter;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * dedis/lib
 * Roster.java
 * Purpose: A list of ServerIdentities make up a roster that can be used as a temporary
 * cothority.
 */

public class Roster {
    private List<ServerIdentity> nodes = new ArrayList<>();
    private Point aggregate; // TODO: can we find better name for it? like aggregatePublicKey or aggregatedKey?
    private UUID id;

    public Roster(List<ServerIdentity> servers) {
        nodes.addAll(servers);

        aggregate = Point.zero();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (final ServerIdentity serverIdentity : nodes) {
                aggregate = aggregate.add(serverIdentity.Public);
                digest.update(serverIdentity.Public.toBytes());
            }
            id = UUIDType5.nameUUIDFromNamespaceAndString(UUIDType5.NAMESPACE_URL, DatatypeConverter.printHexBinary(digest.digest()));
        } catch (NoSuchAlgorithmException e){
            throw new RuntimeException("didn't find sha256");
        }
    }

    public Roster(RosterProto.Roster roster) throws URISyntaxException {
        aggregate = Point.zero();
        id = UUIDType5.fromBytes(roster.getId().toByteArray());
        for (ServerIdentityProto.ServerIdentity siP : roster.getListList()) {
            ServerIdentity si = new ServerIdentity(siP);
            nodes.add(si);
            aggregate = aggregate.add(si.Public);
        }
    }

    public UUID getId() {
        return id;
    }

    public List<ServerIdentity> getNodes() {
        return nodes;
    }

    public Point getAggregate(){
        return aggregate;
    }

    public RosterProto.Roster getProto() {
        RosterProto.Roster.Builder r = RosterProto.Roster.newBuilder();
        r.setId(ByteString.copyFrom(Ed25519.uuid4()));
        for (ServerIdentity si : nodes) {
            r.addList(si.getProto());
        }
        r.setAggregate(aggregate.toProto());

        return r.build();
    }

    public ByteString sendMessage(String path, com.google.protobuf.GeneratedMessageV3 proto) throws CothorityCommunicationException {
        // TODO - fetch a random node.
        return ByteString.copyFrom(nodes.get(0).SendMessage(path, proto.toByteArray()));
    }

    public static Roster FromToml(String groupToml) {
        Toml toml = new Toml().read(groupToml);
        List<ServerIdentity> cothority = new ArrayList<>();
        List<Toml> servers = toml.getTables("servers");

        for (Toml s : servers) {
            try {
                cothority.add(new ServerIdentity(s));
            } catch (URISyntaxException e) {
            }
        }
        return new Roster(cothority);
    }
}
