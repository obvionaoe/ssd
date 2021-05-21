
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;

public class Blockchain {

    public static ArrayList<Block> Blockchain;
    public int DIFFICULTY = 1;


    public Blockchain(){
        this.Blockchain= new ArrayList<Block>();
        //this.blockchain = new ArrayList<>();
    }

    //imprimir a blockchain

    //ADD e MINING
    public void addBlock(Block newBlock ) {
        if (newBlock != null) {
            newBlock.mineBlock(DIFFICULTY);
            Blockchain.add(newBlock);
        }
    }

    //LAST
    public Block lastBlock() {
        return Blockchain.get(Blockchain.size() - 1);
    }

    public Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String candidateBlock = new String(new char[difficulty]).replace('\0', '0');

        for(int i=1; i < Blockchain.size(); i++) {

            currentBlock = Blockchain.get(i);
            previousBlock = Blockchain.get(i-1);
            //compare registered hash and calculated hash:
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                System.out.println("Current Hashes are not equal, the current transaction's data has been altered");
                return false;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("Previous Hashes are not equal, the previous transaction's data has been altered");
                return false;
            }
            //check if hash is solved
            if(!currentBlock.hash.substring( 0, difficulty).equals(candidateBlock)) {
                System.out.println("This block hasn't been mined");
                return false;
            }
        }
        System.out.println("Blockchain is valid");
        System.out.println("Printing the blockchain...");
        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        return true;
    }

    //Creates a JSON representation of blockchain and prints it to a file.
    public String toJson() {
        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(Blockchain);
        try {
            FileWriter jsonFile = new FileWriter("blockchain.json");
            jsonFile.write(blockchainJson);
            jsonFile.close();
        } catch (IOException e) {
            System.err.println("Error: Cannot write blockchain to JSON");
        }
        return blockchainJson;
    }



}
