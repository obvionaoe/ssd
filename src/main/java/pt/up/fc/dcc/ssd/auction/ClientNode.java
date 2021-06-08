package pt.up.fc.dcc.ssd.auction;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import pt.up.fc.dcc.ssd.blockchain.Blockchain;
import pt.up.fc.dcc.ssd.blockchain.transactions.Transaction;
import pt.up.fc.dcc.ssd.blockchain.transactions.TransactionInput;
import pt.up.fc.dcc.ssd.blockchain.transactions.TransactionOutput;
import pt.up.fc.dcc.ssd.blockchain.transactions.TransactionRepo;
import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;

import javax.net.ssl.SSLException;
import java.io.Serializable;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;

public class ClientNode implements Serializable {
    public String role;
    public KademliaNode kademlia;
    public SellerItem item;
    public ItemsRepo itemsRepo = new ItemsRepo();

    // Wallet
    public PublicKey pbk;
    public PrivateKey pvk;
    public HashMap<String, TransactionOutput> UTXOs = new HashMap<>(); //only UTXOs owned by this wallet.


    public ClientNode(String role) throws SSLException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        // TODO: This is now in getRequest in kademlia
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
        keyGen.initialize(ecSpec, random);   //256 bytes provides an acceptable security level
        KeyPair kp = keyGen.generateKeyPair();

        pbk = kp.getPublic();
        pvk = kp.getPrivate();

        kademlia = KademliaNode
            .newBuilder()
            .build();
        this.role = role;

    }

    public void setItem(String topic, float bid, String item) {
        this.item = new SellerItem(kademlia.getId(), topic, bid, item, pbk);
        itemsRepo.put(this.item.topicId, toByteArray(this.item));
    }

    //returns balance and stores the UTXO's owned by this client in this.UTXOs
    public float getBalance() {
        Blockchain blockchain = kademlia.getBlockchain();
        float total = 0;
        //Blockchain blockchain = (Blockchain) toObject(blockchainRepo.get( ? ??));
        for (Map.Entry<String, TransactionOutput> item : blockchain.UnspentTransactions.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            if (UTXO.isMine(pbk)) { //if output belongs to me ( if coins belong to me )
                UTXOs.put(UTXO.id, UTXO); //add it to our list of unspent transactions.
                total += UTXO.value;
            }
        }
        return total;
    }

    //Generates and returns a new transaction from this wallet.
    public Transaction sendFunds(PublicKey _recipient, float value ) {
        if(getBalance() < value) { //gather balance and check funds.
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        //create array list of inputs
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if(total > value) break;
        }

        Transaction newTransaction = new Transaction(pbk, _recipient , value, inputs);
        newTransaction.generateSignature(pvk);

        for(TransactionInput input: inputs){
            UTXOs.remove(input.transactionOutputId);
        }
        return newTransaction;
    }
}