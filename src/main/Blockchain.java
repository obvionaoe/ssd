
import java.util.ArrayList;
import java.util.HashMap;

public class Blockchain {

    public static ArrayList<Block> blockchain = new ArrayList<>();
    public int difficulty = 1;

    // initialTransaction -> hash de initialTransaction(é única?) -> blockGenesis
    public void createGenesis(){
        System.out.println("Primeiro bloco da blockchain criado");
        // Block genesis = new Block("0", genesisHash);
    }

    /* novaTransaction -> hash de novaTransction -> lista de transaction
    -> novoBloco(lista de transições, nounce, hashPrevious)

    public void createBlock(){
        //new transaction ++ transactions + nounce + previousHash
    }

    */

    public void addBlock(){

    }

    public void currentBlockHash(){

    }

    public void validateChain(){

    }





}
