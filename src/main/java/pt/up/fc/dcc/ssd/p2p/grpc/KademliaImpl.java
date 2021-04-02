package pt.up.fc.dcc.ssd.p2p.grpc;

import io.grpc.stub.StreamObserver;
import pt.up.fc.dcc.ssd.p2p.*;

public class KademliaImpl extends KademliaGrpc.KademliaImplBase {
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        super.ping(request, responseObserver);
    }

    @Override
    public void store(StoreRequest request, StreamObserver<StoreResponse> responseObserver) {
        super.store(request, responseObserver);
    }

    @Override
    public void findNode(SearchRequest request, StreamObserver<SearchResponse> responseObserver) {
        super.findNode(request, responseObserver);
    }

    @Override
    public void findValue(ValueRequest request, StreamObserver<ValueResponse> responseObserver) {
        super.findValue(request, responseObserver);
    }
}