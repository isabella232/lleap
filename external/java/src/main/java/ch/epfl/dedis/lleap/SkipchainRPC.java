package ch.epfl.dedis.lleap;

import ch.epfl.dedis.lib.Roster;
import ch.epfl.dedis.lib.ServerIdentity;
import ch.epfl.dedis.lib.SkipBlock;
import ch.epfl.dedis.lib.SkipblockId;
import ch.epfl.dedis.lib.crypto.Hex;
import ch.epfl.dedis.lib.exception.CothorityCommunicationException;
import ch.epfl.dedis.lib.exception.CothorityCryptoException;
import ch.epfl.dedis.lib.exception.CothorityException;
import ch.epfl.dedis.proto.LleapProto;
import ch.epfl.dedis.proto.SkipBlockProto;
import ch.epfl.dedis.proto.SkipchainProto;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

/**
 * SkipchainRPC offers a reliable, fork-resistant storage of key/value pairs. This class connects to a
 * lleap service that uses the skipchain service (https://github.com/dedis/cothority/tree/master/skipchain)
 * from the cothority.
 * The skipchain service only stores arrays of data in each new skipblock. It is the Lleap service that
 * keeps the key/value pairs in a database, creates a merkle tree out of the key/value pairs and stores
 * the hash of the root-node together with the new key/value pairs in each new skipblock.
 * <p>
 * When creating a new skipchain, a public key is stored in the first block of the skipchain.
 * Every time a new key/value pair is stored, it needs to be signed by the corresponding private key
 * to proof that the writer does have access. The Lleap service stores the key/value pair together with
 * the signature in the skipchain.
 * <p>
 * When the corresponding value to a key is requested, the value together with the signature will be
 * returned to the service.
 */
public class SkipchainRPC {
    private SkipBlock genesis;
    private static int version = 1;
    private final Logger logger = LoggerFactory.getLogger(SkipchainRPC.class);

    /**
     * Initializes a SkipchainRPC with the standard node at roster. This uses the pre-stored and
     * pre-initialized values from the DEDISSkipchain class and accesses the nodes run by roster on the
     * server conode.dedis.ch on ports 15002-15006.
     *
     * @throws CothorityCryptoException
     */
    public SkipchainRPC() throws CothorityCryptoException, CothorityException {
        genesis = DEDISSkipchain.getGenesis();
    }

    /**
     * Initializes SkipchainRPC from a genesis block. All needed information is read from this block.
     *
     * @param genesisBuf
     * @throws CothorityException
     */
    public SkipchainRPC(byte[] genesisBuf) throws CothorityException {
        genesis = new SkipBlock(genesisBuf);
    }

    /**
     * Initializes a new skipchain given a roster of conodes and a public key that is
     * allowed to write on it.
     * This will ask the nodes defined in the roster to create a new skipchain and store
     * the public key in the genesis block.
     */
    public SkipchainRPC(Roster roster, PublicKey pub) throws CothorityCommunicationException {
        LleapProto.CreateSkipchain.Builder request =
                LleapProto.CreateSkipchain.newBuilder();
        request.setRoster(roster.getProto());
        request.setVersion(version);
        request.addWriters(ByteString.copyFrom(pub.getEncoded()));

        ByteString msg = roster.sendMessage("Lleap/CreateSkipchain",
                request.build());

        try {
            LleapProto.CreateSkipchainResponse reply = LleapProto.CreateSkipchainResponse.parseFrom(msg);
            if (reply.getVersion() != version) {
                throw new CothorityCommunicationException("Version mismatch");
            }
            logger.info("Created new skipchain:");
            genesis = new SkipBlock(reply.getSkipblock());
            logger.info(Hex.printHexBinary(genesis.getHash()));
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
        for (ServerIdentity n : getRoster().getNodes()) {
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

    /**
     * setKeyValue sends a key/value pair to the skipchain for inclusion. The skipchain will
     * verify the signature against the public key stored in the genesis-block and the message
     * which is the concatenation of (key | value).
     * <p>
     * setKeyValue will refuse to update a key - a key/value pair can only be stored once.
     *
     * @param key       under which key the value will be stored
     * @param value     the value to store, must be < 1MB
     * @param signature proofing that the writer is authorized. It should be a signature on
     *                  (key | value).
     * @throws CothorityCommunicationException
     */
    public void setKeyValue(byte[] key, byte[] value, byte[] signature) throws CothorityCommunicationException {
        LleapProto.SetKeyValue.Builder request =
                LleapProto.SetKeyValue.newBuilder();
        request.setKey(ByteString.copyFrom(key));
        request.setValue(ByteString.copyFrom(value));
        request.setSkipchainid(getSkipchainId().toBS());
        request.setVersion(version);
        request.setSignature(ByteString.copyFrom(signature));

        ByteString msg = getRoster().sendMessage("Lleap/SetKeyValue",
                request.build());

        try {
            LleapProto.SetKeyValueResponse reply = LleapProto.SetKeyValueResponse.parseFrom(msg);
            if (reply.getVersion() != version) {
                throw new CothorityCommunicationException("Version mismatch");
            }
            logger.info("Set key/value pair");
        } catch (InvalidProtocolBufferException e) {
            throw new CothorityCommunicationException(e);
        }
    }

    /**
     * Convenience function that will sign the key/value pair with the correct message
     * using the given privateKey. privateKey must correspond to the publicKey stored in
     * the genesis-block of the skipchain.
     * <p>
     * For the pre-configured skipchain, the private/public key is available in the
     * DEDISSkipchain class.
     *
     * @param key        under which key the value should be stored
     * @param value      any slice of bytes, must be < 1MB
     * @param privateKey will be used to sign the key/value pair
     * @throws CothorityCommunicationException
     */
    public void setKeyValue(byte[] key, byte[] value, PrivateKey privateKey) throws CothorityCommunicationException {
        try {
            // Create a signature on the key/value pair
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            byte[] message = new byte[key.length + value.length];
            System.arraycopy(key, 0, message, 0, key.length);
            System.arraycopy(value, 0, message, key.length, value.length);
            signature.update(message);

            // And write using the signature
            byte[] sig = signature.sign();
            setKeyValue(key, value, sig);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * getKeyValueBlock gets a KeyValueBlock for the corresponding key. It performs no verification on the data other
     * than that it is in the valid binary format. The caller should consult the functions in the KeyValueBlock class
     * to verify its integrity and access the value, signature and timestamp.
     *
     * @param key which key to retrieve
     * @return KeyValueBlock
     * @throws CothorityCommunicationException if a connection cannot be established or there is an error in the binary
     *                                         format.
     */
    public KeyValueBlock getKeyValueBlock(byte[] key) throws CothorityCommunicationException {
        LleapProto.GetValue.Builder request =
                LleapProto.GetValue.newBuilder();
        request.setKey(ByteString.copyFrom(key));
        request.setSkipchainid(getSkipchainId().toBS());
        request.setVersion(version);

        ByteString msg = getRoster().sendMessage("Lleap/GetValue",
                request.build());

        LleapProto.GetValueResponse reply;
        try {
            reply = LleapProto.GetValueResponse.parseFrom(msg);
            if (reply.getVersion() != version) {
                throw new CothorityCommunicationException("Version mismatch");
            }
        } catch (InvalidProtocolBufferException e) {
            throw new CothorityCommunicationException(e);
        }

        logger.info("Got key/value block");
        try {
            return new KeyValueBlock(reply);
        } catch (CothorityException e) {
            throw new CothorityCommunicationException(e.getMessage());
        }
    }

    /**
     * Returns the skipblock from the skipchain, given its id.
     *
     * @param id the id of the skipblock
     * @return the proto-representation of the skipblock.
     * @throws CothorityCommunicationException in case of communication difficulties
     */
    public SkipBlock getSkipblock(Roster roster, SkipblockId id) throws CothorityCommunicationException {
        SkipchainProto.GetSingleBlock request =
                SkipchainProto.GetSingleBlock.newBuilder().setId(ByteString.copyFrom(id.getId())).build();

        ByteString msg = roster.sendMessage("Skipchain/GetSingleBlock",
                request);

        try {
            SkipBlockProto.SkipBlock sb = SkipBlockProto.SkipBlock.parseFrom(msg);
            //TODO: add verification that the skipblock is valid by hashing and comparing to the id

            logger.debug("Got the following skipblock: {}", sb);
            logger.info("Successfully read skipblock");

            return new SkipBlock(sb);
        } catch (InvalidProtocolBufferException e) {
            throw new CothorityCommunicationException(e);
        }
    }


    /**
     * getGenesis returns the genesis block of the skipchain.
     *
     * @return genesis skipblock
     */
    public SkipBlock getGenesis(){
        return genesis;
    }

    /**
     * getRoster reads the roster from the genesis block.
     *
     * @return roster of the genesis block, or null if there was an error.
     */
    public Roster getRoster() {
        try {
            return genesis.getRoster();
        } catch (CothorityException e) {
            return null;
        }
    }

    /**
     * getSkipchainId reads the skipchain-id from the genesis block.
     *
     * @return the id of the skipchain, or null if there was an error.
     */
    public SkipblockId getSkipchainId() {
        try {
            return genesis.getSkipchainId();
        } catch (CothorityException e) {
            return null;
        }
    }
}
