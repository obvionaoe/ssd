package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.blockchain.BlockchainRepo;
import pt.up.fc.dcc.ssd.p2p.node.Id;
import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;

import javax.net.ssl.SSLException;

import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;
public class ClientNode {
    public String role;
    public KademliaNode kademlia;
    public ClientItem item;
    public BidsRepo bidsRepo;
    public TopicsRepo topicsRepo;
    public BlockchainRepo blockchainRepo;

    public ClientNode(String role, Id topic, String bid, String item ) throws SSLException {
        bidsRepo = new BidsRepo();
        topicsRepo = new TopicsRepo();
        blockchainRepo = new BlockchainRepo();

        bidsRepo.put(topic, toByteArray(bid));
        topicsRepo.put(topic, toByteArray(item));

        kademlia = KademliaNode
            .newBuilder()
            .addBlockchainRepo(blockchainRepo)
            .addBidsRepo(bidsRepo)
            .addTopicRepo(topicsRepo)
            .build();
        this.role = role;
        this.item = new ClientItem(kademlia.getId(), topic , bid, item);
    }
}