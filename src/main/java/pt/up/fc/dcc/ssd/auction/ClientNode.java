package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.blockchain.BlockchainRepo;
import pt.up.fc.dcc.ssd.p2p.node.Id;
import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;

import javax.net.ssl.SSLException;

import java.security.*;

import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;

public class ClientNode {
    public String role;
    public KademliaNode kademlia;
    public ClientItem item;
    public ItemsRepo itemsRepo;
    public BlockchainRepo blockchainRepo;
    public PublicKey pbk;
    public PrivateKey pvk;


    public ClientNode(String role) throws SSLException, NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        pbk = kp.getPublic();
        pvk = kp.getPrivate();

        kademlia = KademliaNode
            .newBuilder()
            .build();
        this.role = role;

    }

    public void setItem(Id topic, float bid, String item) {
        this.item = new ClientItem(kademlia.getId(), topic, bid, item, pbk);
        itemsRepo.put(topic, toByteArray(item));

    }
}