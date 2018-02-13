package ch.epfl.dedis.lleap;

import ch.epfl.dedis.lib.exception.CothorityCommunicationException;
import javafx.util.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.security.rsa.RSAPrivateCrtKeyImpl;

import javax.xml.bind.DatatypeConverter;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;

import static org.junit.jupiter.api.Assertions.*;

public class TestSkipchainRPC {
    private static byte[] key;
    private static byte[] value;
    private static PublicKey publicKey;
    private static PrivateKey privateKey;
    private static SkipchainRPC sc;

    static int KEY_SIZE = 4096;

    @BeforeAll
    public static void initAll() throws Exception {
        key = "first".getBytes();
        value = "value".getBytes();

        privateKey = Rosters.getPrivate();
        publicKey = Rosters.getPublic();

        boolean useLocal = false;
        if (useLocal) {
            sc = new SkipchainRPC(Rosters.Local, publicKey);
        } else {
            sc = new SkipchainRPC();
        }
    }

    @Test
    public void setupKP() throws Exception{
        KeyPairGenerator kpc = KeyPairGenerator.getInstance("RSA");
        kpc.initialize(KEY_SIZE, new SecureRandom());
        KeyPair kp = kpc.generateKeyPair();
        System.out.println(DatatypeConverter.printHexBinary(kp.getPrivate().getEncoded()));
    }

    @Test
    public void connect() throws Exception {
        assertTrue(sc.verify());
    }

    @Test
    public void wrongSignature(){
        // Write with wrong signature
        assertThrows(CothorityCommunicationException.class, ()->sc.setKeyValue(key, value, "".getBytes()));
    }

    @Test
    public void writeAndReadFull() throws Exception {
        // Create correct signature
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        byte[] message = new byte[key.length + value.length];
        System.arraycopy(key, 0, message, 0, key.length);
        System.arraycopy(value, 0, message, key.length, value.length);
        signature.update(message);

        // And write using the signature
        sc.setKeyValue(key, value, signature.sign());

        // Get back value/signature from CISC
        Pair<byte[], byte[]> valueSig = sc.getValue(key);
        assertArrayEquals(value, valueSig.getKey());

        // Verify the signature
        // TODO: verify the inclusion proof
        Signature verify = Signature.getInstance("SHA256withRSA");
        verify.initVerify(publicKey);
        verify.update(message);
        assertTrue(verify.verify(valueSig.getValue()));
    }

    @Test
    public void createSkipchain() throws Exception {
        assertNotNull(sc);
    }
}
