package ch.epfl.dedis.lleap;

import ch.epfl.dedis.lib.CISC;
import ch.epfl.dedis.lib.SkipBlock;
import ch.epfl.dedis.lib.crypto.SchnorrSig;
import ch.epfl.dedis.lib.exception.CothorityException;
import ch.epfl.dedis.proto.IdentityProto;
import ch.epfl.dedis.proto.LleapProto;
import ch.epfl.dedis.proto.SkipBlockProto;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * KeyValueBlock is the response that is returned by SkipchainRPC. It allows the user to access various properties of
 * the response and, more importantly, verify the collective signature which implies block inclusion.
 */
public class KeyValueBlock {
    private LleapProto.GetValueResponse resp;
    private final Logger logger = LoggerFactory.getLogger(KeyValueBlock.class);
    private IdentityProto.Data data = null;
    private CISC cisc;

    public KeyValueBlock(LleapProto.GetValueResponse resp) throws CothorityException {
        this.resp = resp;
        try {
            cisc = new CISC(this.resp.getSkipblock());
        } catch (InvalidProtocolBufferException e) {
            throw new CothorityException(e);
        }
    }

    public KeyValueBlock(byte[] buf) throws CothorityException {
        try {
            resp = LleapProto.GetValueResponse.parseFrom(buf);
            cisc = new CISC(this.resp.getSkipblock());
        } catch (InvalidProtocolBufferException e) {
            throw new CothorityException(e);
        }
    }

    /**
     * verifyBlock verifies the block by performing various integrity checks and, more importantly, checks the
     * collective signature which implies block inclusion.
     *
     * @param key     is the key of interest.
     * @param genesis is the genesis block. The user is expected to store the genesis block at
     *                initialisation.
     * @return true if the verification is ok, otherwise false.
     */
    public boolean verifyBlock(byte[] key, SkipBlock genesis) {
        try {
            // sanity check on the key/value pairs and the forward link
            if (this.getKey() == null) {
                logger.error("key 'newkey' does not exist");
                return false;
            }
            if (!Arrays.equals(key, getKey())) {
                logger.error("mismatch key");
                return false;
            }

            if (!resp.getSkipblock().getHash().equals(this.resp.getForwardlink().getTo())) {
                logger.error("bad forward link");
                return false;
            }

            // forward link hash and check it
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                logger.error("must find SHA-256");
                return false;
            }
            digest.update(resp.getForwardlink().getFrom().toByteArray());
            digest.update(resp.getForwardlink().getTo().toByteArray());
            if (resp.getForwardlink().hasNewRoster()) {
                digest.update(resp.getForwardlink().getNewRoster().getId().toByteArray());
            }
            if (!Arrays.equals(resp.getForwardlink().getSignature().getMsg().toByteArray(), digest.digest())) {
                logger.error("msg in signature is not the same as forward link digest");
                return false;
            }

            // check the collective signature
            SkipBlockProto.FinalSignature fwlSig = resp.getForwardlink().getSignature();
            SchnorrSig schnorr = new SchnorrSig(fwlSig.getSig().toByteArray());
            if (!schnorr.verify(fwlSig.getMsg().toByteArray(), genesis.getRoster().getAggregate())) {
                logger.error("aggregate signature verification failed");
                return false;
            }
        } catch (CothorityException e) {
            logger.error(e.toString());
            return false;
        }
        return true;
    }

    /**
     * verifyBlock is a convenience function for verifyBlock, if you have the genesis block as an array of bytes
     *
     * @param key        is the key of interest.
     * @param genesisBuf is the genesis block as a byte array.
     * @return true if the verification is ok, otherwise false.
     */
    public boolean verifyBlock(byte[] key, byte[] genesisBuf) {
        try {
            return verifyBlock(key, new SkipBlock(genesisBuf));
        } catch (CothorityException e) {
            logger.error(e.toString());
            return false;
        }
    }

    public byte[] getValue() throws CothorityException {
        return cisc.getValue("newvalue");
    }

    public byte[] getKey() throws CothorityException {
        return cisc.getValue("newkey");
    }

    public byte[] getTimestamp() throws CothorityException {
        return cisc.getValue("timestamp");
    }

    public byte[] getSignature() throws CothorityException {
        return cisc.getValue("newsig");
    }

    public byte[] getSignedMsg() throws CothorityException {
        byte[] key = this.getKey();
        byte[] value = this.getValue();
        byte[] message = new byte[key.length + value.length];
        System.arraycopy(key, 0, message, 0, key.length);
        System.arraycopy(value, 0, message, key.length, value.length);
        return message;
    }

    public byte[] toByteArray() {
        return resp.toByteArray();
    }
}