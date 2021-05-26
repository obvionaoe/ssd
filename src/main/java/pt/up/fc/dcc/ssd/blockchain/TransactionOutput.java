import java.security.*;

public class TransactionOutput {
    public String id;
    public PublicKey recipient; // new owner of coins
    public float value; //amount of owned coins
    public String parentTransactionId; //id of the transaction this output was created in

    //Constructor
    public TransactionOutput(PublicKey recipient, float value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = Utils.applySecureHash(Utils.getStringFromKey(recipient)+Float.toString(value)+parentTransactionId);
    }

    //Check coin
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == recipient);
    }

}