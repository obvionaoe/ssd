import java.util.Date;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Block {

    private int index;
    private long timeStamp; //creation date
    public String hash; //sha 256
    public String previousHash;
    public String merkleRoot;
    private String data;
    private int nonce; //used in mining
    //public ArrayList<Transaction> transactions = new ArrayList<Transaction>();

    //bitcoin wiki:
    //int transaction counter?
    //hashMerkleRoot

    //Construtor do Bloco
    public Block(int index, long timestamp , String previousHash,String data) {

        this.index = index;
        this.timeStamp = timestamp; //new Date().getTime();
        //this.transactions = transactions;
        this.previousHash = previousHash;
        this.merkleRoot = merkleRoot;
        this.data=data;

        nonce=0;
        hash=Block.calculateHash(this);
        merkleRoot=Block.calculateHash(); // ou   merkleRoot = StringUtil.getMerkleRoot(transactions); ou das transactions?

    }

    public int getIndex(){
        return index;
    }

    public long getTimeStamp(){
        return timeStamp;
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash(){
        return previousHash;
    }

    public String getData(){
        return data;
    }

    public String str() {
        return index + timeStamp + previousHash + data + nonce;
    }


    /*
    //data to Hash
    public String calculateHash(){
        String newHash= Utils.applySecureHash( previousBlockHash 
                + toString(timeStamp)
                + toString(nonce)
                + data ); //merkleRoot
        return newHash;




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



    } */






    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Block #").append(index)
                .append(" [previousHash : ").append(previousHash).append(", ")
                .append("timestamp : ").append(new Date(timeStamp)).append(", ")
                .append("data : ").append(data).append(", ")
                .append("hash : ").append(hash).append(", ")
                .append("merkleRoot : ").append(merkleRoot).
                .append("]");
        return builder.toString();
    }



    public static String calculateHash(Block block) {
        if (block != null) {
            MessageDigest digest = null;

            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                return null;
            }

            String txt = block.str();
            final byte bytes[] = digest.digest(txt.getBytes());
            final StringBuilder builder = new StringBuilder();

            for (final byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    builder.append('0');
                }

                builder.append(hex);
            }

            return builder.toString();
        }

        return null;
    }

    public void mineBlock(int difficulty) {
        nonce = 0;

        merkleRoot = StringUtil.getMerkleRoot(transactions);

        while (!getHash().substring(0,  difficulty).equals(Utils.zeros(difficulty))) {
            nonce++;
            hash = Block.calculateHash(this);
        }
    }


    
}
