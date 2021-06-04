package pt.up.fc.dcc.ssd;

import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;
import pt.up.fc.dcc.ssd.p2p.node.NodeType;

import javax.net.ssl.SSLException;

import static java.lang.Thread.sleep;

public class RunBootstrapper {
    public static void main(String[] args) throws SSLException, InterruptedException {
        KademliaNode bootstrapNode = KademliaNode.newBuilder().type(NodeType.BOOTSTRAP_NODE).build();
        sleep(3_600_000);
    }
}
