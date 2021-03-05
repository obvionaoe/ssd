import java.util.Date;

public class Block {

    public String hash;
    public String previousBlockHash;
    private String data;
    //public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private long timeStamp;
    private int nonce;

    //Construtor do Bloco
    public Block(String data, String previousBlockHash) {
        this.data = data;
        //this.transactions = transactions;
        this.previousBlockHash = previousBlockHash;
        this.timeStamp = new Date().getTime();
        this.nonce = nonce;
        this.hash = calculateHash();

    }

    //data to Hash
    public String calculateHash(){
        String newHash= Utils.applySecureHash( previousBlockHash 
                + toString(timeStamp)
                + toString(nonce)
                + data ); //merkleRoot
        return newHash;
    }


    //dificuldade igual ao número de zeros iniciais à esquerda da hash
    //merkleRoot de transaçoes? irá ser o candidateBlock
    public void mineBlock(int difficulty){
        String candidateBlock = new String(new char[difficulty]).replace('\0', '0');
        while(!hash.substring(0, difficulty).equals(candidateBlock)){
            nonce ++;
            hash = calculateHash();
        }

        System.out.println("Bloco minado:" + hash);
    }
    
}
