package ch.epfl.dedis.lib.crypto;

import ch.epfl.dedis.proto.SkipBlockProto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SchnorrSig {
    public Point challenge;
    public Scalar response;

    public SchnorrSig(byte[] msg, Scalar priv) {
        KeyPair kp = new KeyPair();
        challenge = kp.Point;

        Point pub = priv.scalarMult(null);
        Scalar xh = priv.mul(toHash(challenge, pub, msg));
        response = kp.Scalar.add(xh);
    }

    public SchnorrSig(byte[] data) {
        challenge = new Point(Arrays.copyOfRange(data, 0, 32));
        response = new Scalar(Arrays.copyOfRange(data, 32, 64));
    }

    public boolean verify(byte[] msg, Point pub) {
        Scalar hash = toHash(challenge, pub, msg);
        Point S = response.scalarMult(null);
        Point Ah = pub.scalarMult(hash);
        Point RAs = challenge.add(Ah);
        return S.equals(RAs);
    }

    public byte[] toBytes() {
        byte[] buf = new byte[64];
        System.arraycopy(challenge.toBytes(), 0, buf, 0, 32);
        System.arraycopy(response.toBytes(), 0, buf, 32, 32);
        return buf;
    }

    /**
     * Function toHash converts a challenge, a public key and a message to a Scalar. Schnorr signatures work with any
     * hash function. We use SHA-256 here as it is what is needed for the corresponding conodes.
     */
    private Scalar toHash(Point challenge, Point pub, byte[] msg) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(challenge.toBytes());
            digest.update(pub.toBytes());
            digest.update(msg);
            byte[] hash = Arrays.copyOfRange(digest.digest(), 0, 64);
            return new Scalar(hash);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
