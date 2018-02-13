package ch.epfl.dedis.lleap;

import ch.epfl.dedis.lib.Roster;
import ch.epfl.dedis.lib.ServerIdentity;
import ch.epfl.dedis.lib.SkipblockId;
import ch.epfl.dedis.lib.exception.CothorityCommunicationException;
import ch.epfl.dedis.lib.exception.CothorityCryptoException;
import ch.epfl.dedis.proto.SicpaProto;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.security.*;

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
     *
     * @throws CothorityCommunicationException
     */
    public SkipchainRPC() throws CothorityCommunicationException, CothorityCryptoException {
        this(Rosters.DEDIS, new SkipblockId(Rosters.DEDISID));
    }

    /**
     * Initializes a NEW skipchain given a roster of conodes and a public key that is
     * allowed to write on it.
     */
    public SkipchainRPC(Roster roster, PublicKey pub) throws CothorityCommunicationException {
        this.roster = roster;
        SicpaProto.CreateSkipchain.Builder request =
                SicpaProto.CreateSkipchain.newBuilder();
        request.setRoster(roster.getProto());
        request.setVersion(version);
        request.addWriters(ByteString.copyFrom(pub.getEncoded()));

        ByteString msg = roster.sendMessage("Sicpa/CreateSkipchain",
                request.build());

        try {
            SicpaProto.CreateSkipchainResponse reply = SicpaProto.CreateSkipchainResponse.parseFrom(msg);
            if (reply.getVersion() != version) {
                throw new CothorityCommunicationException("Version mismatch");
            }
            logger.info("Created new skipchain:");
            logger.info(DatatypeConverter.printHexBinary(reply.getSkipblock().getHash().toByteArray()));
            this.scid = new SkipblockId(reply.getSkipblock().getHash().toByteArray());
            return;
        } catch (InvalidProtocolBufferException e) {
            throw new CothorityCommunicationException(e);
        } catch (CothorityCryptoException e) {
            throw new CothorityCommunicationException(e.getMessage());
        }
    }

    /**
     * Initializes a new storage adapter with a given roster
     *
     * @param roster the list of conodes
     * @throws CothorityCommunicationException
     */
    public SkipchainRPC(Roster roster, SkipblockId id) throws CothorityCommunicationException {
        this.roster = roster;
        this.scid = id;
    }

    /**
     * setKeyValue sends a key/value pair to the skipchain for inclusion. The skipchain will
     * verify that the signature comes from a valid public key. It will verify the signature
     * against the concatenation of (key | value).
     *
     * @param key       under which key the value will be stored
     * @param value     the value to store
     * @param signature proofing that the writer is authorized. It should be a signature on
     *                  (key | value).
     * @throws CothorityCommunicationException
     */
    public void setKeyValue(byte[] key, byte[] value, byte[] signature) throws CothorityCommunicationException {
        SicpaProto.SetKeyValue.Builder request =
                SicpaProto.SetKeyValue.newBuilder();
        request.setKey(ByteString.copyFrom(key));
        request.setValue(ByteString.copyFrom(value));
        request.setSkipchainid(scid.toBS());
        request.setVersion(version);
        request.setSignature(ByteString.copyFrom(signature));

        ByteString msg = roster.sendMessage("Sicpa/SetKeyValue",
                request.build());

        try {
            SicpaProto.SetKeyValueResponse reply = SicpaProto.SetKeyValueResponse.parseFrom(msg);
            if (reply.getVersion() != version) {
                throw new CothorityCommunicationException("Version mismatch");
            }
            logger.info("Set key/value pair");
            return;
        } catch (InvalidProtocolBufferException e) {
            throw new CothorityCommunicationException(e);
        }
    }

    public void setKeyValue(byte[] key, byte[] value, PrivateKey privateKey) throws CothorityCommunicationException{
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            byte[] message = new byte[key.length + value.length];
            System.arraycopy(key, 0, message, 0, key.length);
            System.arraycopy(value, 0, message, key.length, value.length);
            signature.update(message);

            // And write using the signature
            byte[] sig = signature.sign();
            setKeyValue(key, value, sig);
        } catch (InvalidKeyException e){
            throw new RuntimeException(e.getMessage());
        } catch (NoSuchAlgorithmException e){
            throw new RuntimeException(e.getMessage());
        } catch (SignatureException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * getValue returns the value/signature pair of a given key. The signature will verify
     * against the public key of the writer. The message of the signature is the concatenation
     * of (key | value).
     *
     * @param key which key to retrieve
     * @return a value / signature pair
     * @throws CothorityCommunicationException
     */
    public Pair<byte[], byte[]> getValue(byte[] key) throws CothorityCommunicationException {
        SicpaProto.GetValue.Builder request =
                SicpaProto.GetValue.newBuilder();
        request.setKey(ByteString.copyFrom(key));
        request.setSkipchainid(scid.toBS());
        request.setVersion(version);

        ByteString msg = roster.sendMessage("Sicpa/GetValue",
                request.build());

        try {
            SicpaProto.GetValueResponse reply = SicpaProto.GetValueResponse.parseFrom(msg);
            if (reply.getVersion() != version) {
                throw new CothorityCommunicationException("Version mismatch");
            }
            logger.info("Got value");
            return new Pair<>(reply.getValue().toByteArray(),
                    reply.getSignature().toByteArray());
        } catch (InvalidProtocolBufferException e) {
            throw new CothorityCommunicationException(e);
        }
    }

    public byte[] getValue(byte[] key, PublicKey publicKey) throws CothorityCommunicationException{
        Pair<byte[], byte[]> valueSig = getValue(key);

        byte[] value = valueSig.getKey();
        byte[] message = new byte[key.length + value.length];
        System.arraycopy(key, 0, message, 0, key.length);
        System.arraycopy(value, 0, message, key.length, value.length);
        try {
            Signature verify = Signature.getInstance("SHA256withRSA");
            verify.initVerify(publicKey);
            verify.update(message);
            if (!verify.verify(valueSig.getValue())) {
                throw new CothorityCommunicationException("Signature verification failed");
            }
            // TODO: verify the inclusion proof
        } catch (InvalidKeyException e){
            throw new RuntimeException(e.getMessage());
        } catch (NoSuchAlgorithmException e){
            throw new RuntimeException(e.getMessage());
        } catch (SignatureException e){
            throw new RuntimeException(e.getMessage());
        }
        return value;
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
