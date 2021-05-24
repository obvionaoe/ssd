package pt.up.fc.dcc.ssd.p2p.grpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.up.fc.dcc.ssd.p2p.conn.ConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.conn.DistancedConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.grpc.KademliaGrpc.KademliaBlockingStub;
import pt.up.fc.dcc.ssd.p2p.node.Id;

import static pt.up.fc.dcc.ssd.common.Utils.isNull;
import static pt.up.fc.dcc.ssd.p2p.grpc.ResponsePair.pair;
import static pt.up.fc.dcc.ssd.p2p.grpc.Status.FAILED;

//TODO: add comments

/**
 * Object that creates a request and calls an RPC
 */
public class RpcCall {
    private KademliaBlockingStub stub;
    private ConnectionInfo originConnectionInfo;
    private ConnectionInfo destinationConnectionInfo;
    private RpcType type;
    private Id idToFind;
    private Data data;

    private RpcCall() {
    }

    public static RpcCall rpc() {
        return new RpcCall();
    }

    public RpcCall withOriginConnInfo(ConnectionInfo originConnectionInfo) {
        this.originConnectionInfo = originConnectionInfo;
        return this;
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

    public RpcCall withData(Id key, byte[] value) {
        data = Data
                .newBuilder()
                .setKey(key.toBinaryString())
                .setValue(ByteString.copyFrom(value))
                .build();

        return this;
    }

    /**
     * Calls and RPC of the specified type and returns a ResponsePair<Status, GeneratedMessageV3>
     *
     * @return a ResponsePair with the status and the response
     * @throws NullPointerException if a
     */
    public ResponsePair<Status, GeneratedMessageV3> call() throws NullPointerException {
        if (isNull(destinationConnectionInfo)) {
            throw new NullPointerException("Missing destination connection information!");
        }

        if (isNull(originConnectionInfo)) {
            throw new NullPointerException("Missing origin connection information!");
        }

        if (isNull(type)) {
            throw new NullPointerException("Missing RPC type!");
        }

        ManagedChannel channel = null;
        ResponsePair<Status, GeneratedMessageV3> responsePair = null;
        try {
            channel = ManagedChannelBuilder
                    .forAddress(destinationConnectionInfo.getAddress(), destinationConnectionInfo.getPort())
                    .usePlaintext()
                    .build();

            if (isNull(stub)) {
                stub = KademliaGrpc.newBlockingStub(channel);
            }

            switch (type) {
                case PING:
                    PingResponse pingResponse = stub.ping(PingRequest
                            .newBuilder()
                            .setOriginConnectionInfo(originConnectionInfo
                                    .toDistancedConnectionInfo()
                                    .toGrpcConnectionInfo()
                            )
                            .build()
                    );

                    responsePair = pair(pingResponse.getStatus(), pingResponse);
                    break;
                case STORE:
                    if (isNull(data)) {
                        throw new NullPointerException("Missing data to store!");
                    }

                    StoreResponse storeResponse = stub.store(StoreRequest
                            .newBuilder()
                            .setOriginConnectionInfo(originConnectionInfo
                                    .toDistancedConnectionInfo()
                                    .toGrpcConnectionInfo()
                            ).setData(data)
                            .build()
                    );

                    responsePair = pair(storeResponse.getStatus(), storeResponse);
                    break;
                case FIND_NODE:
                    if (isNull(idToFind)) {
                        throw new NullPointerException("Missing lookup id!");
                    }

                    FindNodeResponse findNodeResponse = stub.findNode(FindNodeRequest
                            .newBuilder()
                            .setDestId(idToFind.toBinaryString())
                            .setOriginConnectionInfo(originConnectionInfo
                                    .toDistancedConnectionInfo()
                                    .toGrpcConnectionInfo()
                            )
                            .build()
                    );

                    responsePair = pair(null, findNodeResponse);
                    break;
                case FIND_VALUE:
                    if (isNull(idToFind)) {
                        throw new NullPointerException("Missing lookup id!");
                    }

                    FindValueResponse findValueResponse = stub.findValue(FindValueRequest
                            .newBuilder()
                            .setOriginConnectionInfo(originConnectionInfo
                                    .toDistancedConnectionInfo()
                                    .toGrpcConnectionInfo()
                            )
                            .setKey(idToFind.toBinaryString())
                            .build()
                    );

                    responsePair = pair(findValueResponse.getStatus(), findValueResponse);
                    break;
                case LEAVE:
                    if (isNull(idToFind)) {
                        throw new NullPointerException("Missing node id!");
                    }

                    LeaveResponse leaveResponse = stub.leave(LeaveRequest
                            .newBuilder()
                            .setId(idToFind.toBinaryString())
                            .build()
                    );

                    responsePair = pair(leaveResponse.getStatus(), leaveResponse);
                    break;
                case GOSSIP:
                    if (isNull(data)) {
                        throw new NullPointerException("Missing data");
                    }

                    GossipResponse gossipResponse = stub.gossip(GossipRequest
                            .newBuilder() //TODO: add stuff here
                            .build()
                    );

                    responsePair = pair(gossipResponse.getStatus(), gossipResponse);
                    break;
            }
        } catch (StatusRuntimeException e) {
            responsePair = pair(FAILED, null);
        } finally {
            if (channel != null) {
                channel.shutdownNow();
            }
        }

        return responsePair;
    }
}
