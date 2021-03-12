
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Blockchain {

    public static List<Block> blocks;
    public int DIFFICULTY = 1;


    public Blockchain(int DIFFICULTY){
        this.DIFFICULTY = DIFFICULTY;
        blocks = new ArrayList<>();

        Block a = new Block(0, System.currentTimeMillis(), null, "Genesis");
        a.mineBlock(DIFFICULTY);
        blocks.add(a);
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
            a.mineBlock(DIFFICULTY);
            blocks.add(a);
        }
    }

    //LAST
    public Block latestBlock() {
        return blocks.get(blocks.size() - 1);
    }

    //Validate genesis block
    public boolean isGenesisValid() {
        Block Genesis = blocks.get(0);

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

        return false;
    }


    public boolean isBlockChainValid() {
        if (!isGenesisValid()) {
            return false;
        }

        for (int i = 1; i < blocks.size(); i++) {
            Block currentBlock = blocks.get(i);
            Block previousBlock = blocks.get(i - 1);

            if (!isValidNewBlock(currentBlock, previousBlock)) {
                return false;
            }
        }

        return true;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Block block : blocks) {
            builder.append(block).append("\n");
        }

        return builder.toString();
    }






}
