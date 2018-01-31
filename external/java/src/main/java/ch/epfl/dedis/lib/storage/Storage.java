package ch.epfl.dedis.lib.storage;

import ch.epfl.dedis.lib.darc.Darc;
import ch.epfl.dedis.lib.darc.Signer;
import ch.epfl.dedis.sicpa.Key;
import ch.epfl.dedis.sicpa.Value;
import ch.epfl.dedis.lib.Roster;
import ch.epfl.dedis.lib.exception.CothorityCommunicationException;

/**
 * Storage represents a way to store data protecting against equivocation attacks.
 * A Storage has to be initialised by an administrator before it can be used. Depending
 * on the implementation of the Storage, different configuration settings need to be
 * provided to initialise the storage.
 * Once a storage is initialised, it can be used by providing the StorageId returned
 * from the initialisation.
 * The roster represents one or more remote nodes that can be used to interact with
 * the storage.
 */
public abstract class Storage {
    public abstract void setKeyValue(Key key, Value value, Signer writer) throws CothorityCommunicationException;

    public abstract Value getValue(Key key) throws CothorityCommunicationException;

    public abstract String getStatus(String key) throws CothorityCommunicationException;

    public abstract StorageAdmin getAdmin(Signer admin);
}
