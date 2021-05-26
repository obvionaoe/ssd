package pt.up.fc.dcc.ssd.blockchain;

import java.util.*;

public class Blockchain {

    public ArrayList<Block> blockchain=new ArrayList<Block>();
    public int difficulty = 1;

    public static final float minimumTransaction = 0.1f;
    public static Map<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
    public static Transaction genesisTransaction;

    //wallets
    //Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

    public Blockchain(int difficulty){
        this.difficulty = difficulty;
        blockchain = new ArrayList<>();

        Block genesis = new Block(0, System.currentTimeMillis(), null, "Genesis");
        genesis.mineBlock(difficulty);
        blockchain.add(genesis);
    }


    //CREATE
    public Block newBlock(String data) {
        Block latestBlock = latestBlock();
        return new Block(latestBlock.getIndex() + 1, System.currentTimeMillis(),
                latestBlock.getHash(), data);
    }

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

    /*
     //Validate genesis block -> um quitos inútil
    public boolean isGenesisValid() {
        Block Genesis = blockchain.get(0);

        if (Genesis.getIndex() != 0) {
            return false;
        }

        if (Genesis.getPreviousHash() != null) {
            return false;
        }

        if (Genesis.getHash() == null ||
                !Block.calculateHash(Genesis).equals(Genesis.getHash())) {
            return false;
        }

        return true;
    }
    */


//usa POW
    public boolean isValidNewBlock(Block newBlock, Block previousBlock) {
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
    }


    //prof disse que não era preciso
    /*
    public boolean isBlockChainValid() {
        if (!isGenesisValid()) {
            return false;
        }

        for (int i = 1; i < blockchain.size(); i++) {
            Block currentBlock = blockchain.get(i);
            Block previousBlock = blockchain.get(i - 1);

            if (!isValidNewBlock(currentBlock, previousBlock)) {
                return false;
            }
        }

        return true;
    }

     */

    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Block block : blockchain) {
            builder.append(block).append("\n");
        }

        return builder.toString();
    }

}