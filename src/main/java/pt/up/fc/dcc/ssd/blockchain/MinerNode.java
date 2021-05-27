package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;

public class MinerNode {
    public KademliaNode kademlia;
    public Blockchain blockchain;

    public MinerNode(){

        // TODO: Add blockchain
        kademlia = KademliaNode
                .newBuilder()
                .build();
    }

}
