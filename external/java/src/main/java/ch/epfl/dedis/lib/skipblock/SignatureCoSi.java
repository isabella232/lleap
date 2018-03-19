package ch.epfl.dedis.lib.skipblock;


import ch.epfl.dedis.lib.Roster;
import ch.epfl.dedis.proto.SkipBlockProto;

/**
 * SignatureCoSi holds a collective schnorr signature from a list of nodes.
 * To verify the signature, one has to extract the map of exceptions from the
 * signature and then remove the missing node from the aggregate public key.
 * Only then can the schnorr signature be verified.
 */
public class SignatureCoSi {
    SkipBlockProto.FinalSignature finalSignature;
    public SignatureCoSi(SkipBlockProto.FinalSignature finalSignature){
        this.finalSignature = finalSignature;
    }

    public byte[] getMsg(){
        return finalSignature.getMsg().toByteArray();
    }

    public byte[] getSig(){
        return finalSignature.getSig().toByteArray();
    }

    public boolean verifySig(Roster r){
        return verifySig(r, getMsg());
    }

    public boolean verifySig(Roster r, byte[] msg){
        return false;
    }

    public SkipBlockProto.FinalSignature getProto(){
        return null;
    }
}
