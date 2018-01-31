package ch.epfl.dedis.lib.storage;

import ch.epfl.dedis.lib.Roster;
import ch.epfl.dedis.lib.darc.Darc;
import ch.epfl.dedis.lib.darc.Ed25519Signer;
import ch.epfl.dedis.lib.darc.SignaturePath;
import ch.epfl.dedis.lib.darc.Signer;
import ch.epfl.dedis.lib.exception.CothorityCommunicationException;
import ch.epfl.dedis.lib.exception.CothorityCryptoException;
import ch.epfl.dedis.sicpa.Key;
import ch.epfl.dedis.sicpa.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MockTest {
    public static Roster roster;
    public static List<ConfigurationEntry> config;
    public static Darc adminDarc;
    public static Ed25519Signer admin;
    public static Darc writerDarc;
    public static Ed25519Signer writer;
    public static Key key;
    public static Value value;

    @BeforeAll
    static void testAll() throws CothorityCryptoException, CothorityCommunicationException {
        admin = new Ed25519Signer();
        writer = new Ed25519Signer();
        adminDarc = new Darc(admin, null, null);
        writerDarc = new Darc(writer, Arrays.asList(writer), null);
        key = new Key("one".getBytes());
        value = new Value("two".getBytes());
    }

    @Test
        // Connects to an existing mock, then stores a key/value pair and retrieves it.
    void keyValue() throws CothorityCommunicationException, CothorityCryptoException {
        MockAdmin mockAdmin = new MockAdmin(admin.getIdentity(), writer.getIdentity());
        Storage mock = new Mock(mockAdmin.getStorageId());
        mock.setKeyValue(key, value, writer);
        assertArrayEquals(value.getId(), mock.getValue(key).getId());
        assertNull(mock.getValue(new Key("two".getBytes())));
    }

    @Test
    // Verify only correct writers can change a value
    void writeAccess() throws CothorityCommunicationException, CothorityCryptoException {
        MockAdmin mockAdmin = new MockAdmin(admin.getIdentity(), writer.getIdentity());
        Storage mock = new Mock(mockAdmin.getStorageId());

        Signer writer2 = new Ed25519Signer();
        mock.setKeyValue(key, new Value("three".getBytes()), writer2);
        Darc writerDarc2 = writerDarc.copy();
        writerDarc2.addUser(writer2);
        writerDarc2.setEvolution(writerDarc, new SignaturePath(writerDarc, writer, SignaturePath.OWNER), writer);
    }
}
