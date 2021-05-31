package pt.up.fc.dcc.ssd.blockchain;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Transaction {

    public String transactionId; //Contains a hash of transaction
    public PublicKey sender; //Senders address/public key.
    public PublicKey recipient; //Recipients address/public key.
    public float value; //fee
    public byte[] signature; //assinatura do cheque

    public List<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    private static int transactionCount = 0;

    // Constructor:
    public Transaction(PublicKey from, PublicKey to, float value, List<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    public boolean processTransaction() {

        if (!verifySignature()) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        //Gathers transaction inputs (Making sure they are unspent):
        for (TransactionInput i : inputs) {
            i.UTXO = Blockchain.UTXOs.get(i.transactionOutputId);
        }

        //Checks if transaction is valid:
        if (getInputsValue() < Blockchain.minimumTransaction) {
            System.out.println("Transaction Inputs too small: " + getInputsValue());
            System.out.println("Please enter the amount greater than " + Blockchain.minimumTransaction);
            return false;
        }

        //Generate transaction outputs:
        float leftOver = getInputsValue() - value; //get value of inputs then the left over change:
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId)); //send value to recipient
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId)); //send the left over 'change' back to sender

        //Add outputs to Unspent list
        for (TransactionOutput o : outputs) {
            Blockchain.UTXOs.put(o.id, o);
        }

        //Remove transaction inputs from UTXO lists as spent:
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue; //if Transaction can't be found skip it
            Blockchain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    public float getInputsValue() {
        float total = 0;
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue; //if Transaction can't be found skip it
            total += i.UTXO.value;
        }
        return total;
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = Utils.getStringFromKey(sender) + Utils.getStringFromKey(recipient) + Float.toString(value);
        signature = Utils.applyECDSASig(privateKey, data);
    }

    public boolean verifySignature() {
        String data = Utils.getStringFromKey(sender) + Utils.getStringFromKey(recipient) + Float.toString(value);
        return Utils.verifyECDSASig(sender, data, signature);
    }

    public float getOutputsValue() {
        float total = 0;
        for (TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }

    private String calculateHash() {
        transactionCount++; //to avoid 2 identical transactions having the same hash
        return Utils.applySecureHash(
            Utils.getStringFromKey(sender) +
                Utils.getStringFromKey(recipient) +
                Float.toString(value) + transactionCount
        );
    }
}