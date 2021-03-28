package pt.up.fc.dcc.ssd.p2p.node;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import pt.up.fc.dcc.ssd.p2p.grpc.KademliaImpl;
import pt.up.fc.dcc.ssd.p2p.grpc.Transaction;

public class ClientNode extends KademliaNode{
    public String role;
    public String topic;
    public String bid;

    public ClientNode(int port, ClientImpl clientImpl, String Role, String Topic, String Bid) {
        super(port, clientImpl);
        role = Role;
        topic = Topic;
        bid = Bid;
    }

    public void makeTransaction(){
        Channel channel = ManagedChannelBuilder
                .forAddress("localhost", port)
                .usePlaintext()
                .keepAliveWithoutCalls(true)
                .build();

        ValueRequest request = ValueRequest
                .newBuilder()
                .setRole(role)
                .setTopic(topic)
                .setBid(bid)
                .build();

        KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        idk = stub.findValue(ValueRequest.newBuilder().build());
    }
}

public class ClientImpl extends KademliaImpl{
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        responseObserver.onNext(PingResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void findValue(ValueRequest request, StreamObserver<ValueResponse> responseObserver) {
        

        // This is meant to find a node with same topic 
        // TODO: if its the same topic and good bid, make transaction
        if(request.topic == topic) {
            string transactionId = new hash();
            long timestamp = new timestamp();
            if (request.role == "seller") {
                if (request.bid <= bid)
                    new Transaction(transactionId, senderNodeId, receiverNodeId, timestamp, request.bid);
            }else if (request.role == "buyer") {
                if (request.bid >= bid)
                    new Transaction(transactionId, senderNodeId, receiverNodeId, timestamp, request.bid);
            }
        }
        responseObserver.onNext(ValueResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
