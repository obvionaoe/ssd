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
    public ItemsRepo itemsRepo;
    public BlockchainRepo blockchainRepo;

    public ClientNode(String role) throws SSLException {

        kademlia = KademliaNode
            .newBuilder()
            .build();
        this.role = role;

    }

    public void setItem(Id topic, String bid, String item) {
        this.item = new ClientItem(kademlia.getId(), topic, Float.parseFloat(bid), item);
        itemsRepo.put(topic, toByteArray(item));

    }

    // TODO: seller communication
    public void bidTalk() {

    }
}