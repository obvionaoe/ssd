package pt.up.fc.dcc.ssd.p2p.grpc.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.up.fc.dcc.ssd.p2p.conn.ConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.conn.DistancedConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.grpc.*;
import pt.up.fc.dcc.ssd.p2p.grpc.KademliaGrpc.KademliaBlockingStub;
import pt.up.fc.dcc.ssd.p2p.node.ID;

import static pt.up.fc.dcc.ssd.p2p.common.util.Utils.isNull;

public class RpcCall {
    private KademliaBlockingStub stub;
    private ConnectionInfo originConnectionInfo;
    private ConnectionInfo destinationConnectionInfo;
    private RpcType type;
    private ID idToFInd;
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

    public RpcCall withIdToFind(ID destinationId) {
        this.idToFInd = destinationId;
        return this;
    }

    public RpcCall withData(ID key, byte[] value) {
        data = Data
                .newBuilder()
                .setKey(key.toBinaryString())
                .setValue(ByteString.copyFrom(value))
                .build();

        return this;
    }

    public GeneratedMessageV3 call() throws NullPointerException {
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
        GeneratedMessageV3 response = null;
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
                    response = stub.ping(PingRequest
                            .newBuilder()
                            .setOriginConnectionInfo(originConnectionInfo
                                    .toDistancedConnectionInfo()
                                    .toGrpcConnectionInfo()
                            )
                            .build()
                    );
                    break;
                case STORE:
                    if (isNull(data)) {
                        throw new NullPointerException("Missing data to store!");
                    }

                    response = stub.store(StoreRequest
                            .newBuilder()
                            .setOriginConnectionInfo(originConnectionInfo
                                    .toDistancedConnectionInfo()
                                    .toGrpcConnectionInfo()
                            ).setData(data)
                            .build()
                    );
                    break;
                case FIND_NODE:
                    if (isNull(idToFInd)) {
                        throw new NullPointerException("Missing lookup id!");
                    }

                    response = stub.findNode(FindNodeRequest
                            .newBuilder()
                            .setDestId(idToFInd.toBinaryString())
                            .setOriginConnectionInfo(originConnectionInfo
                                    .toDistancedConnectionInfo()
                                    .toGrpcConnectionInfo()
                            )
                            .build()
                    );
                    break;
                case FIND_VALUE:
                    if (isNull(idToFInd)) {
                        throw new NullPointerException("Missing lookup id!");
                    }

                    response = stub.findValue(FindValueRequest
                            .newBuilder()
                            .setOriginConnectionInfo(originConnectionInfo
                                    .toDistancedConnectionInfo()
                                    .toGrpcConnectionInfo()
                            )
                            .setKey(idToFInd.toBinaryString())
                            .build()
                    );
                    break;
                case LEAVE:
                    break;
                case GOSSIP:
                    if (isNull(data)) {
                        throw new NullPointerException("Missing data");
                    }

                    response = stub.gossip(StoreRequest.newBuilder().build());
                    break;
            }
        } finally {
            if (channel != null) {
                channel.shutdownNow();
            }
        }

        return response;
    }
}
