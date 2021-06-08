package pt.up.fc.dcc.ssd.p2p.grpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import pt.up.fc.dcc.ssd.common.Pair;
import pt.up.fc.dcc.ssd.p2p.conn.ConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.conn.DistancedConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.grpc.KademliaGrpc.KademliaBlockingStub;
import pt.up.fc.dcc.ssd.p2p.node.Id;
import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;

import javax.net.ssl.SSLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static pt.up.fc.dcc.ssd.common.Pair.pair;
import static pt.up.fc.dcc.ssd.common.Utils.isNull;
import static pt.up.fc.dcc.ssd.p2p.grpc.DataType.TOPIC;
import static pt.up.fc.dcc.ssd.p2p.grpc.Status.ACCEPTED;
import static pt.up.fc.dcc.ssd.p2p.grpc.Status.FAILED;
import static pt.up.fc.dcc.ssd.p2p.security.Ssl.loadClientTlsCredentials;

/**
 * Object that creates a request and calls an RPC
 */
public class RpcCall {
    private KademliaNode self;
    private KademliaBlockingStub stub;
    private ConnectionInfo destinationConnectionInfo;
    private RpcType type;
    private Id idToFind;
    private Float bid;
    private Data data;
    private DataType dataType;
    private List<Id> visitedIds;

    private RpcCall(KademliaNode self) {
        this.self = self;
    }

    public static RpcCall rpc(KademliaNode self) {
        return new RpcCall(self);
    }

    public RpcCall withDestConnInfo(ConnectionInfo destinationConnectionInfo) {
        this.destinationConnectionInfo = destinationConnectionInfo;
        return this;
    }

    public RpcCall withDestConnInfo(DistancedConnectionInfo destinationConnectionInfo) {
        this.destinationConnectionInfo = destinationConnectionInfo.getConnectionInfo();
        return this;
    }

    public RpcCall type(RpcType type) {
        this.type = type;
        return this;
    }

    public RpcCall withId(Id destinationId) {
        this.idToFind = destinationId;
        return this;
    }

    public RpcCall withBid(float bid) {
        this.bid = bid;
        return this;
    }

    /**
     * Adds a Data object to the Builder
     *
     * @param key the Id of the data
     * @param value the byte[] with the data
     * @return this Builder
     */
    public RpcCall withData(Id key, byte[] value) {
        data = Data
            .newBuilder()
            .setKey(key.toBinaryString())
            .setValue(ByteString.copyFrom(value))
            .build();

        return this;
    }

    /**
     * Adds a Data object to the Builder
     *
     * @param value the byte[] with the data
     * @return this Builder
     */
    public RpcCall withData(byte[] value) {
        data = Data
            .newBuilder()
            .setValue(ByteString.copyFrom(value))
            .build();

        return this;
    }

    public RpcCall withDataType(DataType dataType) {
        this.dataType = dataType;
        return this;
    }

    public RpcCall withVisitedIds(List<Id> visitedIds) {
        this.visitedIds = visitedIds;
        return this;
    }

    /**
     * Calls and RPC of the specified type and returns a ResponsePair<Status, GeneratedMessageV3>
     *
     * @return a ResponsePair with the status and the response
     * @throws NullPointerException if a
     */
    public Pair<Status, GeneratedMessageV3> call() throws NullPointerException {
        if (isNull(destinationConnectionInfo)) {
            throw new NullPointerException("Missing destination connection information!");
        }

        if (isNull(self.getConnectionInfo())) {
            throw new NullPointerException("Missing origin connection information!");
        }

        if (isNull(type)) {
            throw new NullPointerException("Missing RPC type!");
        }

        ManagedChannel channel = null;
        Pair<Status, GeneratedMessageV3> pair = null;
        try {
            SslContext sslContext = loadClientTlsCredentials();
            channel = NettyChannelBuilder
                .forAddress(destinationConnectionInfo.getAddress(), destinationConnectionInfo.getPort())
                .sslContext(sslContext)
                .build();

            if (isNull(stub)) {
                stub = KademliaGrpc.newBlockingStub(channel);
            }

            switch (type) {
                case PING:
                    PingResponse pingResponse = stub.ping(PingRequest
                        .newBuilder()
                        .setOriginConnectionInfo(self.getConnectionInfo()
                            .toDistancedConnectionInfo()
                            .toGrpcConnectionInfo()
                        )
                        .build()
                    );

                    pair = pair(pingResponse.getStatus(), pingResponse);
                    break;
                case STORE:
                    if (isNull(data)) {
                        throw new NullPointerException("Missing data to store!");
                    }

                    StoreResponse storeResponse = stub.store(StoreRequest
                        .newBuilder()
                        .setOriginConnectionInfo(self.getConnectionInfo()
                            .toDistancedConnectionInfo()
                            .toGrpcConnectionInfo()
                        ).setData(data)
                        .setDataType(TOPIC)
                        .build()
                    );

                    pair = pair(storeResponse.getStatus(), storeResponse);
                    break;
                case GOSSIP:
                    if (isNull(data) || isNull(dataType)) {
                        throw new NullPointerException("Missing data");
                    }

                    List<String> visitedList;

                    if (isNull(visitedIds)) {
                        visitedList = new ArrayList<>();
                        visitedList.add(self.getId().toBinaryString());
                    } else {
                        visitedList = visitedIds.stream().map(Id::toBinaryString).collect(Collectors.toList());
                    }

                    GossipResponse gossipResponse = stub.gossip(GossipRequest
                        .newBuilder()
                        .setOriginConnectionInfo(self.getConnectionInfo()
                            .toDistancedConnectionInfo()
                            .toGrpcConnectionInfo()
                        )
                        .addAllVisitedNodeIds(visitedList)
                        .setData(data)
                        .setDataType(dataType)
                        .build()
                    );

                    pair = pair(gossipResponse.getStatus(), gossipResponse);
                    break;
                case BID:
                    if (isNull(idToFind) || isNull(bid)) {
                        throw new NullPointerException("Missing node id!");
                    }

                    BidResponse bidResponse = stub.bid(BidRequest
                        .newBuilder()
                        .setOriginConnectionInfo(self.getConnectionInfo()
                            .toDistancedConnectionInfo()
                            .toGrpcConnectionInfo()
                        )
                        .setItemId(idToFind.toBinaryString())
                        .setBid(bid)
                        .build()
                    );

                    pair = pair(bidResponse.getStatus(), bidResponse);
                    break;
                case FIND_NODE:
                    if (isNull(idToFind)) {
                        throw new NullPointerException("Missing lookup id!");
                    }

                    FindNodeResponse findNodeResponse = stub.findNode(FindNodeRequest
                        .newBuilder()
                        .setDestId(idToFind.toBinaryString())
                        .setOriginConnectionInfo(self.getConnectionInfo()
                            .toDistancedConnectionInfo()
                            .toGrpcConnectionInfo()
                        )
                        .build()
                    );

                    pair = pair(ACCEPTED, findNodeResponse);
                    break;
                case FIND_VALUE:
                    if (isNull(idToFind)) {
                        throw new NullPointerException("Missing lookup id!");
                    }

                    FindValueResponse findValueResponse = stub.findValue(FindValueRequest
                        .newBuilder()
                        .setOriginConnectionInfo(self.getConnectionInfo()
                            .toDistancedConnectionInfo()
                            .toGrpcConnectionInfo()
                        )
                        .setKey(idToFind.toBinaryString())
                        .build()
                    );

                    pair = pair(findValueResponse.getStatus(), findValueResponse);
                    break;
                case FIND_ITEMS:
                    if (isNull(idToFind)) {
                        throw new NullPointerException("Missing lookup id!");
                    }

                    FindItemsResponse findItemsResponse = stub.findItems(FindItemsRequest
                        .newBuilder()
                        .setOriginConnectionInfo(self.getConnectionInfo()
                            .toDistancedConnectionInfo()
                            .toGrpcConnectionInfo()
                        )
                        .setTopic(idToFind.toBinaryString())
                        .setDataType(TOPIC)
                        .build()
                    );

                    pair = pair(findItemsResponse.getStatus(), findItemsResponse);
                    break;
                case LEAVE:
                    stub.leave(LeaveRequest
                        .newBuilder()
                        .setId(self.getId().toBinaryString())
                        .build()
                    );

                    pair = null;
                    break;
            }
        } catch (StatusRuntimeException | SSLException e) {
            pair = pair(FAILED, null);
        } finally {
            if (channel != null) {
                channel.shutdownNow();
            }
        }

        return pair;
    }
}
