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

/**
 * TestApp class shows two main tests on how to use the lleap-skipchains to store and retrieve key/value pairs:
 * - writeAndRead is the online case, where both the writer and the reader have access to the internet.
 * - verifyOffline shows how to get a byte[] representation of the key/value pair and how to verify that
 *   it is part of the skipchain.
 */
public class TestApp {

    // The following test shows all steps necessary for an online client to write and read a key from the
    // skipchain.
    @Test
    public void writeAndRead() throws Exception {
        // Initialising private/public keys
        PrivateKey privateKey = LLEAPKey.getPrivate();
        PublicKey publicKey = LLEAPKey.getPublic();

        // Connecting to the skipchain and verifying the connection
        SkipchainRPC sc;

        // useLocal is for testing, so we don't have to use the public lleap skipchain. If useLocal == false, then
        // we do use the public lleap skipchain.
        boolean useLocal = false;
        if (useLocal) {
            // In the local case, we create a new skipchain on every test.
            sc = new SkipchainRPC(Local.roster, publicKey);
        } else {
            // For the public lleap skipchain, we use the preconfigured values.
            sc = new SkipchainRPC();
        }
        // Verify if the conodes holding the skipchain are alive or not.
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
        // which implies inclusion.
        KeyValueBlock kvb = sc.getKeyValueBlock(key);
        assertTrue(kvb.verifyBlock(sc.getGenesis()));
        assertArrayEquals(key, kvb.getKey());
        assertArrayEquals(value, kvb.getValue());
<<<<<<< HEAD
        assertTrue(kvb.verifyBlock(key, sc.getGenesis()));
=======
>>>>>>> 372fa1a563c2efd48e8457e4991b99f3bfd72d53

        // Verify the writer's signature on the key/value pair
        Signature verify = Signature.getInstance("SHA256withRSA");
        verify.initVerify(publicKey);
        verify.update(kvb.getSignedMsg());
        assertTrue(verify.verify(kvb.getSignature()));
    }

    // This test shows how to store a key/value pair and then verify it from the client who can be offline,
    // but needs the genesis block to verify the inclusion in the skipchain of the key/value block.
    @Test
    public void verifyOffline()throws Exception{
        // ** Online client will connect, store a key/value pair and retrieve the proof it is on the skipchain.

        // Connect to the lleap skipchain and verify the conode is up and running.
        SkipchainRPC sc = connect();

        // Store a new key/value
        String keyStr = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        byte[] key = keyStr.getBytes();
        byte[] value = "hashes".getBytes();
        sc.setKeyValue(key, value, LLEAPKey.getPrivate());

        // Reading it back from the skipchain. The keyValueBlock holds the following information:
        // - signature by the conodes on the block holding the key/value pair
        // - block where the key/value pair is stored
        KeyValueBlock kvb = sc.getKeyValueBlock(key);

        // Now we can send a binary representation of that KeyValueBlock to an offline client. The offline client
        // needs to have access to the genesis block.
        offlineVerify(key, kvb.toByteArray(), DEDISSkipchain.getGenesis().toByteArray());
    }

    // offlineVerify shows how an offline verifier can verify that a key/value is actually on the blockchain. It does
    // so by verifying that there is a valid signature on the block holding the key/value pair.
    public void offlineVerify(byte[] key, byte[] kvbBuf, byte[] genesisBuf) throws Exception{
        // Recreate the KeyValueBlock
        KeyValueBlock kvb = new KeyValueBlock(kvbBuf);

        // Verify the block is valid. The client needs to have a copy of the genesis block to do this.
        assertTrue(kvb.verifyBlock(genesisBuf));

        // Now we know that the block is valid and can check it holds the key we're interested in:
        assertArrayEquals(key, kvb.getKey());

        // and then we can retrieve the value and verify it's what we stored.
        byte[] value = kvb.getValue();
        assertArrayEquals("hashes".getBytes(), value);
    }

    // connect either uses the public lleap skipchain, or a local skipchain for tests.
    public SkipchainRPC connect() throws Exception{
        // Connecting to the skipchain and verifying the connection
        SkipchainRPC sc;

        // useLocal is for testing, so we don't have to use the public lleap skipchain. If useLocal == false, then
        // we do use the public lleap skipchain.
        boolean useLocal = false;
        if (useLocal) {
            // In the local case, we create a new skipchain on every test.
            sc = new SkipchainRPC(Local.roster, DEDISSkipchain.getPublic());
        } else {
            // For the public lleap skipchain, we use the preconfigured values.
            sc = new SkipchainRPC();
        }
        // Verify if the conodes holding the skipchain are alive or not.
        if (!sc.verify()) {
            throw new RuntimeException("couldn't connect to skipchain");
        }

        return sc;
    }
}
