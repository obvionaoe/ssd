import java.util.Date;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Block {

    private long timeStamp= new Date().getTime();
    public String previousHash;
    //this.merkleRootHash should hash equal merkleroot?
    public String hash;
    public String merkleRoot;
    //private String data;
    private int nonce;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();


    //Construtor do Bloco
    public Block( String previousHash,ArrayList<Transaction> transactions) {
        this.previousHash = previousHash;
        this.hash=calculateHash();

        //isto é necessário?
        this.timeStamp = timeStamp
        this.merkleRoot = merkleRoot;
        this.nonce=nonce;
        this.transactions = transactions; //data

    }

    public ArrayList<Transaction> getTransactions() {
        return this.transaction;
    }

    public String calculateHash() {
        return StringUtil.applySecureHash(previousHash
                + timeStamp
                + nonce
                + merkleRoot);
    }

    /*
    public String calculateHash() {
        String calculatehash = StringUtil.applySecureHash(
                previousHash + Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        merkleRoot
        );
        return calculatehash;
    }*/

    public void mineBlock(int difficulty){
        //nonce = 0;
        merkleRoot=StringUtil.getMerkleRoot(transactions);
        //while(!merkleRootHash.substring(0, difficulty).equals(target)) {
        String candidateBlock = new String(new char[difficulty]).replace('\0', '0');
        while(!hash.substring(0, difficulty).equals(candidateBlock)){
            nonce ++;
            hash = calculateHash();
        }

        System.out.println("Bloco minado:" + hash);
    }

    public boolean addTransaction(Transaction transaction) {
        if (transaction == null)
            return false;
        if (!previousHash.equals("0")) {
            if (!transaction.processTransaction()) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction successfully added to block");
        return true;
    }






    
}
