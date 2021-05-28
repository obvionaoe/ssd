package pt.up.fc.dcc.ssd.p2p;

import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.math.BigInteger;

import static pt.up.fc.dcc.ssd.p2p.node.Id.idFromBinaryString;

public class Config {
    // CONSTANTS
    // B
    public static final int ID_N_BITS = 256;
    public static final BigInteger MAX_DISTANCE = new BigInteger("115792089237316195423570985008687907853269984665640564039457584007913129639936");

    // K
    public static final int MAX_BUCKET_SIZE = 20;

    // BOOTSTRAP
    public static final Id BOOTSTRAP_NODE_ID = idFromBinaryString(
            "1100001000000110000110000000110000010001100111000010110101110001011101000111111011100110111000001010001010101010111000110000100101000011000001001100010100110001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
    );
    public static final String BOOTSTRAP_NODE_ADDR = "localhost";
    public static final int BOOTSTRAP_NODE_PORT = 50001;
}
