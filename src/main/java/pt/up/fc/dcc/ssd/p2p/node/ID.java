package pt.up.fc.dcc.ssd.p2p.node;

import pt.up.fc.dcc.ssd.p2p.Config;

import javax.xml.bind.DatatypeConverter;
import java.util.BitSet;
import java.util.stream.IntStream;

public class ID extends BitSet {
    /**
     * Generates a random KademliaNode ID
     */
    public ID() {
        super(Config.ID_N_BITS);
        for (int i = 0; i < Config.ID_N_BITS; i++) {
            if (Math.random() > 0.5) {
                this.flip(i);
            }
        }
    }

    public ID(boolean flag) {
        super(8);
        this.set(0, false);
        this.set(1, true);
        this.set(2, false);
        this.set(3, true);
    }

    public ID(int flag) {
        super(8);
        this.set(0, true);
        this.set(1, false);
        this.set(2, true);
        this.set(3, true);
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
        final StringBuilder buffer = new StringBuilder(Config.ID_N_BITS);
        buffer.append("LSB ");
        IntStream.range(0, Config.ID_N_BITS).mapToObj(i -> get(i) ? '1' : '0').forEach(buffer::append);
        buffer.append(" MSB");
        return buffer.toString();
    }

    @Override
    public String toString() {
        return DatatypeConverter.printHexBinary(this.toByteArray());
    }
}
