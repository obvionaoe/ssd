package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;

import javax.net.ssl.SSLException;

public class MinerNode {
    public KademliaNode kademlia;
    public Blockchain blockchain;

    public MinerNode() throws SSLException {

        // TODO: Add blockchain
        kademlia = KademliaNode
            .newBuilder()
            .build();
    }

}
