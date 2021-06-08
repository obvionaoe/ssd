package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.auction.ClientNode;
import pt.up.fc.dcc.ssd.blockchain.transactions.Transaction;
import pt.up.fc.dcc.ssd.blockchain.transactions.TransactionOutput;
import pt.up.fc.dcc.ssd.p2p.grpc.DataType;

import javax.net.ssl.SSLException;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;

public class Blockchain implements Serializable {

    public ArrayList<Block> blockchain = new ArrayList<Block>();
    public int difficulty = 1;

    public static final float minimumTransaction = 0.1f;
    public static Map<String, TransactionOutput> UnspentTransactions = new HashMap<String, TransactionOutput>();
    public static Transaction genesisTransaction;
    ClientNode genesis = new ClientNode("Genesis");

    public Blockchain(int difficulty) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException, SSLException {
        this.difficulty = difficulty;
        blockchain = new ArrayList<>();

        genesisTransaction = new Transaction(genesis.pbk, genesis.pbk, 100f, null);
        genesisTransaction.generateSignature(genesis.pvk);
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId));

        UnspentTransactions.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        //System.out.println("Creating and Mining Genesis block... ");
        Block genesisBlock = new Block(0, System.currentTimeMillis(), "0", new ArrayList<>());
        genesisBlock.addTransaction(genesisTransaction);

        addBlock(genesisBlock);

        genesis.kademlia.gossip(toByteArray(genesisBlock), DataType.BLOCK);
    }

    // For development and demonstration reasons we can give a buyer money from genesis
    public Block MakeGenesisBuyer(ClientNode buyerGenesis, Blockchain prevBlockchain){
        Transaction genesisTransaction = new Transaction(genesis.pbk, buyerGenesis.pbk, 100f, null);
        genesisTransaction.generateSignature(genesis.pvk);
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId));

        UnspentTransactions.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        //System.out.println("Giving 100.0 to genesis buyer ");
        Block prev = prevBlockchain.latestBlock();
        Block genesisBlock = new Block(prev.getIndex(), System.currentTimeMillis(), prev.previousHash, new ArrayList<>());
        genesisBlock.addTransaction(genesisTransaction);
        prevBlockchain.addBlock(genesisBlock);

        buyerGenesis.kademlia.setBlockchain(prevBlockchain);

        return genesisBlock;
    }

    //ADD e MINING
    public void addBlock(Block a) {
        if (a != null) {
            a.mineBlock(difficulty);
            blockchain.add(a);
        }
    }

    public Block latestBlock() {
        return blockchain.get(blockchain.size() - 1);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Block block : blockchain) {
            builder.append(block).append("\n");
        }

        return builder.toString();
    }

}