package ch.epfl.dedis.lleap;

import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;

/**
 * This implementation is immutable and can be used as value for collections
 */
public class Value {
    private final byte[] id;

    public Value(byte[] id){
        this.id = Arrays.copyOf(id, id.length);
    }

    public byte[] getId() {
        return Arrays.copyOf(id, id.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return Arrays.equals(id, ((Value) o).id);
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
