package pt.up.fc.dcc.ssd.blockchain.transactions;

import pt.up.fc.dcc.ssd.blockchain.Utils;

import java.io.Serializable;
import java.security.PublicKey;

public class TransactionOutput implements Serializable {
    public String id;
    public PublicKey recipient; // new owner of coins
    public float value; //amount of owned coins
    public String parentTransactionId; //id of the transaction this output was created in

    //Constructor
    public TransactionOutput(PublicKey recipient, float value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = Utils.applySecureHash(Utils.getStringFromKey(recipient) + Float.toString(value) + parentTransactionId);
    }

    //Check coin
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == recipient);
    }

}