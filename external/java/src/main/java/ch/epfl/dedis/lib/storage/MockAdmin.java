package ch.epfl.dedis.lib.storage;

import ch.epfl.dedis.lib.darc.Darc;
import ch.epfl.dedis.lib.darc.Identity;
import ch.epfl.dedis.lib.darc.Signer;
import ch.epfl.dedis.lib.exception.CothorityCommunicationException;
import ch.epfl.dedis.lib.exception.CothorityCryptoException;
import ch.epfl.dedis.lib.exception.CothorityNotFoundException;
import ch.epfl.dedis.sicpa.Key;
import ch.epfl.dedis.sicpa.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock can be used to simulate access to the storage backend of the blockchain.
 * It simulates a free key/value storage.
 */
public class MockAdmin extends StorageAdmin {
    public static Map<StorageId, MockAdmin> mocks = new HashMap<>();
    public Map<Key, Value> storage = new HashMap<>();

    private StorageId id;

    public MockAdmin(Identity admin, Identity writer) throws CothorityCommunicationException, CothorityCryptoException {
        super(admin, writer);
        try {
            this.id = new StorageId(admins.getId().getId());
        } catch (CothorityCryptoException e) {
            throw new CothorityCommunicationException(e.getMessage());
        }
        this.mocks.put(this.getStorageId(), this);
    }

    public static MockAdmin Connect(Signer admin, StorageId mock) throws CothorityNotFoundException {
        if (mocks.containsKey(mock)) {
            return mocks.get(mock);
        }
        throw new CothorityNotFoundException("didn't find mock with this id");
    }

    public void updateConfiguration(String key, String value) throws CothorityCommunicationException {
        throw new CothorityCommunicationException("No configuration supported");
    }

    public String getConfiguration(String key) throws CothorityCommunicationException {
        throw new CothorityCommunicationException("No configuration supported");
    }

    public String getStatus(String key) throws CothorityCommunicationException {
        switch (key) {
            case "ping":
                return "pong";
        }
        throw new CothorityCommunicationException("Command not recognized");
    }

    public StorageId getStorageId() {
        return id;
    }

    public void updateDarc(Darc newdarc) throws CothorityCommunicationException {
    }

    public Storage getStorage() throws CothorityCommunicationException {
        return new Mock(getStorageId());
    }
}
