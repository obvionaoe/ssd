package pt.up.fc.dcc.ssd.p2p.node;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import pt.up.fc.dcc.ssd.p2p.KademliaGrpc;
import pt.up.fc.dcc.ssd.p2p.KademliaGrpc.KademliaBlockingStub;
import pt.up.fc.dcc.ssd.p2p.PingRequest;
import pt.up.fc.dcc.ssd.p2p.PingResponse;
import pt.up.fc.dcc.ssd.p2p.grpc.KademliaImpl;

import java.io.IOException;

public class KademliaNode {
    private Server server;
    private int port;

    public KademliaNode(int port, KademliaImpl kademliaImpl) {
        this.port = port;
        this.server = ServerBuilder.forPort(port)
                .addService(kademliaImpl)
                .build();
    }

    public void start() throws IOException {

        server.start();
    }

    public boolean ping(int port) {
        Channel channel = ManagedChannelBuilder
                .forAddress("localhost", port)
                .usePlaintext()
                .keepAliveWithoutCalls(true)
                .build();

        KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        return stub.ping(PingRequest.newBuilder().build()) != null;
    }

    public static void main(String[] args) throws IOException {
        KademliaNode node1 = new KademliaNode(50001, new Impl());
        KademliaNode node2 = new KademliaNode(50002, new Impl());

        node1.start();
        node2.start();

        System.out.println("Is node2 alive? " + node1.ping(50002));
        System.out.println("Is node2 alive? " + node2.ping(50001));
    }
}

public class Impl extends KademliaImpl {
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        responseObserver.onNext(PingResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}