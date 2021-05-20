package pt.up.fc.dcc.ssd.p2p.grpc;

import com.google.protobuf.ByteString;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.up.fc.dcc.ssd.p2p.conn.DistancedConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.node.Id;
import pt.up.fc.dcc.ssd.p2p.routing.RoutingTable;
import pt.up.fc.dcc.ssd.p2p.routing.exceptions.RoutingTableException;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static io.grpc.Status.INTERNAL;
import static io.grpc.Status.INVALID_ARGUMENT;
import static pt.up.fc.dcc.ssd.p2p.common.util.Utils.isNotNull;
import static pt.up.fc.dcc.ssd.p2p.conn.DistancedConnectionInfo.fromGrpcConnectionInfo;
import static pt.up.fc.dcc.ssd.p2p.grpc.Status.*;
import static pt.up.fc.dcc.ssd.p2p.grpc.util.Validation.validateRequest;
import static pt.up.fc.dcc.ssd.p2p.node.Id.idFromBinaryString;

/**
 * Kademlia RPC implementation
 */
public class KademliaImpl extends KademliaGrpc.KademliaImplBase {
    private final RoutingTable routingTable;
    private final HashMap<Id, byte[]> repository;

    public KademliaImpl(RoutingTable routingTable, HashMap<Id, byte[]> repository) {
        this.routingTable = routingTable;
        this.repository = repository;
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

        // TODO: should have keepAlive time
        if (isNotNull(repository.put(idFromBinaryString(data.getKey()), data.getValue().toByteArray()))) {
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

        if (repository.containsKey(key)) {
            Data data = Data
                    .newBuilder()
                    .setKey(request.getKey())
                    .setValue(ByteString.copyFrom(repository.get(key)))
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
}