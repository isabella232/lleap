package ch.epfl.dedis.sicpa;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestSkipchainRPC {
    private static byte[] key;
    private static byte[] value;
    @BeforeAll
    public void initAll(){
        key = "first".getBytes();
        value = "value".getBytes();
    }

    @Test
    public void connect() throws Exception{
        SkipchainRPC sc = new SkipchainRPC();
        assertTrue(sc.verify());
    }

    @Test
    public void writeRead() throws Exception{
        SkipchainRPC sc = new SkipchainRPC();
        sc.setKeyValue(key, value);
        assertEquals(value, sc.getValue(key));
    }
}
