package ch.epfl.dedis.lib.storage;

import ch.epfl.dedis.lib.Sha256id;
import ch.epfl.dedis.lib.exception.CothorityCryptoException;

/**
 * This class represents a StorageId, which is a sha256-hash of
 * the storage generated.
 */
public class StorageId extends Sha256id {
    public StorageId(byte[] id) throws CothorityCryptoException{
        super(id);
    }
}
