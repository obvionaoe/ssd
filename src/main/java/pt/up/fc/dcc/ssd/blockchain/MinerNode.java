package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.p2p.node.Id;
import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;

import javax.net.ssl.SSLException;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;

public class MinerNode {
    public KademliaNode kademlia;
    public TransactionRepo transactionRepo;

    public MinerNode() throws SSLException, NoSuchAlgorithmException {
        transactionRepo = new TransactionRepo();

        kademlia = KademliaNode
            .newBuilder()
            .build();
    }

    public void Mine(Transaction transaction){
        Blockchain blockchain = kademlia.getBlockchain();
        Block prev = blockchain.latestBlock();
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);


        Block newBlock = new Block(
            prev.index,
            System.currentTimeMillis(),
            prev.previousHash,
            transactions
        );

        newBlock.addTransaction(transaction);
        blockchain.addBlock(newBlock);

        System.out.println("Successful mining! Going to share the mined block");

        kademlia.gossip(
                new Id(),
                toByteArray(newBlock)
        );
    }

}
