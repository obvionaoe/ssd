package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;

import javax.net.ssl.SSLException;
import java.security.*;

public class MinerNode {
    public KademliaNode kademlia;
    public Blockchain blockchain;
    public BlockchainRepo blockchainRepo;

    public MinerNode(Blockchain blockchain ) throws SSLException, NoSuchAlgorithmException {
        this.blockchain = blockchain;
        blockchainRepo = new BlockchainRepo();

        kademlia = KademliaNode
            .newBuilder()
            .build();
    }

    public void Mine(){
        System.out.println("Waiting for bid to occur... ");
        // TODO:
    }

}
