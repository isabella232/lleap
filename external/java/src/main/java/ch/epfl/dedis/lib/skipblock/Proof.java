package ch.epfl.dedis.lib.skipblock;

import ch.epfl.dedis.lib.Roster;
import ch.epfl.dedis.lib.exception.CothorityCryptoException;
import ch.epfl.dedis.proto.SkipBlockProto;
import ch.epfl.dedis.proto.SkipchainProto;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Proof holds everything necessary to link a given block to a genesis-block. By
 * following forward-links, we can link any two blocks in a blockchain by
 * verifying the signature of the forwardlink.
 */
public class Proof {
    protected SkipblockId genesisId;
    protected Roster genesisRoster;
    protected List<ForwardLink> forwardLinks;
    protected SkipBlock lastBlock;

    /**
     * Start a proof with a known genesis block.
     */
    public Proof(SkipBlock genesis) throws CothorityCryptoException, URISyntaxException {
        this(genesis.getId(), genesis.getRoster());
    }

    public Proof(SkipblockId id, Roster roster){
        this.genesisId = id;
        this.genesisRoster = roster;
        forwardLinks = new ArrayList<>();
    }

    /**
     * Recreate a proof from a protobuf.
     * @param proto
     */
    public Proof(SkipchainProto.Proof proto) throws CothorityCryptoException, URISyntaxException, IOException{
        this(new SkipblockId(proto.getGenesisId()), new Roster(proto.getGenesisRoster()));
        for (SkipBlockProto.ForwardLink fl: proto.getLinksList()){
            addForwardLink(new ForwardLink(fl));
        }
    }

    /**
     * Adds a forward link to the proof. The given forward link is verified to be a valid
     * link by comparing the 'to' of the last forward link with the 'from' of the given forward
     * link. Furthermore the signature is verified to be a valid signature given the
     * latest valid roster.
     * @param flNew
     */
    public void addForwardLink(ForwardLink flNew) throws URISyntaxException, CothorityCryptoException, IOException{
        if (lastBlock != null){
            throw new CothorityCryptoException("cannot add links after last block");
        }
        Roster latestRoster = genesisRoster;
        for (ForwardLink fl: forwardLinks){
            if (fl.getRoster() != null){
                latestRoster = fl.getRoster();
            }
        }
        ForwardLink latest = forwardLinks.get(forwardLinks.size()-1);
        if (!latest.getTo().equals(flNew.getFrom())){
            throw new CothorityCryptoException("this forward link doesn't fit");
        }
        if (!flNew.verifySig(latestRoster)){
            throw new CothorityCryptoException("signature verification failed on new forwardlink");
        }
        forwardLinks.add(flNew);
    }

    /**
     * This adds a block to the end of the forward-links. It also verifies that all links
     * are correct and that the block does have the correct Id to fit at the end of the
     * forward-links.
     * @param skipBlock the skipblock to store in the proof
     * @throws CothorityCryptoException
     */
    public void addLastBlock(SkipBlock skipBlock) throws CothorityCryptoException{
        if (forwardLinks.size() == 0) {
            throw new CothorityCryptoException("cannot add block without forward-links");
        }
        try {
            SkipblockId latest = verify();
            if (!latest.equals(skipBlock.getHash())){
                throw new CothorityCryptoException("latest forward-link doesn't point to this skipblock");
            }
            lastBlock = skipBlock;
        } catch (URISyntaxException e){
            throw new CothorityCryptoException("something with the forward-links is not correct");
        } catch(IOException e){
            throw new CothorityCryptoException("something with the forward-links is not correct");
        }
    }

    /**
     * Verify makes sure that all forward links are correctly signed and create a chain.
     * @return It will return the Id of the latest block the forward links point to.
     */
    public SkipblockId verify() throws CothorityCryptoException, URISyntaxException, IOException{
        Roster currentRoster = genesisRoster;
        SkipblockId currentId = genesisId;
        for (ForwardLink fl: forwardLinks){
            if (!currentId.equals(fl.getFrom())){
                throw new CothorityCryptoException("forward link is broken");
            }
            if (!fl.verifySig(currentRoster)){
                throw new CothorityCryptoException("forward link has wrong signature");
            }
            currentId = fl.getTo();
            if (fl.getRoster() != null){
                currentRoster = fl.getRoster();
            }
        }
        return currentId;
    }

    /**
     * Creates a protobuf-representation of the data.
     * @return
     */
    public SkipchainProto.Proof toProto() {
        SkipchainProto.Proof.Builder builder = SkipchainProto.Proof.newBuilder();
        builder.setGenesisId(genesisId.toBS());
        builder.setGenesisRoster(genesisRoster.getProto());
        for (ForwardLink fl: forwardLinks){
            builder.addLinks(fl.getProto());
        }
        return builder.build();
    }
}
