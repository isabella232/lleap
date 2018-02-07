package ch.epfl.dedis.sicpa;

import ch.epfl.dedis.lib.Roster;
import ch.epfl.dedis.lib.ServerIdentity;
import ch.epfl.dedis.lib.SkipblockId;
import ch.epfl.dedis.lib.exception.CothorityCommunicationException;
import ch.epfl.dedis.lib.exception.CothorityCryptoException;
import ch.epfl.dedis.proto.SicpaProto;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;

/**
 * SkipchainRPC offers a reliable, fork-resistant storage of key/value pairs.
 */
public class SkipchainRPC {
    private Roster roster;
    private SkipblockId scid;
    private static int version = 1;
    private final Logger logger = LoggerFactory.getLogger(SkipchainRPC.class);
    /**
     * Initializes a Storage adapter with the standard node at DEDIS.
     * @throws CothorityCommunicationException
     */
    public SkipchainRPC() throws CothorityCommunicationException, CothorityCryptoException{
        this(Rosters.DEDIS, new SkipblockId(Rosters.DEDISID));
    }

    /**
     * Initializes a NEW skipchain
     */
    public SkipchainRPC(Roster roster) throws CothorityCommunicationException{
        this.roster = roster;
        SicpaProto.CreateSkipchain.Builder request =
                SicpaProto.CreateSkipchain.newBuilder();
        request.setRoster(roster.getProto());
        request.setVersion(version);

        ByteString msg = roster.sendMessage("Sicpa/CreateSkipchain",
                request.build());

        try {
            SicpaProto.CreateSkipchainResponse reply = SicpaProto.CreateSkipchainResponse.parseFrom(msg);
            if (reply.getVersion() != version){
                throw new CothorityCommunicationException("Version mismatch");
            }
            logger.info("Created new skipchain:");
            logger.info(DatatypeConverter.printHexBinary(reply.getSkipblock().getHash().toByteArray()));
            return;
        } catch (InvalidProtocolBufferException e) {
            throw new CothorityCommunicationException(e);
        }
    }

    /**
     * Initializes a new storage adapter with a given roster
     * @param roster the list of conodes
     * @throws CothorityCommunicationException
     */
    public SkipchainRPC(Roster roster, SkipblockId id) throws CothorityCommunicationException{
        this.roster = roster;
        this.scid = id;
    }

    public void setKeyValue(byte[] key, byte[] value) throws CothorityCommunicationException{
        SicpaProto.SetKeyValue.Builder request =
                SicpaProto.SetKeyValue.newBuilder();
        request.setKey(ByteString.copyFrom(key));
        request.setValue(ByteString.copyFrom(value));
        request.setSkipchainid(scid.toBS());
        request.setVersion(version);

        ByteString msg = roster.sendMessage("Sicpa/SetKeyValue",
                request.build());

        try {
            SicpaProto.SetKeyValueResponse reply = SicpaProto.SetKeyValueResponse.parseFrom(msg);
            if (reply.getVersion() != version){
                throw new CothorityCommunicationException("Version mismatch");
            }
            logger.info("Set key/value pair");
            return;
        } catch (InvalidProtocolBufferException e) {
            throw new CothorityCommunicationException(e);
        }
    }

    public byte[] getValue(byte[] key) throws CothorityCommunicationException{
        SicpaProto.GetValue.Builder request =
                SicpaProto.GetValue.newBuilder();
        request.setKey(ByteString.copyFrom(key));
        request.setSkipchainid(scid.toBS());
        request.setVersion(version);

        ByteString msg = roster.sendMessage("Sicpa/GetValue",
                request.build());

        try {
            SicpaProto.GetValueResponse reply = SicpaProto.GetValueResponse.parseFrom(msg);
            if (reply.getVersion() != version){
                throw new CothorityCommunicationException("Version mismatch");
            }
            logger.info("Got value");
            return reply.getValue().toByteArray();
        } catch (InvalidProtocolBufferException e) {
            throw new CothorityCommunicationException(e);
        }
    }

    /**
     * Contacts all nodes in the cothority and returns true only if _all_
     * nodes returned OK.
     *
     * @return true only if all nodes are OK, else false.
     */
    public boolean verify() {
        boolean ok = true;
        for (ServerIdentity n : roster.getNodes()) {
            logger.info("Testing node {}", n.getAddress());
            try {
                n.GetStatus();
            } catch (CothorityCommunicationException e) {
                logger.warn("Failing node {}", n.getAddress());
                ok = false;
            }
        }
        return ok;
    }
}
