package ch.epfl.dedis.lleap;

import ch.epfl.dedis.Local;
import ch.epfl.dedis.lib.exception.CothorityCommunicationException;
import ch.epfl.dedis.lib.exception.CothorityException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.xml.bind.DatatypeConverter;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TestSkipchainRPC {
    private static byte[] value;
    private static PublicKey publicKey;
    private static PrivateKey privateKey;
    private static SkipchainRPC sc;
    private static int KEY_SIZE = 4096;
    private static String genesisHex = "";

    @BeforeAll
    public static void initAll() throws CothorityException {
        value = "value".getBytes();

        privateKey = DEDISSkipchain.getPrivate();
        publicKey = DEDISSkipchain.getPublic();

        boolean useLocal = false;
        if (useLocal) {
            sc = new SkipchainRPC(Local.roster, publicKey);
            genesisHex = Local.genesisHex;
        } else {
            sc = new SkipchainRPC();
            genesisHex = DEDISSkipchain.genesisHex;
        }
    }

    @Test
    public void checkGenesis() throws CothorityException {
        SkipchainRPC sc = new SkipchainRPC(DatatypeConverter.parseHexBinary(genesisHex));
        // TODO we should compare this one to what is initialised in initAll
    }

    @Test
    public void connect() {
        assertTrue(sc.verify());
    }

    @Test
    public void wrongSignature(){
        // Write with wrong signature
        String keyStr = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        byte[] key = keyStr.getBytes();
        assertThrows(CothorityCommunicationException.class, ()->sc.setKeyValue(key, value, "".getBytes()));
    }

    @Test
    public void writeAndReadFull() throws Exception {
        String keyStr = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        byte[] key = keyStr.getBytes();

        // Create correct signature
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);

        byte[] message = new byte[key.length + value.length];
        System.arraycopy(key, 0, message, 0, key.length);
        System.arraycopy(value, 0, message, key.length, value.length);
        signature.update(message);

        // And write using the signature
        sc.setKeyValue(key, value, signature.sign());

        // Verify we cannot overwrite value
        assertThrows(CothorityCommunicationException.class, ()->sc.setKeyValue(key, value, signature.sign()));

        // Get back value/signature from CISC
        KeyValueBlock kvb = sc.getKeyValueBlock(key);

        // Check the block integrity
        assertTrue(Arrays.equals(key, kvb.getKey()));
        assertTrue(Arrays.equals(value, kvb.getValue()));
        assertTrue(kvb.getTimestamp().length > 0);
        assertNotNull(kvb.getSignature());

        // Perform the check on the collective signature of the forward link
        assertTrue(kvb.verifyBlock(key, DatatypeConverter.parseHexBinary(genesisHex)));

        // Verify the key/value signature
        Signature verify = Signature.getInstance("SHA256withRSA");
        verify.initVerify(publicKey);
        verify.update(message);
        assertTrue(verify.verify(kvb.getSignature()));
    }

    @Test
    public void createSkipchain() {
        assertNotNull(sc);
    }
}
