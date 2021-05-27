package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.p2p.node.Id;
import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;

public class ClientNode {
    public String role;
    public KademliaNode kademlia;
    public ClientItem item;

    public ClientNode(String role, Id topic, String bid, String item ) {
        BidsRepo bidObserver = new BidsRepo();
        Bid bidObservable = new Bid();
        bidObservable.registerObserver(bidObserver);
        bidObservable.setBid(bid);

        TopicsRepo topicObserver = new TopicsRepo();
        Topic topicObservable = new Topic();
        topicObservable.registerObserver(topicObserver);
        topicObservable.setTopic(topic.toString());

        kademlia = KademliaNode
            .newBuilder()
            .addBlockchainRepo(observable)
            .addBidsRepo(bidObserver)
            .addTopicRepo(topicObserver)
            .build();
        this.role = role;
        this.item = new ClientItem(kademlia.getId(), topic , bid, item);
    }
}