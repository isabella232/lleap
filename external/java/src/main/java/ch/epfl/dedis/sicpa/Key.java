package ch.epfl.dedis.sicpa;

import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;

/**
 * This implementation is immutable and can be used as key for collections
 */
public class Key {
    private final byte[] id;
    public final static int length = 32;

    public Key(byte[] id){
        this.id = Arrays.copyOf(id, id.length);
    }

    public byte[] getId() {
        return Arrays.copyOf(id, id.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return Arrays.equals(id, ((Key) o).id);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(id);
    }

    @Override
    public String toString(){
        return DatatypeConverter.printHexBinary(id);
    }
}
