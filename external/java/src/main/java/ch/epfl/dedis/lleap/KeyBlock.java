package ch.epfl.dedis.lleap;

import ch.epfl.dedis.lib.crypto.SchnorrSig;
import ch.epfl.dedis.lib.exception.CothorityCryptoException;
import ch.epfl.dedis.lib.skipblock.ForwardLink;
import ch.epfl.dedis.lib.skipblock.SkipBlock;
import ch.epfl.dedis.proto.CiscProto;
import ch.epfl.dedis.proto.SkipBlockProto;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.URISyntaxException;

public class KeyBlock extends SkipBlock {
    ForwardLink forwardLink;
    public KeyBlock(SkipBlockProto.SkipBlock skipBlock, SkipBlockProto.ForwardLink forwardLink) throws CothorityCryptoException, URISyntaxException, IOException {
        super(skipBlock);
        this.forwardLink = new ForwardLink(forwardLink);
    }

    public boolean verifyBlock(SkipBlock genesis){
        genesis.getRoster().getAggregate()
        getHash()
    }

    public byte[] getCiscValue(byte[] key) throws InvalidProtocolBufferException {
        CiscProto.Data data = CiscProto.Data.parseFrom(skipBlock.getData());
        String keyString = new String(key);
        if (data.containsStorage(keyString)) {
            return data.getStorageMap().get(keyString).getBytes();
        }
        return null;
    }

    public byte[] getValue() throws InvalidProtocolBufferException {
        return getCiscValue("newkey".getBytes());
    }

    public byte[] getKey() throws InvalidProtocolBufferException {
        return getCiscValue("newvalue".getBytes());
    }

    public byte[] getTimestamp() throws InvalidProtocolBufferException {
        return getCiscValue("timestamp".getBytes());
    }
}
