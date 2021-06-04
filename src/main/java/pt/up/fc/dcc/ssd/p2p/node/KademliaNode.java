package pt.up.fc.dcc.ssd.p2p.node;

import com.google.protobuf.ByteString;
import com.google.rpc.Code;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.stub.StreamObserver;
import pt.up.fc.dcc.ssd.auction.ItemsRepo;
import pt.up.fc.dcc.ssd.blockchain.BlockchainRepo;
import pt.up.fc.dcc.ssd.common.Pair;
import pt.up.fc.dcc.ssd.p2p.Config;
import pt.up.fc.dcc.ssd.p2p.conn.ConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.conn.DistancedConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.grpc.*;
import pt.up.fc.dcc.ssd.p2p.routing.RoutingTable;
import pt.up.fc.dcc.ssd.p2p.routing.exceptions.RoutingTableException;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static pt.up.fc.dcc.ssd.common.Pair.cast;
import static pt.up.fc.dcc.ssd.common.Utils.isNotNull;
import static pt.up.fc.dcc.ssd.common.Utils.isNull;
import static pt.up.fc.dcc.ssd.p2p.Config.*;
import static pt.up.fc.dcc.ssd.p2p.conn.DistancedConnectionInfo.fromGrpcConnectionInfo;
import static pt.up.fc.dcc.ssd.p2p.grpc.RpcCall.rpc;
import static pt.up.fc.dcc.ssd.p2p.grpc.RpcType.*;
import static pt.up.fc.dcc.ssd.p2p.grpc.Status.*;
import static pt.up.fc.dcc.ssd.p2p.node.NodeType.NODE;
import static pt.up.fc.dcc.ssd.p2p.security.Ssl.loadServerTlsCredentials;

public class KademliaNode {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final Id id;
    private final String address;
    private ConnectionInfo connectionInfo;
    private final Server server;
    public final RoutingTable routingTable;
    private final BlockchainRepo blockchain;
    private final ItemsRepo itemsRepo;
    private boolean started;

    protected KademliaNode(Id id, String address, int port, SslContext sslContext) {
        this.id = id;
        this.address = address;
        routingTable = new RoutingTable(id);
        this.blockchain = new BlockchainRepo();
        this.itemsRepo = new ItemsRepo();
        ServerBuilder<?> sb = NettyServerBuilder.forPort(port).sslContext(sslContext);
        sb.addService(new KademliaImpl(this));
        server = sb.build();
        started = false;
        // TODO: Add timer for routing table management pings
    }

    protected KademliaNode(Id id, String address, SslContext sslContext) {
        this.id = id;
        this.address = address;
        routingTable = new RoutingTable(id);
        this.blockchain = new BlockchainRepo();
        this.itemsRepo = new ItemsRepo();
        ServerBuilder<?> sb = NettyServerBuilder.forPort(0).sslContext(sslContext);
        sb.addService(new KademliaImpl(this));
        server = sb.build();
        started = false;
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
        if (!started) {
            server.start();
            connectionInfo = new ConnectionInfo(id, address, server.getPort());
            started = true;
        } else {
            logger.warning("This node has already been started!");
        }
    }

    /**
     * Graciously shuts down this node
     *
     * @throws InterruptedException if there's a problem while shutting down this node
     */
    public boolean stop() throws InterruptedException {
        if (started) {
            leave();
            return server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        } else {
            logger.warning("This node has not been started yet!");
            return false;
        }
    }

    /**
     * Gets the ID associated with this node
     *
     * @return this node's ID
     */
    public Id getId() {
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

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public BlockchainRepo getBlockchain() {
        return blockchain;
    }

    public ItemsRepo getItemsRepo() {
        return itemsRepo;
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
    public boolean bootstrap(Id destinationId, String address, int port) {
        ConnectionInfo destinationConnectionInfo = new ConnectionInfo(destinationId, address, port);

        routingTable.update(destinationId, destinationConnectionInfo);

        Pair<Status, FindNodeResponse> pair = cast(rpc(this)
                .withDestConnInfo(destinationConnectionInfo)
                .type(FIND_NODE)
                .withId(id)
                .call(),
            Status.class,
            FindNodeResponse.class
        );

        if (pair.first().equals(FAILED)) {
            return false;
        }

        try {
            return addResultsAndPing(fromGrpcConnectionInfo(pair
                .second()
                .getConnectionInfosList())
            );
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
     * if the ID is not present in this node's routing table it calls {@link #findNode(Id)} for the given ID
     *
     * @param destinationId the ID of the node to be pinged
     * @return true if the pinged node is alive, false otherwise
     */
    public boolean ping(Id destinationId) {
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
                Pair<Status, PingResponse> pair = cast(rpc(this)
                        .withDestConnInfo(closestInfo)
                        .type(PING)
                        .call(),
                    Status.class,
                    PingResponse.class
                );

                if (pair.first().equals(PONG)) {
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
     * Calls the {@link pt.up.fc.dcc.ssd.p2p.grpc.KademliaImpl#findNode(FindNodeRequest, StreamObserver)}
     * RPC on each of the nodes this node has in it's routing table
     * (the max. number of nodes will be MAX_BUCKET_SIZE={@value Config#MAX_BUCKET_SIZE},
     * since if the {@value Config#MAX_BUCKET_SIZE} closest nodes can't find the node,
     * then it cannot be found entirely), until it gets a response with an ID matching the provided ID
     *
     * @param destinationId the ID this node wants to find
     * @return true if the node was found, false otherwise
     */
    public boolean findNode(Id destinationId) {
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
                Pair<Status, FindNodeResponse> pair = cast(rpc(this)
                        .withDestConnInfo(info)
                        .type(FIND_NODE)
                        .withId(destinationId)
                        .call(),
                    Status.class,
                    FindNodeResponse.class
                );

                if (pair.first().equals(FAILED)) {
                    return false;
                }

                List<DistancedConnectionInfo> receivedInfos = fromGrpcConnectionInfo(pair
                    .second()
                    .getConnectionInfosList()
                );

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

    /**
     * Calls the {@link pt.up.fc.dcc.ssd.p2p.grpc.KademliaImpl#findValue(FindValueRequest, StreamObserver)}
     * RPC on each until it finds a node which has the key ID stored in it's repository
     *
     * @param key and ID representing data stored in the network
     * @return a byte[] with the stored info if found, null otherwise
     */
    @SuppressWarnings("DuplicatedCode")
    public byte[] findValue(Id key) {
        try {
            List<DistancedConnectionInfo> closestInfosList = routingTable.findClosest(key);

            if (closestInfosList.isEmpty()) {
                logger.warning("Empty routing table, please bootstrap this node first!");
                return null;
            }

            for (int i = 0; i <= closestInfosList.size() - 1; i++) {
                DistancedConnectionInfo info = closestInfosList.get(i);

                Pair<Status, FindValueResponse> pair = cast(rpc(this)
                        .withDestConnInfo(info)
                        .type(FIND_VALUE)
                        .withId(key)
                        .call(),
                    Status.class,
                    FindValueResponse.class
                );

                if (pair.first().equals(FOUND)) {
                    Data data = pair.second().getData();
                    return data.getValue().toByteArray();
                } else if (pair.first().equals(NOT_FOUND)) {
                    List<DistancedConnectionInfo> receivedInfos = fromGrpcConnectionInfo(pair
                        .second()
                        .getConnectionInfosList()
                    );

                    receivedInfos.removeIf(receivedInfo -> receivedInfo.getId().equals(id));

                    // TODO: check if nodes have bigger distance
                    receivedInfos.removeAll(closestInfosList);

                    if (!receivedInfos.isEmpty()) {
                        routingTable.update(receivedInfos);

                        closestInfosList.addAll(closestInfosList.indexOf(info) + 1, receivedInfos);
                    }
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
     * @param key  the ID of the data to be stored
     * @param data the data in byte[]
     * @return true if at least one node successfully stored the <key, value> pair, false otherwise
     */
    public boolean store(Id key, byte[] data) {
        try {
            List<DistancedConnectionInfo> closestInfoList = routingTable.findClosest(key);

            List<Pair<Status, StoreResponse>> responses = new ArrayList<>();

            closestInfoList.forEach(info ->
                responses.add(
                    cast(rpc(this)
                            .withDestConnInfo(info).type(STORE)
                            .withData(key, data)
                            .call(),
                        Status.class,
                        StoreResponse.class
                    )
                )
            );

            itemsRepo.put(key, data);

            return responses.stream().anyMatch(pair -> pair.first().equals(ACCEPTED));
        } catch (RoutingTableException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    /**
     * Given a seller Id and an item Id, bids on an item
     *
     * @param sellerId id of seller
     * @param itemId   id of item to bid on
     * @param bid      bid amount
     * @return true if the seller accepts the bid, false otherwise
     */
    public boolean bid(Id sellerId, Id itemId, float bid) {
        try {
            if (!findNode(sellerId)) {
                return false;
            }

            DistancedConnectionInfo sellerConnInfo = routingTable.findClosest(sellerId).get(0);

            Pair<Status, BidResponse> responsePair = cast(rpc(this)
                    .withDestConnInfo(sellerConnInfo)
                    .type(BID)
                    .withId(itemId)
                    .withBid(bid)
                    .call(),
                Status.class,
                BidResponse.class
            );

            return responsePair.first().equals(ACCEPTED);
        } catch (RoutingTableException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    /**
     * Passes the given data to all the nodes on the network
     *
     * @param dataId the id of the data to gossip
     * @param data   the data to gossip
     * @return true if at least one node accepted the request, false otherwise
     */
    public boolean gossip(Id dataId, byte[] data) {
        return gossip(dataId, data, null);
    }

    /**
     * Passes the given data to all the nodes on the network
     *
     * @param dataId         the id of the data to gossip
     * @param data           the data to gossip
     * @param visitedNodeIds list of Ids of the already visited nodes
     * @return true if at least one node accepted the request, false otherwise
     */
    public boolean gossip(Id dataId, byte[] data, List<Id> visitedNodeIds) {
        try {
            List<DistancedConnectionInfo> allConnectionInfos = routingTable.getAll();

            if (isNotNull(visitedNodeIds)) {
                visitedNodeIds.forEach(visitedId ->
                    allConnectionInfos.removeIf(info ->
                        info.getId().equals(visitedId)
                    )
                );
            }

            List<Pair<Status, GossipResponse>> responses = new ArrayList<>();

            allConnectionInfos.forEach(destinationInfo ->
                responses.add(cast(rpc(this)
                        .withDestConnInfo(destinationInfo)
                        .type(GOSSIP)
                        .withData(dataId, data)
                        .withVisitedIds(visitedNodeIds)
                        .call(),
                    Status.class,
                    GossipResponse.class
                    )
                )
            );

            return responses.stream().anyMatch(responsePair -> responsePair.first().equals(ACCEPTED));
        } catch (RoutingTableException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    /**
     * Tries to find items with the given topic on the network
     *
     * @param topic the topic to search for
     * @return a list of Items in byte arrays form or null if none were found
     */
    @SuppressWarnings("DuplicatedCode")
    public List<byte[]> findItems(Id topic) {
        try {
            List<DistancedConnectionInfo> closestInfosList = routingTable.findClosest(topic);

            if (closestInfosList.isEmpty()) {
                logger.warning("Empty routing table, please bootstrap this node first!");
                return null;
            }

            for (int i = 0; i <= closestInfosList.size() - 1; i++) {
                DistancedConnectionInfo info = closestInfosList.get(i);

                Pair<Status, FindItemsResponse> pair = cast(rpc(this)
                        .withDestConnInfo(info)
                        .type(FIND_ITEMS)
                        .withId(topic)
                        .call(),
                    Status.class,
                    FindItemsResponse.class
                );

                if (pair.first().equals(FOUND)) {
                    return pair.second().getItemsList().stream().map(ByteString::toByteArray).collect(Collectors.toList());
                } else if (pair.first().equals(NOT_FOUND)) {
                    List<DistancedConnectionInfo> receivedInfos = fromGrpcConnectionInfo(pair
                        .second()
                        .getConnectionInfosList()
                    );

                    receivedInfos.removeIf(receivedInfo -> receivedInfo.getId().equals(id));

                    // TODO: check if nodes have bigger distance
                    receivedInfos.removeAll(closestInfosList);

                    if (!receivedInfos.isEmpty()) {
                        routingTable.update(receivedInfos);

                        closestInfosList.addAll(closestInfosList.indexOf(info) + 1, receivedInfos);
                    }
                }
            }

            return null;
        } catch (RoutingTableException e) {
            logger.warning(e.getMessage());
            return null;
        }
    }

    private void leave() {
        try {
            List<DistancedConnectionInfo> allConnectionInfos = routingTable.getAll();

            allConnectionInfos.forEach(destinationInfo ->
                rpc(this)
                    .withDestConnInfo(destinationInfo)
                    .type(LEAVE)
                    .call()
            );
        } catch (RoutingTableException e) {
            logger.warning(e.getMessage());
        }
    }

    /**
     * Builder object to help with KademliaNode object construction
     */
    public static final class Builder {
        private Id id = new Id();
        private Integer port = 0;
        private String address = "localhost";
        private NodeType type = NODE;
        private BlockchainRepo blockchain;
        private ItemsRepo itemsRepo;

        private Builder() {
        }

        /**
         * Assigns the provided Id to the this node
         *
         * @param id the Id to assign to this node
         * @return the builder
         */
        public Builder id(Id id) {
            this.id = id;
            return this;
        }

        /**
         * Assigns the provided port to this node
         *
         * @param port the port to assign to this node
         * @return the builder
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Assigns the provided address to this node
         *
         * @param address the address to assign to this node
         * @return the builder
         */
        public Builder address(String address) {
            this.address = address;
            return this;
        }

        /**
         * Specifies the NodeType of this node
         *
         * @param type the NodeType of this node
         * @return the builder
         */
        public Builder type(NodeType type) {
            this.type = type;
            return this;
        }

        /**
         * Builds the KademliaNode object from the properties provided to the builder
         *
         * @return the KademliaNode
         */
        public KademliaNode build() throws SSLException {
            SslContext sslContext = loadServerTlsCredentials();
            switch (type) {
                case BOOTSTRAP_NODE:
                    return new KademliaNode(BOOTSTRAP_NODE_ID, BOOTSTRAP_NODE_ADDR, BOOTSTRAP_NODE_PORT, sslContext);
                case NODE:
                default:
                    return isNull(port) ? new KademliaNode(id, address, sslContext)
                        : new KademliaNode(id, address, port, sslContext);
            }
        }
    }
}