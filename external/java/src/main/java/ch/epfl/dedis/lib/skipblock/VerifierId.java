package ch.epfl.dedis.lib.skipblock;

import ch.epfl.dedis.lib.UUIDType5;
import ch.epfl.dedis.lib.exception.CothorityCryptoException;

import java.util.UUID;

/**
 * This class represents a SkipblockId, which is a sha256-hash of
 * the static fields of the skipblock.
 */
public class VerifierId {
    private UUID uuid;
    public VerifierId(byte[] id) throws CothorityCryptoException {
        uuid = UUIDType5.fromBytes(id);
    }
}
