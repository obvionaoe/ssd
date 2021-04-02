package pt.up.fc.dcc.ssd.p2p.node;
import pt.up.fc.dcc.ssd.p2p.grpc.Client;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import pt.up.fc.dcc.ssd.p2p.grpc.ClientImpl;
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

        // Luhs part
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