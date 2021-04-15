package pt.up.fc.dcc.ssd.p2p.node;

import com.google.rpc.Code;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import pt.up.fc.dcc.ssd.p2p.conn.ConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.conn.DistancedConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.grpc.*;
import pt.up.fc.dcc.ssd.p2p.grpc.KademliaGrpc.KademliaBlockingStub;
import pt.up.fc.dcc.ssd.p2p.node.util.NodeType;
import pt.up.fc.dcc.ssd.p2p.node.util.ValidationUtils;
import pt.up.fc.dcc.ssd.p2p.table.RoutingTable;
import pt.up.fc.dcc.ssd.p2p.table.exceptions.RoutingTableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static Builder newBuilder() {
        return new Builder();
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
     * @param id      an ID object representing the bootstrap node's id
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

        routingTable.update(id, new ConnectionInfo(id, address, port));

        FindNodeRequest request = FindNodeRequest
                .newBuilder()
                .setDestId(this.id.toBinaryString())
                .setConnectionInfo(connectionInfo.toDistancedConnectionInfo().toGrpcConnectionInfo())
                .build();

        FindNodeResponse response = stub.findNode(request);

        return addResultsAndPing(DistancedConnectionInfo.fromGrpcConnectionInfo(response.getConnectionInfosList()));
    }

    public boolean ping(ID id) {
        // the destination node should always be in the routing table,
        // so it will automatically be the closest node as distance(closest, dest) = 0
        // that means it will be the first node in our sorted list of connection information
        try {
            // TODO: catch exception if the routing table is empty + findNode if it is missing
            DistancedConnectionInfo infoClosest = routingTable.findClosest(id).get(0);

            KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(
                    ManagedChannelBuilder
                            .forAddress(infoClosest.getAddress(), infoClosest.getPort())
                            .usePlaintext()
                            .build()
            );

            try {
                boolean alive = stub.ping(PingRequest
                        .newBuilder()
                        .setConnectionInfo(this.connectionInfo.toDistancedConnectionInfo().toGrpcConnectionInfo())
                        .build()).getAlive();

                if (alive) {
                    routingTable.update(infoClosest.getId(), infoClosest.getConnectionInfo());
                } else {
                    routingTable.remove(infoClosest.getId());
                }

                return alive;
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode().toString().equals(Code.UNAVAILABLE.toString())) {
                    logger.warning("Node with id " + id + " is not online");
                } else {
                    logger.warning(e.getMessage() + e.getStatus().getCode());
                }

                return false;
            }
        } catch (RoutingTableException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    private boolean addResultsAndPing(List<DistancedConnectionInfo> connectionInfos) {
        final boolean[] result = {true};
        connectionInfos.forEach(info -> {
            if (!routingTable.update(info.getId(), info.getConnectionInfo())) {
                result[0] = false;
            }
        });
        connectionInfos.forEach(info -> result[0] = ping(info.getId()));

        return result[0];
    }

    // WIP

    /**
     * This function calls the FIND_NODE RPC in each of the nodes this node has in it's routing table
     * (the max. number of nodes will be MAX_BUCKET_SIZE={@value pt.up.fc.dcc.ssd.p2p.Config#MAX_BUCKET_SIZE},
     * since if the {@value pt.up.fc.dcc.ssd.p2p.Config#MAX_BUCKET_SIZE} closest nodes can't find the node,
     * then it cannot be found entirely), until it gets a response with an ID matching the provided ID
     *
     * @param destinationId the ID this node wants to find
     * @return true if the node was found, false otherwise
     */
    public boolean findNode(ID destinationId) {
        try {
            List<DistancedConnectionInfo> connectionInfos = routingTable.findClosest(destinationId);

            if (connectionInfos.isEmpty()) {
                logger.warning("Node has not been bootstrapped");
                return false;
            }

            if (connectionInfos.get(0).getId().equals(destinationId)) {
                return true;
            }

            for (DistancedConnectionInfo info : connectionInfos) {
                KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(
                        ManagedChannelBuilder
                                .forAddress(info.getAddress(), info.getPort())
                                .usePlaintext()
                                .build()
                );

                FindNodeRequest request = FindNodeRequest
                        .newBuilder()
                        .setDestId(destinationId.toBinaryString())
                        .setConnectionInfo(info.toGrpcConnectionInfo())
                        .build();

                FindNodeResponse response = stub.findNode(FindNodeRequest
                        .newBuilder()
                        .setDestId(destinationId.toBinaryString())
                        .setConnectionInfo(connectionInfo.toDistancedConnectionInfo().toGrpcConnectionInfo())
                        .build()
                );

                List<DistancedConnectionInfo> receivedInfos = DistancedConnectionInfo
                        .fromGrpcConnectionInfo(response.getConnectionInfosList());
                // they come with distance to destination node but i should save them without distance in this nodes routing table

                // check if nodes have bigger distance
                receivedInfos.removeAll(connectionInfos);

                routingTable.update(receivedInfos);

                if (receivedInfos.get(0).getId().equals(destinationId)) {
                    return true;
                } else {
                    connectionInfos.addAll(connectionInfos.indexOf(info) + 1, receivedInfos);
                }
            }

            return false;
        } catch (RoutingTableException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    // WIP
    public boolean findValue(ID id) {
        return false;
    }

    // WIP
    public boolean store(ID id, byte[] byteArray) {
        return false;
    }

    // WIP
    public boolean gossip(byte[] byteArray) {
        return false;
    }

    /**
     * Kademlia RPC implementation
     */
    // TODO: make everything async
    private class Impl extends KademliaGrpc.KademliaImplBase {

        @Override
        public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
            if (!ValidationUtils.validateRequest(request)) {
                responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT));
                return;
            }

            DistancedConnectionInfo originConnectionInfo = DistancedConnectionInfo.fromGrpcConnectionInfo(request.getConnectionInfo());

            if (!routingTable.update(originConnectionInfo.getId(), originConnectionInfo.getConnectionInfo())) {
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

            List<DistancedConnectionInfo> infos;
            try {
                infos = routingTable.findClosest(ID.fromString(request.getDestId()));
            } catch (RoutingTableException e) {
                responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage())));
                return;
            }

            DistancedConnectionInfo originConnectionInfo = DistancedConnectionInfo.fromGrpcConnectionInfo(request.getConnectionInfo());

            routingTable.update(originConnectionInfo.getId(), originConnectionInfo.getConnectionInfo());

            FindNodeResponse.Builder response = FindNodeResponse.newBuilder();

            if (!(infos == null)) {
                response.addAllConnectionInfos(infos.stream().map(DistancedConnectionInfo::toGrpcConnectionInfo).collect(Collectors.toList()));
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

    public static final class Builder {
        private ID id = new ID();
        private int port = 0;
        private String address = "localhost";
        private NodeType type = NodeType.NODE;
        private final ArrayList<KademliaImpl> implementations = new ArrayList<>();

        public Builder id(ID id) {
            this.id = id;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder addService(KademliaImpl kademliaImpl) {
            implementations.add(kademliaImpl);
            return this;
        }

        public Builder addServices(KademliaImpl... kademliaImpl) {
            implementations.addAll(Arrays.asList(kademliaImpl));
            return this;
        }

        public Builder type(NodeType type) {
            this.type = type;
            return this;
        }

        public KademliaNode build() {
            switch (type) {
                case BOOTSTRAP_NODE:
                    return new KademliaNode(BOOTSTRAP_NODE_ID, BOOTSTRAP_NODE_ADDR, BOOTSTRAP_NODE_PORT, implementations);
                case NODE:
                default:
                    return new KademliaNode(id, address, port, implementations);
            }
        }
    }
}