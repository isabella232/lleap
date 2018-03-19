package ch.epfl.dedis.lib.skipblock;

import ch.epfl.dedis.lib.Roster;
import ch.epfl.dedis.lib.UUIDType5;
import ch.epfl.dedis.lib.exception.CothorityCryptoException;
import ch.epfl.dedis.proto.DarcProto;
import ch.epfl.dedis.proto.SkipBlockProto;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

public class ForwardLink {
    private SkipBlockProto.ForwardLink forwardLink;

    public ForwardLink(SkipBlockProto.ForwardLink forwardLink){
        this.forwardLink = forwardLink;
    }

    public ForwardLink(SkipblockId from, SkipblockId to, Roster newRoster, SignatureCoSi signature){
        SkipBlockProto.ForwardLink.Builder fbBuilder = SkipBlockProto.ForwardLink.newBuilder();
        if (from != null) {
            fbBuilder.setFrom(from.toBS());
        }
        if (to != null) {
            fbBuilder.setTo(to.toBS());
        }
        if (newRoster != null) {
            fbBuilder.setNewRoster(newRoster.getProto());
        }
        if (signature != null) {
            fbBuilder.setFinalSignature(signature.getProto());
        }
    }

    public SkipBlockProto.ForwardLink getProto(){
        return forwardLink;
    }

    public SkipblockId getFrom() throws CothorityCryptoException{
        return new SkipblockId(forwardLink.getFrom());
    }

    public SkipblockId getTo() throws CothorityCryptoException{
        return new SkipblockId(forwardLink.getTo());
    }

    public Roster getRoster() throws CothorityCryptoException, URISyntaxException{
        if (!forwardLink.hasNewRoster()){
            return null;
        }
        return new Roster(forwardLink.getNewRoster());
    }

    public SignatureCoSi getSignature(){
        return new SignatureCoSi(forwardLink.getFinalSignature());
    }

    public boolean verifySig(Roster r) throws CothorityCryptoException, IOException, URISyntaxException{
        SignatureCoSi sig = getSignature();
        ByteArrayOutputStream msg = new ByteArrayOutputStream( );
        msg.write(getFrom().getId());
        msg.write(getTo().getId());
        if (forwardLink.hasNewRoster()){
            msg.write(UUIDType5.toBytes(getRoster().getId()));
        }

        return sig.verifySig(r, msg.toByteArray());
    }
}
