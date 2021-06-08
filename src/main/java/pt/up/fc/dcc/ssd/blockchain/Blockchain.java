package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.auction.ClientNode;
import pt.up.fc.dcc.ssd.blockchain.transactions.Transaction;
import pt.up.fc.dcc.ssd.blockchain.transactions.TransactionOutput;

import javax.net.ssl.SSLException;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

        Transaction genesisTransaction = new Transaction(genesis.pbk, genesis.pbk, 100f, null);
        genesisTransaction.generateSignature(genesis.pvk);
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId));

        UnspentTransactions.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        System.out.println("Creating and Mining Genesis block... ");
        Block genesisBlock = new Block(0, System.currentTimeMillis(), "0", new ArrayList<>());
        genesisBlock.addTransaction(genesisTransaction);

        addBlock(genesisBlock);
    }

    // For development and demonstration reasons we can give a buyer money from genesis
    public Block MakeGenesisBuyer(ClientNode buyerGenesis, Blockchain prevBlockchain){
        Transaction genesisTransaction = new Transaction(genesis.pbk, buyerGenesis.pbk, 100f, null);
        genesisTransaction.generateSignature(genesis.pvk);
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId));

        UnspentTransactions.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        System.out.println("Giving 100.0 to genesis buyer ;) ");
        Block prev = prevBlockchain.latestBlock();
        Block genesisBlock = new Block(prev.getIndex(), System.currentTimeMillis(), prev.previousHash, new ArrayList<>());
        genesisBlock.addTransaction(genesisTransaction);
        prevBlockchain.addBlock(genesisBlock);

        buyerGenesis.kademlia.setBlockchain(prevBlockchain);

        return genesisBlock;
    }


    //CREATE
    /*public Block newBlock(String data) {
        Block latestBlock = latestBlock();
        return new Block(latestBlock.getIndex() + 1, System.currentTimeMillis(),
            latestBlock.getHash(), data);
    }*/

    //ADD e MINING
    public void addBlock(Block a) {
        if (a != null) {
            a.mineBlock(difficulty);
            blockchain.add(a);
        }
    }

    //LAST
    public Block latestBlock() {
        return blockchain.get(blockchain.size() - 1);
    }

    // TODO: use this
    public boolean isValidNewBlock(Block newBlock, Block previousBlock) {
        Block currentBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        //loop through blockchain to check hashes:
        for(int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            //compare registered hash and calculated hash:
            if(!currentBlock.hash.equals(currentBlock.calculateHash(currentBlock)) ){
                System.out.println("Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
            //check if hash is solved
            if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
                System.out.println("This block hasn't been mined");
                return false;
            }
        }
        return true;
    }



    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Block block : blockchain) {
            builder.append(block).append("\n");
        }

        return builder.toString();
    }

}