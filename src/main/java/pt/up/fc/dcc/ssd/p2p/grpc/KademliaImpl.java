package pt.up.fc.dcc.ssd.p2p.grpc;

import com.google.protobuf.ByteString;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.up.fc.dcc.ssd.auction.BidsRepo;
import pt.up.fc.dcc.ssd.auction.TopicsRepo;
import pt.up.fc.dcc.ssd.blockchain.BlockchainRepo;
import pt.up.fc.dcc.ssd.common.Repository;
import pt.up.fc.dcc.ssd.p2p.conn.DistancedConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.node.Id;
import pt.up.fc.dcc.ssd.p2p.routing.RoutingTable;
import pt.up.fc.dcc.ssd.p2p.routing.exceptions.RoutingTableException;

import java.util.List;
import java.util.stream.Collectors;

import static io.grpc.Status.INTERNAL;
import static io.grpc.Status.INVALID_ARGUMENT;
import static pt.up.fc.dcc.ssd.p2p.conn.DistancedConnectionInfo.fromGrpcConnectionInfo;
import static pt.up.fc.dcc.ssd.p2p.grpc.Status.*;
import static pt.up.fc.dcc.ssd.p2p.grpc.Validation.validateRequest;
import static pt.up.fc.dcc.ssd.p2p.node.Id.idFromBinaryString;

/**
 * Kademlia RPC implementation
 */
public class KademliaImpl extends KademliaGrpc.KademliaImplBase {
    private final RoutingTable routingTable;
    private final BlockchainRepo blockchain;
    private final TopicsRepo topicsRepo;
    private final BidsRepo bidsRepo;

    public KademliaImpl(RoutingTable routingTable, BlockchainRepo blockchain, TopicsRepo topicsRepo, BidsRepo bidsRepo) {
        this.routingTable = routingTable;
        this.blockchain = blockchain;
        this.topicsRepo = topicsRepo;
        this.bidsRepo = bidsRepo;
    }

    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        if (!validateRequest(request)) {
            responseObserver.onError(new StatusRuntimeException(INVALID_ARGUMENT));
            return;
        }

        DistancedConnectionInfo originConnectionInfo = fromGrpcConnectionInfo(request.getOriginConnectionInfo());

        if (!routingTable.update(originConnectionInfo)) {
            responseObserver.onError(new StatusRuntimeException(INTERNAL));
            return;
        }

        responseObserver.onNext(PingResponse
                .newBuilder()
                .setStatus(PONG)
                .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void store(StoreRequest request, StreamObserver<StoreResponse> responseObserver) {
        if (!validateRequest(request)) {
            responseObserver.onError(new StatusRuntimeException(INVALID_ARGUMENT));
            return;
        }

        if (!routingTable.update(fromGrpcConnectionInfo(request.getOriginConnectionInfo()))) {
            responseObserver.onError(new StatusRuntimeException(INTERNAL));
            return;
        }

        Data data = request.getData();

        StoreResponse.Builder response = StoreResponse.newBuilder();

        Repository repo = getRepo(request.getDataType());

        // TODO: should have keepAlive time
        if (repo.put(idFromBinaryString(data.getKey()), data.getValue().toByteArray())) {
            response.setStatus(ACCEPTED);
        } else {
            response.setStatus(FAILED);
        }

        // TODO: check type and store in blockchain?

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void findNode(FindNodeRequest request, StreamObserver<FindNodeResponse> responseObserver) {
        if (!validateRequest(request)) {
            responseObserver.onError(new StatusRuntimeException(INVALID_ARGUMENT));
            return;
        }

        List<DistancedConnectionInfo> infos;
        try {
            infos = routingTable.findClosest(idFromBinaryString(request.getDestId()));
        } catch (RoutingTableException e) {
            responseObserver.onError(new StatusRuntimeException(INTERNAL.withDescription(e.getMessage())));
            return;
        }

        DistancedConnectionInfo originConnectionInfo = fromGrpcConnectionInfo(request.getOriginConnectionInfo());

        routingTable.update(originConnectionInfo);

        FindNodeResponse.Builder response = FindNodeResponse.newBuilder();

        if (infos != null) {
            response.addAllConnectionInfos(infos.stream().map(DistancedConnectionInfo::toGrpcConnectionInfo).collect(Collectors.toList()));
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void findValue(FindValueRequest request, StreamObserver<FindValueResponse> responseObserver) {
        if (!validateRequest(request)) {
            responseObserver.onError(new StatusRuntimeException(INVALID_ARGUMENT));
            return;
        }

        Id key = idFromBinaryString(request.getKey());

        FindValueResponse.Builder response = FindValueResponse.newBuilder();

        Repository repo = getRepo(request.getDataType());

        if (repo.containsKey(key)) {
            Data data = Data
                    .newBuilder()
                    .setKey(request.getKey())
                    .setValue(ByteString.copyFrom(repo.get(key)))
                    .build();

            responseObserver.onNext(response
                    .setStatus(FOUND)
                    .setData(data)
                    .build()
            );
            responseObserver.onCompleted();
        } else {
            List<DistancedConnectionInfo> infos;
            try {
                infos = routingTable.findClosest(key);
            } catch (RoutingTableException e) {
                responseObserver.onError(new StatusRuntimeException(INTERNAL.withDescription(e.getMessage())));
                return;
            }

            if (infos != null) {
                response.addAllConnectionInfos(infos.stream().map(DistancedConnectionInfo::toGrpcConnectionInfo).collect(Collectors.toList()));
            }

            responseObserver.onNext(response
                    .setStatus(NOT_FOUND)
                    .build()
            );
            responseObserver.onCompleted();
        }
    }

    @Override
    public void leave(LeaveRequest request, StreamObserver<LeaveResponse> responseObserver) {
        // TODO
    }

    @Override
    public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
        // TODO
    }

    private Repository getRepo(DataType dataType) {
        return dataType.equals(DataType.BID) ? bidsRepo : dataType.equals(DataType.TOPIC) ? topicsRepo : blockchain;
    }
}