package pt.up.fc.dcc.ssd.p2p.node;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.up.fc.dcc.ssd.p2p.grpc.KademliaGrpc;
import pt.up.fc.dcc.ssd.p2p.grpc.KademliaGrpc.KademliaBlockingStub;
import pt.up.fc.dcc.ssd.p2p.grpc.PingRequest;
import pt.up.fc.dcc.ssd.p2p.grpc.PingResponse;
import pt.up.fc.dcc.ssd.p2p.grpc.KademliaImpl;
import pt.up.fc.dcc.ssd.p2p.rt.ConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.rt.RoutingTable;

import java.io.IOException;
import java.util.BitSet;
import java.util.stream.Stream;

public class KademliaNode {
    private final ID id;
    private ConnectionInfo connectionInfo;
    private final Server server;
    private final int port;
    private final RoutingTable rt;

    public KademliaNode(int port, KademliaImpl... kademliaImpl) {
        this.id = new ID();
        this.port = port;
        this.rt = new RoutingTable();
        ServerBuilder<?> sb = ServerBuilder.forPort(port);
        Stream.of(kademliaImpl).forEach(sb::addService);
        this.server = sb.build();
    }

    public void start() throws IOException {

        server.start();
    }

    public void bootstrap() {

    }

    public PingResponse ping(ID id) {
        Channel channel = ManagedChannelBuilder
                .forAddress("localhost", port)
                .usePlaintext()
                .keepAliveWithoutCalls(true)
                .build();

        KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        return stub.ping(PingRequest.newBuilder().build());
    }
}