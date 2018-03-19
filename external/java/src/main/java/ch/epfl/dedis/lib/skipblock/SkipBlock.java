package ch.epfl.dedis.lib.skipblock;

import ch.epfl.dedis.lib.exception.CothorityCryptoException;
import ch.epfl.dedis.proto.SkipBlockProto;
import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.List;

public class SkipBlock extends SkipBlockHeader {
    protected SkipBlockProto.SkipBlock skipBlock;

    public SkipBlock(SkipBlockProto.SkipBlock skipBlock) {
        super(skipBlock);
        this.skipBlock = skipBlock;
    }

    public byte[] getHash() {
        return skipBlock.getHash().toByteArray();
    }

    public SkipblockId getId() throws CothorityCryptoException{
        return new SkipblockId(getHash());
    }

    public List<ForwardLink> getForwardLinks() {
        List<ForwardLink> fls = new ArrayList<>();
        for (SkipBlockProto.ForwardLink fl : skipBlock.getForwardList()) {
            fls.add(new ForwardLink(fl));
        }
        return fls;
    }

    public List<SkipblockId> getChildren() throws CothorityCryptoException{
        List<SkipblockId> children = new ArrayList<>();
        for (ByteString child : skipBlock.getChildrenList()) {
            children.add(new SkipblockId(child.toByteArray()));
        }
        return children;
    }
}
