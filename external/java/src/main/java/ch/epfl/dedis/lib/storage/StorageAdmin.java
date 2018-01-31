package ch.epfl.dedis.lib.storage;

import ch.epfl.dedis.lib.darc.*;
import ch.epfl.dedis.lib.exception.CothorityCommunicationException;
import ch.epfl.dedis.lib.exception.CothorityCryptoException;

import java.util.Arrays;

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
public abstract class StorageAdmin {
    protected Darc admins;
    protected Darc writers;

    /**
     * Initialises the two darcs, so that the admin-darc and the writers-darc
     * are modifiable by the admin.
     *
     * @param admin is allowed to update admins- and writers-darc
     * @param writer is allowed to add new
     * @throws CothorityCryptoException
     */
    public StorageAdmin(Identity admin, Identity writer) throws CothorityCryptoException{
        admins = new Darc(Arrays.asList(admin), Arrays.asList(admin), null);
        writers = new Darc(Arrays.asList(IdentityFactory.New(admins)), Arrays.asList(writer), null);
    }

    public abstract void updateConfiguration(String key, String value) throws CothorityCommunicationException;

    public abstract String getConfiguration(String key) throws CothorityCommunicationException;

    public abstract String getStatus(String key) throws CothorityCommunicationException;

    public abstract StorageId getStorageId();

    public abstract void updateDarc(Darc newdarc) throws CothorityCommunicationException;

    public abstract Storage getStorage() throws CothorityCommunicationException;

    public void addAdmin(Identity newAdmin, Signer admin) throws CothorityCommunicationException {
        try {
            Darc newAdmins = admins.copy();
            newAdmins.addUser(newAdmin);
            newAdmins.addOwner(newAdmin);
            SignaturePath path = new SignaturePath(admins, admin, SignaturePath.OWNER);
            newAdmins.setEvolution(admins, path, admin);
            updateDarc(newAdmins);
        } catch (CothorityCryptoException e) {
            throw new CothorityCommunicationException(e.getMessage());
        }
    }

    public void removeAdmin(Identity newAdmin, Signer admin) throws CothorityCommunicationException {
        try {
            Darc newAdmins = admins.copy();
            newAdmins.removeUser(newAdmin);
            newAdmins.removeOwner(newAdmin);
            SignaturePath path = new SignaturePath(admins, admin, SignaturePath.OWNER);
            newAdmins.setEvolution(admins, path, admin);
            updateDarc(newAdmins);
        } catch (CothorityCryptoException e) {
            throw new CothorityCommunicationException(e.getMessage());
        }
    }

    public void addWriter(Identity newWriter, Signer admin) throws CothorityCommunicationException {
        try {
            Darc newWriters = writers.copy();
            newWriters.addUser(newWriter);
            SignaturePath path = new SignaturePath(writers, admin, SignaturePath.OWNER);
            newWriters.setEvolution(writers, path, admin);
            updateDarc(newWriters);
        } catch (CothorityCryptoException e) {
            throw new CothorityCommunicationException(e.getMessage());
        }
    }

    public void removeWriter(Identity newWriter, Signer admin) throws CothorityCommunicationException {
        try {
            Darc newWriters = writers.copy();
            newWriters.removeUser(newWriter);
            SignaturePath path = new SignaturePath(writers, admin, SignaturePath.OWNER);
            newWriters.setEvolution(writers, path, admin);
            updateDarc(newWriters);
        } catch (CothorityCryptoException e) {
            throw new CothorityCommunicationException(e.getMessage());
        }
    }
}
