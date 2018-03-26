package ch.epfl.dedis.lleap;

import ch.epfl.dedis.Local;
import ch.epfl.dedis.lib.ServerIdentity;
import ch.epfl.dedis.lib.SkipBlock;
import ch.epfl.dedis.lib.exception.CothorityException;
import org.junit.jupiter.api.Test;
import javax.xml.bind.DatatypeConverter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSkipBlock {

    @Test
    public void serialize() throws CothorityException {
        byte[] sbHex  = DatatypeConverter.parseHexBinary(Local.genesisHex);
        SkipBlock sb = new SkipBlock(sbHex);
        byte[] sbHex2 = sb.toByteArray();
        assertArrayEquals(sbHex, sbHex2);
    }

    @Test
    public void rosterAgg() throws CothorityException {
        byte[] sbHex  = DatatypeConverter.parseHexBinary(Local.genesisHex);
        SkipBlock sb = new SkipBlock(sbHex);
        assertTrue(sb.getRoster().getAggregate().equals(Local.roster.getAggregate()));
    }
}
