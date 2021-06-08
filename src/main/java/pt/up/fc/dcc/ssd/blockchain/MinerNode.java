package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.p2p.node.Id;
import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;

import javax.net.ssl.SSLException;
import java.security.*;
import java.util.ArrayList;

import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;

public class MinerNode {
    public KademliaNode kademlia;
    public Blockchain blockchain;
    public TransactionRepo transactionRepo;

    public MinerNode() throws SSLException, NoSuchAlgorithmException {
        transactionRepo = new TransactionRepo();

        kademlia = KademliaNode
            .newBuilder()
            .build();
    }

    public void Mine(Transaction transaction){
        Block prev = blockchain.latestBlock();

        Block newBlock = new Block(
            prev.index,
            System.currentTimeMillis(),
            prev.previousHash,
            prev.transactions
        );

        newBlock.addTransaction(transaction);
        blockchain.addBlock(newBlock);


        // TODO: é assim?
        kademlia.store(
            new Id(), // TODO: ID??
            toByteArray(blockchain)
        );
    }

}
