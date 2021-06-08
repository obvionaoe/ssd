package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.blockchain.transactions.Transaction;
import pt.up.fc.dcc.ssd.blockchain.transactions.TransactionOutput;
import pt.up.fc.dcc.ssd.blockchain.transactions.TransactionRepo;
import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;

import javax.net.ssl.SSLException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;
import static pt.up.fc.dcc.ssd.p2p.grpc.DataType.BLOCK;

public class MinerNode {
    public KademliaNode kademlia;
    public TransactionRepo transactionRepo;

    // Wallet
    private double wallet = 0.0;

    public MinerNode() throws SSLException, NoSuchAlgorithmException {
        transactionRepo = new TransactionRepo();

        kademlia = KademliaNode
            .newBuilder()
            .build();
    }

    public void mine(Transaction transaction) {
        // Transaction fee
        //double paymentForMiner = transaction.value*0.001;
        //transaction.value -= paymentForMiner;

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
        //wallet += paymentForMiner;

        kademlia.gossip(
            toByteArray(newBlock),
            BLOCK
        );
    }

}
