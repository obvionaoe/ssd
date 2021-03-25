package pt.up.fc.dcc.ssd.p2p.grpc;

import io.grpc.stub.StreamObserver;
import pt.up.fc.dcc.ssd.p2p.KademliaGrpc;
import pt.up.fc.dcc.ssd.p2p.PingRequest;
import pt.up.fc.dcc.ssd.p2p.PingResponse;

public class KademliaImpl extends KademliaGrpc.KademliaImplBase {
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        super.ping(request, responseObserver);
    }


}