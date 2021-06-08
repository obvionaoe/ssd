package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.io.Serializable;
import java.security.PublicKey;

import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;

// Represents an item put by a seller
public class SellerItem implements Serializable {
    public String topicName;
    public Id topicId;
    public Id itemId;
    public String itemName;
    public Id SellerId;
    public float bid;
    public final long serialVersionUID = 1L;
    public PublicKey sellerPbk;

    public SellerItem(Id clientId, String topic, float bid, String item, PublicKey pbk) {
        this.SellerId = clientId;
        this.topicName = topic;
        this.topicId = Id.idFromData(toByteArray(topic));
        this.bid = bid;
        this.itemName = item;
        this.itemId = Id.idFromData(toByteArray(item));
        this.sellerPbk = pbk;
    }

    @Override
    public String toString() {
        return "SellerItem{" +
            "topicName='" + topicName + '\'' +
            ", topicId=" + topicId +
            ", itemId=" + itemId +
            ", itemName='" + itemName + '\'' +
            ", SellerId=" + SellerId +
            ", bid=" + bid +
            ", serialVersionUID=" + serialVersionUID +
            ", sellerPbk=" + sellerPbk +
            '}';
    }
}

