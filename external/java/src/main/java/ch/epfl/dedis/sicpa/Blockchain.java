package ch.epfl.dedis.sicpa;

import ch.epfl.dedis.lib.exception.CothorityCommunicationException;
import ch.epfl.dedis.lib.storage.Storage;

/**
 * Blockchain offers a reliable, fork-resistant storage of key/value pairs, backed
 * by different blockchain-types.
 */
public class Blockchain {
    Storage storage;

    /**
     * Initializes a Storage adapter and tries to contact it.
     * @param storage
     * @throws CothorityCommunicationException
     */
    public Blockchain(Storage storage) throws CothorityCommunicationException{
        this.storage = storage;
        storage.getStatus("ping");
    }
}
