package ch.epfl.dedis.lib.skipblock;

import ch.epfl.dedis.lib.Roster;
import ch.epfl.dedis.lib.exception.CothorityCryptoException;
import ch.epfl.dedis.proto.SkipBlockProto;
import com.google.protobuf.ByteString;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class SkipBlockHeader {
    private SkipBlockProto.SkipBlock skipBlock;

    public SkipBlockHeader(SkipBlockProto.SkipBlock skipBlock){
        this.skipBlock = skipBlock;
    }

    // Index of the block in the chain. Index == 0 -> genesis-block.
    public int getIndex() {
        return skipBlock.getIndex();
    }

    // Height of that SkipBlock, starts at 1.
    public int getHeight() {
        return skipBlock.getHeight();
    }

    // The max height determines the height of the next block
    // For deterministic SkipChains, chose a value >= 1 - higher
    // bases mean more 'height = 1' SkipBlocks
    // For random SkipChains, chose a value of 0
    public int getMaximumHeight() {
        return skipBlock.getMaxHeight();
    }

    public int getBaseHeight() {
        return skipBlock.getBaseHeight();
    }

    // BackLink is a slice of hashes to previous SkipBlocks
    public List<SkipblockId> getBackLinkIDs() throws CothorityCryptoException{
        List<SkipblockId> bls = new ArrayList<>();
        for (ByteString bs: skipBlock.getBacklinksList()){
            bls.add(new SkipblockId(bs.toByteArray()));
        }
        return bls;
    }

    // VerifierID is a SkipBlock-protocol verifying new SkipBlocks
    public List<VerifierId> getVerifierIDs() throws CothorityCryptoException{
        List<VerifierId> vids = new ArrayList<>();
        for (ByteString id: skipBlock.getVerifiersList()){
            vids.add(new VerifierId(id.toByteArray()));
        }
        return vids;
    }

    // SkipBlockParent points to the SkipBlock of the responsible Roster -
    // is nil if this is the Root-roster
    public SkipblockId getParentBlockID() throws CothorityCryptoException{
        return new SkipblockId(skipBlock.getParent().toByteArray());
    }

    // GenesisID is the ID of the genesis-block. For the genesis-block, this
    // is null. The SkipBlockID() method returns the correct ID both for
    // the genesis block and for later blocks.
    public SkipblockId getGenesisID() throws CothorityCryptoException{
        if (getIndex() == 0){
            return new SkipblockId(skipBlock.getHash().toByteArray());
        } else {
            return new SkipblockId(skipBlock.getGenesis().toByteArray());
        }
    }

    // Data is any data to be stored in that SkipBlock
    public byte[] getData() {
        return skipBlock.getData().toByteArray();
    }

    // Roster holds the roster-definition of that SkipBlock
    public Roster getRoster() throws URISyntaxException{
        return new Roster(skipBlock.getRoster());
    }
}
