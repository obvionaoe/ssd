package pt.up.fc.dcc.ssd.p2p.node;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import pt.up.fc.dcc.ssd.p2p.conn.ConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.grpc.*;
import pt.up.fc.dcc.ssd.p2p.grpc.KademliaGrpc.KademliaBlockingStub;
import pt.up.fc.dcc.ssd.p2p.node.util.ValidationUtils;
import pt.up.fc.dcc.ssd.p2p.table.RoutingTable;
import pt.up.fc.dcc.ssd.p2p.table.exceptions.RoutingTableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static pt.up.fc.dcc.ssd.p2p.Config.*;

// methods should all be async
public class KademliaNode {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final ID id;
    private final String address;
    private ConnectionInfo connectionInfo;
    private final Server server;
    public final RoutingTable routingTable;
    private final HashMap<ID, byte[]> repository;

    protected KademliaNode(ID id, String address, int port, ArrayList<KademliaImpl> kademliaImpl) {
        this.id = id;
        ServerBuilder<?> sb = ServerBuilder.forPort(port);
        sb.addService(new Impl());
        kademliaImpl.forEach(sb::addService);
        this.server = sb.build();
        this.address = address;
        this.routingTable = new RoutingTable(id);
        this.repository = new HashMap<>();
        // Add timer for routing table management pings
    }

    public static KademliaNodeBuilder newBuilder() {
        return new KademliaNodeBuilder();
    }

    public void start() throws IOException {
        server.start();
        connectionInfo = new ConnectionInfo(id, address, server.getPort());
    }

    public void stop() throws InterruptedException {
        server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }

    public ID getId() {
        return id;
    }

    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public String getAddress() {
        return connectionInfo.getAddress();
    }

    public int getPort() {
        return connectionInfo.getPort();
    }

    /**
     * Bootstraps this node using the standard address and port for the bootstrap node, which should already be running
     *
     * @return true if the node was successfully bootstrapped, false otherwise
     */
    public boolean bootstrap() {
        return bootstrap(BOOTSTRAP_NODE_ID, BOOTSTRAP_NODE_ADDR, BOOTSTRAP_NODE_PORT);
    }

    /**
     * Bootstraps this node using the provided address and port
     *
     * @param id      an ID object representing the bootstrapper's id
     * @param address a string with the IP or URI compliant name, eg: "localhost", "127.0.0.1", "192.168.2.36"
     * @param port    a port number
     * @return true if the node was successfully bootstrapped, false otherwise
     */
    public boolean bootstrap(ID id, String address, int port) {
        KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(
                ManagedChannelBuilder
                        .forAddress(address, port)
                        .usePlaintext()
                        .build()
        );

        System.out.println(this.id);
        System.out.println(id);
        routingTable.update(id, new ConnectionInfo(id, address, port));

        FindNodeRequest request = FindNodeRequest
                .newBuilder()
                .setDestId(this.id.toBinaryString())
                .setConnectionInfo(connectionInfo.toGrpcConnectionInfo())
                .build();

        FindNodeResponse response = stub.findNode(request);

        return addResultsAndPing(ConnectionInfo.fromGrpcConnectionInfo(response.getConnectionInfoList()));
    }

    public boolean ping(ID id) {
        // the destination node should always be in the routing table,
        // so it will automatically be the closest node as distance(closest, dest) = 0
        // that means it will be the first node in our sorted list of connection information
        try {
            ConnectionInfo connectionInfo = routingTable.findClosest(id).get(0);
            System.out.println(connectionInfo);

            KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(
                    ManagedChannelBuilder
                            .forAddress(connectionInfo.getAddress(), connectionInfo.getPort())
                            .usePlaintext()
                            .build()
            );

            try {
                boolean alive = stub.ping(PingRequest
                        .newBuilder()
                        .setConnectionInfo(this.connectionInfo.toGrpcConnectionInfo())
                        .build()).getAlive();

                if (alive) {
                    routingTable.update(connectionInfo.getId(), connectionInfo);
                } else {
                    routingTable.remove(connectionInfo.getId());
                }

                return alive;
            } catch (StatusRuntimeException e) {
                if (e.getStatus() == Status.UNAVAILABLE) {
                    logger.warning("Node with id " + id + " is not online");
                } else {
                    logger.warning(e.getMessage());
                }

                return false;
            }
        } catch (RoutingTableException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    // WIP
    public boolean findNode(ID id) {
        return false;
    }

    private boolean addResultsAndPing(List<ConnectionInfo> connectionInfos) {
        final boolean[] result = {true};
        connectionInfos.forEach(info -> {
            if (!routingTable.update(info.getId(), info)) {
                result[0] = false;
            }
        });
        connectionInfos.forEach(info -> result[0] = ping(info.getId()));

        return result[0];
    }

    /**
     * Kademlia RPC implementation
     */
    private class Impl extends KademliaGrpc.KademliaImplBase {

        @Override
        public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
            if (!ValidationUtils.validateRequest(request)) {
                responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT));
                return;
            }

            ConnectionInfo originConnectionInfo = ConnectionInfo.fromGrpcConnectionInfo(request.getConnectionInfo());


            if (!routingTable.update(originConnectionInfo.getId(), originConnectionInfo)) {
                responseObserver.onError(new StatusRuntimeException(Status.INTERNAL));
                return;
            }

            responseObserver.onNext(PingResponse
                    .newBuilder()
                    .setAlive(true)
                    .build()
            );
            responseObserver.onCompleted();
        }

        @Override
        public void store(StoreRequest request, StreamObserver<StoreResponse> responseObserver) {
            super.store(request, responseObserver);
        }

        @Override
        public void findNode(FindNodeRequest request, StreamObserver<FindNodeResponse> responseObserver) {
            if (!ValidationUtils.validateRequest(request)) {
                responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT));
                return;
            }

            List<ConnectionInfo> infos;
            try {
                infos = routingTable.findClosest(ID.fromString(request.getDestId()));
            } catch (RoutingTableException e) {
                responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage())));
                return;
            }

            ConnectionInfo originConnectionInfo = ConnectionInfo.fromGrpcConnectionInfo(request.getConnectionInfo());

            routingTable.update(originConnectionInfo.getId(), originConnectionInfo);

            FindNodeResponse.Builder response = FindNodeResponse.newBuilder();

            if (!(infos == null)) {
                response.addAllConnectionInfo(infos.stream().map(ConnectionInfo::toGrpcConnectionInfo).collect(Collectors.toList()));
            }

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }

        @Override
        public void findValue(ValueRequest request, StreamObserver<ValueResponse> responseObserver) {
            super.findValue(request, responseObserver);
        }

        @Override
        public void leave(LeaveRequest request, StreamObserver<LeaveResponse> responseObserver) {
            super.leave(request, responseObserver);
        }

        @Override
        public void gossip(StoreRequest request, StreamObserver<StoreResponse> responseObserver) {
            super.gossip(request, responseObserver);
        }
    }
}