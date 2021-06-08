package pt.up.fc.dcc.ssd.blockchain.transactions;

import java.io.Serializable;

public class TransactionInput implements Serializable {
    public String transactionOutputId; //Reference to TransactionOutputs -> transactionId
    public TransactionOutput UTXO; //Contains the Unspent transaction output

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}