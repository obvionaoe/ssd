package pt.up.fc.dcc.ssd.p2p;

import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;
import pt.up.fc.dcc.ssd.p2p.node.NodeType;

import java.io.IOException;

import static java.lang.Thread.sleep;

public class Bootstrap {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("NODE[STATUS]: INITIALIZING...");
        KademliaNode bootstrapNode = KademliaNode
            .newBuilder()
            .type(NodeType.BOOTSTRAP_NODE)
            .build();

        System.out.println("NODE[STATUS]: RUNNING...");
        bootstrapNode.start();

        sleep(3_600_000);

    }

}
