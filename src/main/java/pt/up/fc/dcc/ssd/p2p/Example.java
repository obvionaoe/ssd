package pt.up.fc.dcc.ssd.p2p;

import pt.up.fc.dcc.ssd.p2p.node.ID;
import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;
import pt.up.fc.dcc.ssd.p2p.node.exceptions.KademliaNodeBuilderException;
import pt.up.fc.dcc.ssd.p2p.node.util.NodeType;

import java.io.IOException;

import static java.lang.Thread.sleep;

public class Example {
    public static void main(String[] args) throws IOException, KademliaNodeBuilderException, InterruptedException {
        System.out.println("KademliaNode Example:\n");

        // by giving this node the BOOTSTRAP_NODE type we give him a specific ID
        // and connection info that all other nodes know
        KademliaNode bootstrapNode = KademliaNode
                .newBuilder()
                .type(NodeType.BOOTSTRAP_NODE)
                .build();

        // these are all just normal
        KademliaNode node1 = KademliaNode
                .newBuilder()
                .id(ID.fromString("01100010"))
                .build();
        KademliaNode node2 = KademliaNode
                .newBuilder()
                .id(ID.fromString("10100000"))
                .build();
        KademliaNode node3 = KademliaNode
                .newBuilder()
                .id(ID.fromString("01111011"))
                .build();


        System.out.println("Bootstrap Node:");
        System.out.println("ID (Hex): " + bootstrapNode.getId());
        System.out.println("ID (Bin): LSB " + bootstrapNode.getId().toBinaryString() + " MSB \n");

        System.out.println("Node #1:");
        System.out.println("ID (Hex): " + node1.getId());
        System.out.println("ID (Bin): LSB " + node1.getId().toBinaryString() + " MSB \n");

        System.out.println("Node #2:");
        System.out.println("ID (Hex): " + node2.getId());
        System.out.println("ID (Bin): LSB " + node2.getId().toBinaryString() + " MSB \n");

        System.out.println("Node #3:");
        System.out.println("ID (Hex): " + node3.getId());
        System.out.println("ID (Bin): LSB " + node3.getId().toBinaryString() + " MSB \n");

        System.out.println("Ping Example:\n");

        // Start the nodes
        bootstrapNode.start();
        System.out.println("Bootstrap Node - ConnInfo: " + bootstrapNode.getConnectionInfo() + "\n");

        node1.start();
        System.out.println("Node1 - ConnInfo: " + node1.getConnectionInfo() + "\n");

        node2.start();
        System.out.println("Node2 - ConnInfo: " + node2.getConnectionInfo() + "\n");

        node3.start();
        System.out.println("Node3 - ConnInfo: " + node3.getConnectionInfo() + "\n");

        // bootstrap the nodes
        System.out.println("Node1 bootstrapped? " + node1.bootstrap());
        System.out.println("Node2 bootstrapped? " + node2.bootstrap());
        System.out.println("Node3 bootstrapped? " + node3.bootstrap());

        System.out.println("Node2: Is node1 alive? " + node2.ping(node1.getId()));
        System.out.println("Node1: Is node2 alive? " + node1.ping(node2.getId()));

        node2.stop();
        System.out.println("Node2 stopped");
        sleep(30_000);

        System.out.println("Node3: Is node1 alive? " + node3.ping(node1.getId()));
        System.out.println("Node3: Is node2 alive (should return false)? " + node3.ping(node2.getId()));
    }
}