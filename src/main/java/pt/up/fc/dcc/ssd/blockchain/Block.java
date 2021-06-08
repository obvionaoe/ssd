package pt.up.fc.dcc.ssd.blockchain;

import java.util.ArrayList;
import java.util.List;

public class Block {

    public int index;
    public long timeStamp;
    public String hash;
    public String previousHash;
    public String merkleRoot;
    //private String data;
    public List<Transaction> transactions = new ArrayList<Transaction>();
    private int nonce;

    //Construtor do Bloco
    public Block(int index, long timestamp, String previousHash, List<Transaction> transactions) {

        this.index = index;
        this.timeStamp = timestamp; //new Date().getTime();
        this.previousHash = previousHash;
        this.transactions= transactions;
        this.nonce = 0;
        this.merkleRoot = merkleRoot;
        this.hash = calculateHash(this);
    }

    public int getIndex() {
        return index;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }


//    public String getData(){ return data; }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public void setMerkleRoot(String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    /*
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Block #").append(index)
                .append(" [previousHash : ").append(previousHash).append(", ")
                .append("timestamp : ").append(new Date(timeStamp)).append(", ")
                .append("data : ").append(data).append(", ")
                .append("hash : ").append(hash).append(", ")
                .append("merkleRoot : ").append(merkleRoot)
                .append("]");
        return builder.toString();
    }*/

    public String calculateHash(Block block) {
        return Utils.applySecureHash(previousHash
            + Long.toString(timeStamp)
            + Integer.toString(nonce)
            + merkleRoot);
    }

    public String mineBlock(int difficulty) {

        merkleRoot = Utils.getMerkleRoot(transactions);
        while (!getHash().substring(0, difficulty).equals(Utils.zeros(difficulty))) {
            nonce++;
            hash = calculateHash(this);
        }
        return hash;
    }

    public boolean addTransaction(Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        if ((!"0".equals(previousHash))) {
            if ((transaction.processTransaction() != true)) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }

}