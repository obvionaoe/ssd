package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.auction.ClientNode;

import javax.net.ssl.SSLException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Blockchain {

    public ArrayList<Block> blockchain = new ArrayList<Block>();
    public int difficulty = 1;

    public static final float minimumTransaction = 0.1f;
    public static Map<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
    public static Transaction genesisTransaction;
    ClientNode genesis = new ClientNode("Genesis");

    public Blockchain(int difficulty) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException, SSLException {
        this.difficulty = difficulty;
        blockchain = new ArrayList<>();

        // TODO: figure this
        Transaction genesisTransaction = new Transaction(genesis.pbk, buyerGenesis.pbk, 100f, null);
        genesisTransaction.generateSignature(genesis.pvk);
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId));

        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        System.out.println("Creating and Mining Genesis block... ");
        Block genesisBlock = new Block(0, System.currentTimeMillis(), "0", new ArrayList<>());
        genesisBlock.addTransaction(genesisTransaction);

        addBlock(genesisBlock);
    }

    // For development and demonstration reasons we can give a buyer money from genesis
    public void MakeGenesisBuyer(ClientNode buyerGenesis){
        Transaction genesisTransaction = new Transaction(genesis.pbk, buyerGenesis.pbk, 100f, null);
        genesisTransaction.generateSignature(genesis.pvk);
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId));

        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        System.out.println("Creating and Mining Genesis block... ");
        Block genesisBlock = new Block(0, System.currentTimeMillis(), "0", new ArrayList<>());
        genesisBlock.addTransaction(genesisTransaction);

        addBlock(genesisBlock);
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




//usa POW
    /*public boolean isValidNewBlock(Block newBlock, Block previousBlock) {
        if (newBlock != null  &&  previousBlock != null) {
            if (previousBlock.getIndex() + 1 != newBlock.getIndex()) {
                return false;
            }

            if (newBlock.getPreviousHash() == null  ||
                    !newBlock.getPreviousHash().equals(previousBlock.getHash())) {
                return false;
            }

            if (newBlock.getHash() == null  ||
                    !Block.calculateHash(newBlock).equals(newBlock.getHash())) {
                return false;
            }

            return true;
        }

      //  return false;
    }*/



    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Block block : blockchain) {
            builder.append(block).append("\n");
        }

        return builder.toString();
    }

}