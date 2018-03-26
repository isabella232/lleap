package ch.epfl.dedis.lib;

import ch.epfl.dedis.lib.exception.CothorityException;
import ch.epfl.dedis.proto.SkipBlockProto;
import com.google.protobuf.InvalidProtocolBufferException;

import java.net.URISyntaxException;

public class SkipBlock{
    private SkipBlockProto.SkipBlock skipBlock;

    public SkipBlock(SkipBlockProto.SkipBlock skipBlock) {
        this.skipBlock = skipBlock;
    }

    public SkipBlock(byte[] sb) throws CothorityException {
        try {
            this.skipBlock = SkipBlockProto.SkipBlock.parseFrom(sb);
        } catch (InvalidProtocolBufferException e) {
            throw new CothorityException(e);
        }
    }

    public SkipBlockProto.SkipBlock getProto(){
        return skipBlock;
    }

    public byte[] toByteArray() {
        return this.skipBlock.toByteArray();
    }

    public byte[] getHash() {
        return skipBlock.getHash().toByteArray();
    }

    public byte[] getId() {
        return this.getHash();
    }

    // Roster holds the roster-definition of that SkipBlock
    public Roster getRoster() throws CothorityException {
        try {
            return new Roster(skipBlock.getRoster());
        } catch (URISyntaxException e) {
            throw new CothorityException(e);
        }
    }
}
