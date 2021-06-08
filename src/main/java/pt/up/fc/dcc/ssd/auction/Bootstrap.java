package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;
import pt.up.fc.dcc.ssd.p2p.node.NodeType;

import javax.net.ssl.SSLException;
import java.io.IOException;

import static java.lang.Thread.sleep;

public class Bootstrap {
    public static void main(String[] args) throws IOException, InterruptedException {
        KademliaNode bootstrapNode = KademliaNode
                .newBuilder()
                .type(NodeType.BOOTSTRAP_NODE)
                .build();

        bootstrapNode.start();

        sleep(3_600_000);
    }
}
