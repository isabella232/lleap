package ch.epfl.dedis.lib.storage;

import ch.epfl.dedis.lib.darc.Identity;
import ch.epfl.dedis.lib.darc.Signer;
import ch.epfl.dedis.lib.exception.CothorityCommunicationException;
import ch.epfl.dedis.lib.exception.CothorityCryptoException;
import ch.epfl.dedis.sicpa.Key;
import ch.epfl.dedis.sicpa.Value;

/**
 * Mock can be used to simulate access to the storage backend of the blockchain.
 * It simulates a free key/value storage.
 */
public class Mock extends Storage {
    private MockAdmin mock;

    public Mock(StorageId storageId) throws CothorityCommunicationException {
        if (!MockAdmin.mocks.containsKey(storageId)) {
            throw new CothorityCommunicationException("no such mock found");
        }
        mock = MockAdmin.mocks.get(storageId);
    }

    public void setKeyValue(Key key, Value value, Signer writer) throws CothorityCommunicationException {
        Identity w;
        try {
            w = writer.getIdentity();
        } catch (CothorityCryptoException e){
            throw new CothorityCommunicationException("Crypto-exception:" + e.getMessage());
        }
        if (mock.writers.hasUser(w)) {
            mock.storage.put(key, value);
        } else {
            throw new CothorityCommunicationException("not correct writer");
        }
    }

    public Value getValue(Key key) throws CothorityCommunicationException {
        return mock.storage.get(key);
    }

    public String getStatus(String key) throws CothorityCommunicationException {
        return "";
    }

    public StorageAdmin getAdmin(Signer admin){
        return null;
    }
}
