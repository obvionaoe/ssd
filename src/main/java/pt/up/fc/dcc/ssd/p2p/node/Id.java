package pt.up.fc.dcc.ssd.p2p.node;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.BitSet;
import java.util.Objects;
import java.util.stream.IntStream;

import static pt.up.fc.dcc.ssd.p2p.common.Config.ID_N_BITS;

public class Id implements Serializable {
    public final long serialVersionUID = 1L;
    private static final SecureRandom sr = new SecureRandom();
    private final BitSet bs;

    /**
     * Generates a random KademliaNode ID
     */
    public Id() {
        byte[] arr = new byte[32];
        sr.nextBytes(arr);

        bs = BitSet.valueOf(getHash(arr));

        /*bs = new BitSet(ID_N_BITS);
        for (int i = 0; i < ID_N_BITS; i++) {
            if (Math.random() > 0.5) {
                bs.flip(i);
            }
        }*/
    }

    private Id(BitSet bs) {
        this.bs = bs;
    }

    /**
     * Creates and Id from the given byte[]
     *
     * @param data a byte[] containing data, either a serialised block or a topic
     * @return an Id built from a SHA-1 hash of the data, null if there was a problem
     */
    public static Id idFromData(byte[] data) {
        return new Id(BitSet.valueOf(getHash(data)));
    }

    /**
     * Creates an Id from a binary string
     *
     * @return an Id that represents the binary string
     */
    public static Id idFromBinaryString(String s) {
        BitSet bs = new BitSet(ID_N_BITS);

        for (int i = 0; i < ID_N_BITS; i++) {
            if (s.charAt(i) == '1')
                bs.set(i);
        }

        return new Id(bs);
    }

    public long toLong() {
        long value = 0L;
        for (int i = 0; i < bs.length(); i++) {
            value += bs.get(i) ? (1L << i) : 0L;
        }
        return value;
    }

    /**
     * Converts the underlying BitSet to a byte[]
     *
     * @return a byte[] representing the underlying BitSet
     */
    public byte[] toBytes() {
        byte[] bytes = new byte[bs.length() / 8 + 1];
        for (int i = 0; i < bs.length(); i++) {
            if (bs.get(i)) {
                bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
            }
        }
        return bytes;
    }

    /**
     * Creates a String representation of the underlying BitSet in binary
     *
     * @return a String that represents the Id in binary
     */
    public String toBinaryString() {
        final StringBuilder buffer = new StringBuilder(ID_N_BITS);
        IntStream.range(0, ID_N_BITS).mapToObj(i -> bs.get(i) ? '1' : '0').forEach(buffer::append);
        return buffer.toString();
    }

    private static byte[] getHash(byte[] data) {
        byte[] hash = null;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            hash = md.digest(data);
        } catch (NoSuchAlgorithmException ignored) {}

        return hash;
    }

    /**
     * Exclusive-OR of the 2 Id's
     *
     * @param id the Id to XOR into this one
     */
    public void xor(Id id) {
        bs.xor(id.bs);
    }

    @Override
    public String toString() {
        return DatatypeConverter.printHexBinary(bs.toByteArray());
    }

    public Id copy() {
        return new Id((BitSet) bs.clone());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Id id = (Id) o;
        return this.bs.equals(id.bs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bs);
    }

    // TESTING
    public byte[] toByteArray() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Id toObject(byte[] arr) {
        ByteArrayInputStream bis = new ByteArrayInputStream(arr);
        try (ObjectInput in = new ObjectInputStream(bis)) {
            return (Id) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
