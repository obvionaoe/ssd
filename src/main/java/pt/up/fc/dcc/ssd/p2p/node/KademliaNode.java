package pt.up.fc.dcc.ssd.p2p.node;

import com.google.rpc.Code;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.up.fc.dcc.ssd.p2p.common.Config;
import pt.up.fc.dcc.ssd.p2p.conn.ConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.conn.DistancedConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.grpc.*;
import pt.up.fc.dcc.ssd.p2p.routing.RoutingTable;
import pt.up.fc.dcc.ssd.p2p.routing.exceptions.RoutingTableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static pt.up.fc.dcc.ssd.p2p.common.Config.*;
import static pt.up.fc.dcc.ssd.p2p.common.util.Utils.isNull;
import static pt.up.fc.dcc.ssd.p2p.conn.DistancedConnectionInfo.fromGrpcConnectionInfo;
import static pt.up.fc.dcc.ssd.p2p.grpc.Status.*;
import static pt.up.fc.dcc.ssd.p2p.grpc.util.RpcCall.rpc;
import static pt.up.fc.dcc.ssd.p2p.grpc.util.RpcType.*;
import static pt.up.fc.dcc.ssd.p2p.node.NodeType.NODE;

public class KademliaNode {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final ID id;
    private final String address;
    private ConnectionInfo connectionInfo;
    private final Server server;
    public final RoutingTable routingTable;
    private final HashMap<ID, byte[]> repository;
    private boolean started;

    protected KademliaNode(ID id, String address, int port, List<KademliaImpl> kademliaImpl) {
        this.id = id;
        this.address = address;
        routingTable = new RoutingTable(id);
        repository = new HashMap<>();
        ServerBuilder<?> sb = ServerBuilder.forPort(port);
        sb.addService(new KademliaImpl(routingTable, repository));
        kademliaImpl.forEach(sb::addService);
        server = sb.build();
        started = false;
        // TODO: Add timer for routing table management pings
    }

    protected KademliaNode(ID id, String address, List<KademliaImpl> kademliaImpl) {
        this.id = id;
        this.address = address;
        routingTable = new RoutingTable(id);
        repository = new HashMap<>();
        ServerBuilder<?> sb = ServerBuilder.forPort(0);
        sb.addService(new KademliaImpl(routingTable, repository));
        kademliaImpl.forEach(sb::addService);
        server = sb.build();
        // TODO: Add timer for routing table management pings
    }

    /**
     * Creates a KademliaNode.Builder object for easier construction of a KademliaNode object
     *
     * @return an empty KademliaNode.Builder object
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Starts the node
     *
     * @throws IOException if there's a problem while starting this node
     */
    public void start() throws IOException {
        server.start();
        connectionInfo = new ConnectionInfo(id, address, server.getPort());
        started = true;
    }

    /**
     * Graciously shutdown this node
     *
     * @throws InterruptedException if there's a problem while shutting down this node
     */
    public boolean stop() throws InterruptedException {
        if (started) {
            // TODO: leave();
            return server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        } else {
            logger.warning("This node has not been started yet");
            return false;
        }
    }

    /**
     * Gets the ID associated with this node
     *
     * @return this node's ID
     */
    public ID getId() {
        return id;
    }

    /**
     * Get's the ConnectionInfo object associated with this node
     *
     * @return this node's connection information
     */
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
     * <p>
     * This node calls the {@link pt.up.fc.dcc.ssd.p2p.grpc.KademliaImpl#findNode(FindNodeRequest, StreamObserver)}
     * RPC on the bootstrap node for it's own node ID
     *
     * @param destinationId an ID object representing the bootstrap node's id
     * @param address       a string with the IP or URI compliant name, eg: "localhost", "127.0.0.1", "192.168.2.36"
     * @param port          a port number
     * @return true if the node was successfully bootstrapped, false otherwise
     */
    public boolean bootstrap(ID destinationId, String address, int port) {
        ConnectionInfo destinationConnectionInfo = new ConnectionInfo(destinationId, address, port);

        routingTable.update(destinationId, destinationConnectionInfo);

        FindNodeResponse response = (FindNodeResponse) rpc()
                .withOriginConnInfo(connectionInfo)
                .withDestConnInfo(destinationConnectionInfo)
                .type(FIND_NODE)
                .withIdToFind(id)
                .call();

        try {
            return addResultsAndPing(fromGrpcConnectionInfo(response.getConnectionInfosList()));
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode().toString().equals(Code.UNAVAILABLE.toString())) {
                logger.warning("Node with id " + destinationId + " is not online");
            } else {
                logger.warning(e.getMessage() + e.getStatus().getCode());
            }

            return false;
        }
    }

    /**
     * Pings the node associated with the given ID in this node's routing table,
     * if the ID is not present in this node's routing table it calls {@link #findNode(ID)} for the given ID
     *
     * @param destinationId the ID of the node to be pinged
     * @return true if the pinged node is alive, false otherwise
     */
    public boolean ping(ID destinationId) {
        // TODO: clean up code
        // the destination node should always be in the routing table,
        // so it will automatically be the closest node as distance(closest, dest) = 0
        // that means it will be the first node in our sorted list of connection information
        try {
            List<DistancedConnectionInfo> closestList = routingTable.findClosest(destinationId);

            if (closestList.isEmpty()) {
                logger.warning("Empty routing table, please bootstrap this node first!");
                return false;
            }

            DistancedConnectionInfo closestInfo = closestList.get(0);

            if (!closestInfo.getId().equals(destinationId)) {
                if (!findNode(destinationId)) {
                    return false;
                }
            }

            try {
                PingResponse response = (PingResponse) rpc()
                        .withOriginConnInfo(connectionInfo)
                        .withDestConnInfo(closestInfo)
                        .type(PING)
                        .call();

                if (response.getStatus().equals(PONG)) {
                    routingTable.update(closestInfo);
                    return true;
                } else {
                    routingTable.remove(closestInfo.getId());
                    return false;
                }
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode().toString().equals(Code.UNAVAILABLE.toString())) {
                    logger.warning("Node with id " + destinationId + " is not online");
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

    // TODO: redo this clusterfuck
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

    /**
     * This function calls the {@link pt.up.fc.dcc.ssd.p2p.grpc.KademliaImpl#findNode(FindNodeRequest, StreamObserver)}
     * RPC on each of the nodes this node has in it's routing table
     * (the max. number of nodes will be MAX_BUCKET_SIZE={@value Config#MAX_BUCKET_SIZE},
     * since if the {@value Config#MAX_BUCKET_SIZE} closest nodes can't find the node,
     * then it cannot be found entirely), until it gets a response with an ID matching the provided ID
     *
     * @param destinationId the ID this node wants to find
     * @return true if the node was found, false otherwise
     */
    public boolean findNode(ID destinationId) {
        try {
            List<DistancedConnectionInfo> closestInfosList = routingTable.findClosest(destinationId);

            if (closestInfosList.isEmpty()) {
                logger.warning("Empty routing table, please bootstrap this node first!");
                return false;
            }

            if (closestInfosList.get(0).getId().equals(destinationId)) {
                return true;
            }

            for (DistancedConnectionInfo info : closestInfosList) {
                FindNodeResponse response = (FindNodeResponse) rpc()
                        .withOriginConnInfo(connectionInfo)
                        .withDestConnInfo(info)
                        .type(FIND_NODE)
                        .withIdToFind(destinationId)
                        .call();

                List<DistancedConnectionInfo> receivedInfos = fromGrpcConnectionInfo(response.getConnectionInfosList());

                receivedInfos.removeIf(i -> i.getId().equals(id));

                // TODO: check if nodes have bigger distance
                receivedInfos.removeAll(closestInfosList);

                if (!receivedInfos.isEmpty()) {
                    routingTable.update(receivedInfos);
                    if (receivedInfos.get(0).getId().equals(destinationId)) {
                        return true;
                    } else {
                        closestInfosList.addAll(closestInfosList.indexOf(info) + 1, receivedInfos);
                    }
                }
            }

            return false;
        } catch (RoutingTableException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    // TODO: findValue
    public byte[] findValue(ID key) {
        try {
            List<DistancedConnectionInfo> closestInfosList = routingTable.findClosest(key);

            if (closestInfosList.isEmpty()) {
                logger.warning("Empty routing table, please bootstrap this node first!");
                return null;
            }

            for (DistancedConnectionInfo info : closestInfosList) {
                FindValueResponse response = (FindValueResponse) rpc()
                        .withOriginConnInfo(connectionInfo)
                        .withDestConnInfo(info)
                        .type(FIND_VALUE)
                        .withIdToFind(key)
                        .call();

                Status status = response.getStatus();

                if (status.equals(FOUND)) {
                    Data data = response.getData();
                    return data.getValue().toByteArray();
                } else if (status.equals(NOT_FOUND)) {
                    List<DistancedConnectionInfo> receivedInfos = fromGrpcConnectionInfo(response.getConnectionInfosList());

                    receivedInfos.removeIf(i -> i.getId().equals(id));

                    // TODO: check if nodes have bigger distance
                    receivedInfos.removeAll(closestInfosList);
                }
            }

            return null;
        } catch (RoutingTableException e) {
            logger.warning(e.getMessage());
            return null;
        }
    }

    /**
     * Calls the {@link pt.up.fc.dcc.ssd.p2p.grpc.KademliaImpl#store(StoreRequest, StreamObserver)} RPC
     * on the {@value Config#MAX_BUCKET_SIZE} closest nodes to the provided key ID
     *
     * @param key   the ID of the data to be stored
     * @param value the data in byte[]
     * @return true if at least one node successfully stored the <key, value> pair, false otherwise
     */
    public boolean store(ID key, byte[] value) {
        try {
            List<DistancedConnectionInfo> closestInfoList = routingTable.findClosest(key);

            List<StoreResponse> responses = new ArrayList<>();

            closestInfoList.forEach(info -> responses
                    .add((StoreResponse) rpc()
                            .withOriginConnInfo(connectionInfo)
                            .withDestConnInfo(info).type(STORE)
                            .withData(key, value)
                            .call()
                    )
            );

            return responses.stream().anyMatch(response -> response.getStatus().equals(ACCEPTED));
        } catch (RoutingTableException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    // TODO: gossip
    public boolean gossip(byte[] byteArray) {
        return false;
    }


    public static final class Builder {
        private ID id = new ID();
        private Integer port = 0;
        private String address = "localhost";
        private NodeType type = NODE;
        private final ArrayList<KademliaImpl> implementations = new ArrayList<>();

        private Builder() {
        }

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
                    return isNull(port) ? new KademliaNode(id, address, implementations)
                            : new KademliaNode(id, address, port, implementations);
            }
        }
    }
}