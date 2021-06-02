package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;
import pt.up.fc.dcc.ssd.p2p.node.NodeType;

import javax.net.ssl.SSLException;
import java.io.IOException;

public class Bootstrap {
    public static void main(String[] args) throws IOException {
        KademliaNode bootstrapNode = KademliaNode
                .newBuilder()
                .type(NodeType.BOOTSTRAP_NODE)
                .build();

        bootstrapNode.start();

        while(true){

        }
    }
}
