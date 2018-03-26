package ch.epfl.dedis.lleap;

import ch.epfl.dedis.Local;
import org.junit.jupiter.api.Test;

import javax.xml.bind.DatatypeConverter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestApp {
    @Test
    public void writeAndRead() throws Exception {
        // Initialising private/public keys
        PrivateKey privateKey = DEDISSkipchain.getPrivate();
        PublicKey publicKey = DEDISSkipchain.getPublic();

        // Connecting to the skipchain and verifying the connection
        SkipchainRPC sc;
        String genesisHex;

        boolean useLocal = false;
        if (useLocal) {
            sc = new SkipchainRPC(Local.roster, publicKey);
            genesisHex = Local.genesisHex;
        } else {
            sc = new SkipchainRPC();
            genesisHex = DEDISSkipchain.genesisHex;
        }
        if (!sc.verify()) {
            throw new RuntimeException("couldn't connect to skipchain");
        }

        // Writing a key/value pair to the skipchain - we cannot overwrite
        // existing values, so we create a different value depending on
        // date/time.
        String keyStr = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        byte[] key = keyStr.getBytes();
        byte[] value = "hashes".getBytes();
        sc.setKeyValue(key, value, privateKey);

        // Reading it back from the skipchain, and verify the collective signature on the forward link,
        // which implies inclusion
        KeyValueBlock kvb = sc.getKeyValueBlock(key);
        assertArrayEquals(value, kvb.getValue());
        assertTrue(kvb.verifyBlock(key, DatatypeConverter.parseHexBinary(genesisHex)));

        // Verify the signature on the key/value pair
        Signature verify = Signature.getInstance("SHA256withRSA");
        verify.initVerify(publicKey);
        verify.update(kvb.getSignedMsg());
        assertTrue(verify.verify(kvb.getSignature()));
    }
}
