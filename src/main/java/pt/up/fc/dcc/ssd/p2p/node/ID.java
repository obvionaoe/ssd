package pt.up.fc.dcc.ssd.p2p.node;

import javax.annotation.Nonnull;
import javax.xml.bind.DatatypeConverter;
import java.util.BitSet;
import java.util.stream.IntStream;

import static pt.up.fc.dcc.ssd.p2p.common.Config.ID_N_BITS;

public class ID extends BitSet {
    /**
     * Generates a random KademliaNode ID
     */
    // TODO: make ID from hash function
    public ID() {
        super(ID_N_BITS);
        for (int i = 0; i < ID_N_BITS; i++) {
            if (Math.random() > 0.5) {
                this.flip(i);
            }
        }
    }

    private ID(String s) {
        super(ID_N_BITS);

        for (int i = 0; i < ID_N_BITS; i++) {
            if (s.charAt(i) == '1')
                this.set(i);
        }
    }



    public static ID fromBinaryString(String s) {
        return new ID(s);
    }

    public long toLong() {
        long value = 0L;
        for (int i = 0; i < this.length(); i++) {
            value += this.get(i) ? (1L << i) : 0L;
        }
        return value;
    }

    @Override
    public Object clone() {
        return super.clone();
    }

    public String toBinaryString() {
        final StringBuilder buffer = new StringBuilder(ID_N_BITS);
        IntStream.range(0, ID_N_BITS).mapToObj(i -> get(i) ? '1' : '0').forEach(buffer::append);
        return buffer.toString();
    }

    @Override
    public String toString() {
        return DatatypeConverter.printHexBinary(super.toByteArray());
    }

    @Nonnull
    @Override
    public byte[] toByteArray() {
        byte[] bytes = new byte[this.length() / 8 + 1];
        for (int i = 0; i < this.length(); i++) {
            if (this.get(i)) {
                bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
            }
        }
        return bytes;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
