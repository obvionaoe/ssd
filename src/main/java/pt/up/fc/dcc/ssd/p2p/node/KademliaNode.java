package pt.up.fc.dcc.ssd.p2p.node;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.up.fc.dcc.ssd.p2p.grpc.*;
import pt.up.fc.dcc.ssd.p2p.grpc.KademliaGrpc.KademliaBlockingStub;
import pt.up.fc.dcc.ssd.p2p.rt.ConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.rt.RoutingTable;

import java.io.IOException;
import java.util.stream.Stream;

public class KademliaNode {
    private final ID id;
    private final ConnectionInfo connectionInfo;
    private final Server server;
    private final RoutingTable rt;

    public KademliaNode(ConnectionInfo connectionInfo, KademliaImpl... kademliaImpl) {
        this.id = new ID();
        this.connectionInfo = connectionInfo;
        ServerBuilder<?> sb = ServerBuilder.forPort(connectionInfo.getPort());
        Stream.of(kademliaImpl).forEach(sb::addService);
        this.server = sb.build();
        this.rt = new RoutingTable();
    }

    public void start() throws IOException {
        server.start();
    }

    public ID getId() {
        return id;
    }

    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public void bootstrap() {
        KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(
                ManagedChannelBuilder
                        .forAddress("localhost", 50000)
                        .usePlaintext()
                        .keepAliveWithoutCalls(true)
                        .build()
        );

        BootstrapResponse response = stub.bootstrap(BootstrapRequest.newBuilder().build());
    }

    public boolean ping(ID id) {
        KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(
                ManagedChannelBuilder
                .forAddress("localhost", connectionInfo.getPort())
                .usePlaintext()
                .keepAliveWithoutCalls(true)
                .build()
        );

        return stub.ping(PingRequest.newBuilder().build()).getAlive();
    }
}