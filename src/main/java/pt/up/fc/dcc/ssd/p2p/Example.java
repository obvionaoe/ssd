package pt.up.fc.dcc.ssd.p2p;

import pt.up.fc.dcc.ssd.p2p.grpc.DataType;
import pt.up.fc.dcc.ssd.p2p.node.Id;
import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;
import pt.up.fc.dcc.ssd.p2p.node.NodeType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.lang.Thread.sleep;
import static pt.up.fc.dcc.ssd.p2p.node.Id.idFromData;

public class Example {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("KademliaNode Example:\n");

        // by giving this node the BOOTSTRAP_NODE type we give him a specific ID
        // and connection info that all other nodes know
        KademliaNode bootstrapNode = KademliaNode
            .newBuilder()
            .type(NodeType.BOOTSTRAP_NODE)
            .build();

        // these are all just normal nodes
        KademliaNode node1 = KademliaNode
            .newBuilder()
            .id(Id.idFromBinaryString("0110001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"))
            .build();

        KademliaNode node2 = KademliaNode
            .newBuilder()
            .id(Id.idFromBinaryString("1010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"))
            .build();

        KademliaNode node3 = KademliaNode
            .newBuilder()
            .id(Id.idFromBinaryString("0111101100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"))
            .build();

        System.out.println("bootstrapNode:");
        System.out.println("ID (Hex): " + bootstrapNode.getId());
        System.out.println("ID (Bin): LSB " + bootstrapNode.getId().toBinaryString() + " MSB \n");

        System.out.println("node1:");
        System.out.println("ID (Hex): " + node1.getId());
        System.out.println("ID (Bin): LSB " + node1.getId().toBinaryString() + " MSB \n");

        System.out.println("node2:");
        System.out.println("ID (Hex): " + node2.getId());
        System.out.println("ID (Bin): LSB " + node2.getId().toBinaryString() + " MSB \n");

        System.out.println("node3:");
        System.out.println("ID (Hex): " + node3.getId());
        System.out.println("ID (Bin): LSB " + node3.getId().toBinaryString() + " MSB \n");

        System.out.println("Ping Example:\n");

        // Start the nodes
        bootstrapNode.start();
        System.out.println("bootstrapNode: " + bootstrapNode.getConnectionInfo() + "\n");

        node1.start();
        System.out.println("node1: " + node1.getConnectionInfo() + "\n");

        node2.start();
        System.out.println("node2: " + node2.getConnectionInfo() + "\n");

        node3.start();
        System.out.println("node3: " + node3.getConnectionInfo() + "\n");

        // bootstrap the nodes
        System.out.println("node1 bootstrapped! true == " + node1.bootstrap());
        System.out.println("node1: ping(node2) -> false == " + node1.ping(node2.getId()));
        System.out.println("node2 bootstrapped! true == " + node2.bootstrap());
        System.out.println("node3 bootstrapped! true == " + node3.bootstrap());

        System.out.println();

        System.out.println("node1: ping(node2) -> true == " + node1.ping(node2.getId()));
        System.out.println("node2: ping(node1) -> true == " + node2.ping(node1.getId()));

        System.out.println();

        System.out.println("node2: stopped -> true == " + node2.stop());
        System.out.println();
        System.out.println("Waiting for 30 seconds...");
        sleep(30_000);

        System.out.println();

        System.out.println("node3: ping(node1) -> true == " + node3.ping(node1.getId()));
        System.out.println("node3: ping(node2) -> false == " + node3.ping(node2.getId()));

        System.out.println("\nStore/FindValue Example:\n");

        byte[] data = "345".getBytes(StandardCharsets.UTF_8);
        Id key = idFromData(data);

        System.out.println("node1: store(\"345\") true == " + node1.store(key, data, DataType.TOPIC));
        System.out.println("node1: findValue(idFromData(\"345\")) = " + new String(node1.findValue(key)));
        System.out.println("node3: findValue(idFromData(\"345\")) = " + new String(node3.findValue(key)));
        System.out.println("bootstrapNode: findValue(idFromData(\"345\")) = " + new String(bootstrapNode.findValue(key)));

        bootstrapNode.stop();
        node1.stop();
        node2.stop();
        node3.stop();
    }
}