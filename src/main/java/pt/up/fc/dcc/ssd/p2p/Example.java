package pt.up.fc.dcc.ssd.p2p;

import io.grpc.stub.StreamObserver;
import pt.up.fc.dcc.ssd.p2p.grpc.KademliaImpl;
import pt.up.fc.dcc.ssd.p2p.grpc.PingRequest;
import pt.up.fc.dcc.ssd.p2p.grpc.PingResponse;
import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;
import pt.up.fc.dcc.ssd.p2p.rt.RoutingTable;

import java.io.IOException;

public class Example {
    public static void main(String[] args) throws IOException {
        KademliaNode node1 = new KademliaNode(50001, new Impl());
        KademliaNode node2 = new KademliaNode(50002, new Impl());

        node1.start();
        node2.start();

        /*System.out.println("Is node2 alive? " + node1.ping(50002));
        System.out.println("Is node2 alive? " + node2.ping(50001));*/
    }
}


class Impl extends KademliaImpl {
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        responseObserver.onNext(PingResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}