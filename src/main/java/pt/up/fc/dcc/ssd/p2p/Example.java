package pt.up.fc.dcc.ssd.p2p;

import io.grpc.stub.StreamObserver;
import pt.up.fc.dcc.ssd.p2p.grpc.KademliaImpl;
import pt.up.fc.dcc.ssd.p2p.grpc.PingRequest;
import pt.up.fc.dcc.ssd.p2p.grpc.PingResponse;
import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;
import pt.up.fc.dcc.ssd.p2p.rt.ConnectionInfo;

import java.io.IOException;

public class Example {
    public static void main(String[] args) throws IOException {
        System.out.println("Node Example:\n");

        KademliaNode node1 = new KademliaNode(new ConnectionInfo("localhost", 50001), new Impl());
        KademliaNode node2 = new KademliaNode(new ConnectionInfo("localhost", 50002), new Impl());

        System.out.println("Node #1:");
        System.out.println("ID (Hex): " + node1.getId());
        System.out.println("ID (Bin): " + node1.getId().toBinaryString() + "\n");

        System.out.println("Node #2:");
        System.out.println("ID (Hex): " + node2.getId());
        System.out.println("ID (Bin): " + node2.getId().toBinaryString() + "\n");

        System.out.println("Ping Example:\n");

        node1.start();
        System.out.println("Start node1: node1.start()\n");
        node2.start();
        System.out.println("Start node2: node2.start()\n");

        System.out.println("Is node2 alive? " + node1.ping(node2.getId()));
        System.out.println("Is node2 alive? " + node2.ping(node1.getId()));
    }
}


class Impl extends KademliaImpl {
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        responseObserver.onNext(PingResponse.newBuilder().setAlive(true).build());
        responseObserver.onCompleted();
    }
}