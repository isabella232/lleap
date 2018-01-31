package ch.epfl.dedis.lib.storage;

import ch.epfl.dedis.lib.darc.Darc;
import ch.epfl.dedis.lib.darc.Signer;
import ch.epfl.dedis.sicpa.Key;
import ch.epfl.dedis.sicpa.Value;
import ch.epfl.dedis.lib.exception.CothorityCommunicationException;

import java.util.List;

/**
 * Skipchain is the storage backend written by the EPFL/DEDIS and implements
 * fork-resistant linked blocks with multiple forward- and backward-links.
 * It uses a set of permissioned nodes that handle the blockchain and create
 * a consensus of the data.
 */
public class Skipchain extends Storage {
    public Skipchain(List<ConfigurationEntry> configuration) throws CothorityCommunicationException{}

    public void setKeyValue(Key key, Value value, Signer writer) throws CothorityCommunicationException{}
    public Value getValue(Key key) throws CothorityCommunicationException{
        return new Value("".getBytes());
    }
    public String getStatus(String key) throws CothorityCommunicationException{
        return "";
    }

}
