package pt.up.fc.dcc.ssd.p2p.common;

import pt.up.fc.dcc.ssd.p2p.node.ID;

import java.math.BigInteger;

// CONSTANTS
public class Config {

    // B
    public static final int ID_N_BITS = 8;
    public static final BigInteger MAX_DISTANCE = new BigInteger(
            "1461501637330902918203684832716283019655932542976"
    );

    // K
    public static final int MAX_BUCKET_SIZE = 20;

    // BOOTSTRAP
    public static final ID BOOTSTRAP_NODE_ID = ID.fromBinaryString(
            "1100001000000110000110000000110000010001100111000010110101110001011101000111111011100110111000001010001010101010111000110000100101000011000001001100010100110001"
    );
    public static final String BOOTSTRAP_NODE_ADDR = "localhost";
    public static final int BOOTSTRAP_NODE_PORT = 50001;
}
