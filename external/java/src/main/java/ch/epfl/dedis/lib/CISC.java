package ch.epfl.dedis.lib;

import ch.epfl.dedis.lib.exception.CothorityException;
import ch.epfl.dedis.lleap.KeyValueBlock;
import ch.epfl.dedis.proto.IdentityProto;
import ch.epfl.dedis.proto.SkipBlockProto;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * CISC Identity SkipChain is a definition of how key/value pairs are stored on a skipchain. It can be used for
 * multiple services like storing ssh keys, web pages, or any other key/value pair.
 */
public class CISC {
    private SkipBlock block;
    private IdentityProto.Data data = null;
    private final Logger logger = LoggerFactory.getLogger(CISC.class);


    /**
     * Initialises a CISC with a skipblock.
     * @param block
     */
    public CISC(SkipBlock block) throws InvalidProtocolBufferException, CothorityException {
        this.block = block;
        // Due to an exotic marshalling in the CISC service we need to start decoding at position 16.
        ByteString protobuf = ByteString.copyFrom(block.getData()).substring(16);
        this.data = IdentityProto.Data.parseFrom(protobuf);
        if (this.data == null){
            throw new CothorityException("cisc data is null");
        }
    }

    /**
     * Convenience function to initialise a CISC.
     * @param block
     */
    public CISC(SkipBlockProto.SkipBlock block) throws InvalidProtocolBufferException, CothorityException{
        this(new SkipBlock(block));
    }

    public byte[] getValue(String key) throws CothorityException {
        if (!this.data.getStorageMap().containsKey(key)) {
            return null;
        }
        return this.data.getStorageMap().get(key).toByteArray();
    }

    public byte[] getDeviceKey(String name){
        if (!data.containsDevice("service")){
            return null;
        }
        return data.getDeviceOrThrow("service").getPoint().toByteArray();
    }
}
