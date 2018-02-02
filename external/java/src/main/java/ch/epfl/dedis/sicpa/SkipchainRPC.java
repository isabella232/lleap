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
    private final Logger logger = LoggerFactory.getLogger(SkipchainRPC.class);
    /**
     * Initializes a Storage adapter with the standard node at DEDIS.
     * @throws CothorityCommunicationException
     */
    public SkipchainRPC() throws CothorityCommunicationException, CothorityCryptoException{
        this(Roster.FromToml("here be toml-file"),
                new SkipblockId(DatatypeConverter.parseHexBinary("coffeebabe")));
    }

    /**
     * Initializes a new storage adapter with a given roster
     * @param roster the list of conodes
     * @throws CothorityCommunicationException
     */
    public SkipchainRPC(Roster roster, SkipblockId id) throws CothorityCommunicationException{

    }

    public void setKeyValue(byte[] key, byte[] value) throws CothorityCommunicationException{
        SicpaProto.AddKeyValue.Builder request =
                SicpaProto.AddKeyValue.newBuilder();
        request.setKey(ByteString.copyFrom(key));
        request.setValue(ByteString.copyFrom(value));

        ByteString msg = roster.sendMessage("Sicpa/AddKeyValue",
                request.build());

        try {
            SicpaProto.AddKeyValueResponse reply = SicpaProto.AddKeyValueResponse.parseFrom(msg);
            logger.info("Set key/value pair");
            return;
        } catch (InvalidProtocolBufferException e) {
            throw new CothorityCommunicationException(e);
        }

    }

    public byte[] getValue(byte[] key) throws CothorityCommunicationException{
        return "".getBytes();
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
