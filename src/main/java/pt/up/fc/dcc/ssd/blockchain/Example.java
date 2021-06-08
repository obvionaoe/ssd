package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.auction.ClientNode;
import pt.up.fc.dcc.ssd.blockchain.transactions.Transaction;
import pt.up.fc.dcc.ssd.blockchain.transactions.TransactionOutput;

import javax.net.ssl.SSLException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.ArrayList;

public class Example {

    public static void main(String[] args) throws SSLException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        ClientNode seller = new ClientNode("Seller");
        ClientNode buyer = new ClientNode("Buyer");
        ClientNode genesis = new ClientNode("Genesis");

        // GENESIS
        Transaction genesisTransaction = new Transaction(genesis.pbk, buyer.pbk, 100f, null);
        genesisTransaction.generateSignature(genesis.pvk);
        genesisTransaction.transactionId = "0"; //manually set the transaction id
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output

        Blockchain blockchain = new Blockchain(3);
        blockchain.UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.

        System.out.println("Creating and Mining Genesis block... ");
        Block genesisBlock = new Block(0, System.currentTimeMillis(), "0", new ArrayList<>());
        genesisBlock.addTransaction(genesisTransaction);

        blockchain.addBlock(genesisBlock);

        // Check Buyer balance
        Block block1 = new Block(
                genesisBlock.index,
                System.currentTimeMillis(),
                genesisBlock.hash,
                new ArrayList<>());
        System.out.println("\nBuyer's balance is: " + buyer.getBalance());


        System.out.println("\nbuyer is Attempting to send funds (40) to seller...");
        block1.addTransaction(buyer.sendFunds(seller.pbk, 40f));
        blockchain.addBlock(block1);
        System.out.println("\nbuyer's balance is: " + buyer.getBalance());
        System.out.println("seller's balance is: " + seller.getBalance());
    }
}
