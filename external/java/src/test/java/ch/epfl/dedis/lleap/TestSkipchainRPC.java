package ch.epfl.dedis.lleap;

import ch.epfl.dedis.Local;
import ch.epfl.dedis.lib.ForwardLink;
import ch.epfl.dedis.lib.SkipBlock;
import ch.epfl.dedis.lib.exception.CothorityCommunicationException;
import ch.epfl.dedis.lib.exception.CothorityException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestSkipchainRPC {
    private static byte[] value;
    private static PublicKey publicKey;
    private static PrivateKey privateKey;
    private static SkipchainRPC sc;
    private static int KEY_SIZE = 4096;

    private static final Logger logger = LoggerFactory.getLogger(TestSkipchainRPC.class);

    @BeforeAll
    public static void initAll() throws CothorityException {
        value = "value".getBytes();

        privateKey = LLEAPKey.getPrivate();
        publicKey = DEDISSkipchain.getPublic();

        boolean useLocal = false;
        if (useLocal) {
            sc = new SkipchainRPC(Local.roster, publicKey);
        } else {
            sc = new SkipchainRPC();
        }
    }

    @Test
    public void connect() {
        assertTrue(sc.verify());
    }

    @Test
    public void wrongSignature() {
        // Write with wrong signature
        String keyStr = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        byte[] key = keyStr.getBytes();
        assertThrows(CothorityCommunicationException.class, () -> sc.setKeyValue(key, value, "".getBytes()));
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
        assertThrows(CothorityCommunicationException.class, () -> sc.setKeyValue(key, value, signature.sign()));

        // Get back value/signature from CISC
        KeyValueBlock kvb = sc.getKeyValueBlock(key);

        // Check the block integrity
        assertTrue(Arrays.equals(key, kvb.getKey()));
        assertTrue(Arrays.equals(value, kvb.getValue()));
        assertTrue(kvb.getTimestamp().length > 0);
        assertNotNull(kvb.getSignature());

        // Perform the check on the collective signature of the forward link
        assertTrue(kvb.verifyBlock(sc.getGenesis().toByteArray()));
        assertArrayEquals(key, kvb.getKey());

        // Verify the key/value signature
        Signature verify = Signature.getInstance("SHA256withRSA");
        verify.initVerify(publicKey);
        verify.update(message);
        assertTrue(verify.verify(kvb.getSignature()));
    }

    /**
     * This test iterates through the whole skipchain. It does this by starting with
     * the genesis block, and then following the forwardlinks. Each forwardlink is a proof
     * that two blocks are part of the same skipchain. There are multiple forwardlinks per
     * block. The first forwardlink points to the next block, the second forwardlink points
     * to the block+10, and so on. Only every tenth block has higher-level forwardlinks.
     *
     * For every block in the skipchain, this test will read the key/value stored in that block
     * and print it to the logger.
     * @throws Exception
     */
    @Test
    public void readFullList() throws Exception {
        SkipBlock iterator = sc.getGenesis();
        iterator = sc.getSkipblock(iterator.getRoster(), iterator.getId());
        for (;;) {
            List<ForwardLink> fls = iterator.getForwardLinks();
            if (fls.size() == 0){
                break;
            }
            logger.info("Got block {} with link-height {}", iterator.getIndex(), fls.size());
            if (iterator.getIndex() > 0) {
                KeyValueBlock kv = new KeyValueBlock(iterator);
                logger.info("Blocks key/value is {}/{}", kv.getKey(), kv.getValue());
            }
            iterator = sc.getSkipblock(iterator.getRoster(), fls.get(0).getTo());
        }
        logger.info("Reached end of chain");
    }

    /**
     * This test is similar as 'readFullList' but takes advantage of the skipchain
     * structure to find the latest block of the chain by following the highest
     * level forwardlinks.
     * @throws Exception
     */
    @Test
    public void readShortestPath() throws Exception {
        SkipBlock iterator = sc.getGenesis();
        iterator = sc.getSkipblock(iterator.getRoster(), iterator.getId());
        for (;;) {
            List<ForwardLink> fls = iterator.getForwardLinks();
            if (fls.size() == 0){
                break;
            }
            logger.info("Got block {} with link-height {}", iterator.getIndex(), fls.size());
            if (iterator.getIndex() > 0) {
                KeyValueBlock kv = new KeyValueBlock(iterator);
                logger.info("Blocks key/value is {}/{}", kv.getKey(), kv.getValue());
            }
            iterator = sc.getSkipblock(iterator.getRoster(), fls.get(fls.size() - 1).getTo());
        }
        logger.info("Reached end of chain");
    }


    @Test
    public void createSkipchain() {
        assertNotNull(sc);
    }
}
