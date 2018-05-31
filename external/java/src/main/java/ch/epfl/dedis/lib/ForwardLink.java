package ch.epfl.dedis.lib;

import ch.epfl.dedis.lib.crypto.SchnorrSig;
import ch.epfl.dedis.lib.exception.CothorityCryptoException;
import ch.epfl.dedis.proto.SkipBlockProto;

import java.net.URISyntaxException;

/**
 * ForwardLink is the proof that two blocks are part of the same skipchain. It contains
 * the id of the source block (from), the id of the destination block (to), and a signature by the
 * roster of the source block. If the roster changed between from and to, NewRoster will be
 * set to the roster of to, else NewRoster will be null.
 */
public class ForwardLink {
    private SkipBlockProto.ForwardLink forward;

    public ForwardLink(SkipBlockProto.ForwardLink fl) {
        forward = fl;
    }

    /**
     * getFrom returns the source-id stored in the forwardlink.
     * @return SkipblockId pointing to the source block.
     * @throws CothorityCryptoException
     */
    public SkipblockId getFrom() throws CothorityCryptoException {
        return new SkipblockId(forward.getFrom().toByteArray());
    }

    /**
     * getTo returns the destination-id stored in the forwardlink.
     * @return SkipblockId pointing to the destination block.
     * @throws CothorityCryptoException
     */
    public SkipblockId getTo() throws CothorityCryptoException {
        return new SkipblockId(forward.getTo().toByteArray());
    }

    /**
     * getRoster returns the new roster stored in the forwardlink. If the
     * source and destination block are the same, getRoster returns null.
     * @return SkipblockId pointing to the source block.
     * @throws CothorityCryptoException
     */
    public Roster getRoster() throws URISyntaxException {
        return new Roster(forward.getNewRoster());
    }

    /**
     * getSignature returns the signature stored in the forwardlink. This
     * signature can be used to verify the forwardlink is valid.
     * @return SkipblockId pointing to the source block.
     * @throws CothorityCryptoException
     */
    public SchnorrSig getSignature() {
        return new SchnorrSig(forward.getSignature().getSig().toByteArray());
    }
}
