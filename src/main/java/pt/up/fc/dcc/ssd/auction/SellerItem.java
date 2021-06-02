package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;

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

    //TODO: Is the clientId really necessary?
    public SellerItem(Id clientId, String topic, float bid, String item, PublicKey pbk) {
        this.SellerId = clientId;
        this.topicName = topic;
        this.topicId = Id.idFromData(topic.getBytes(StandardCharsets.UTF_8));
        this.bid = bid;
        this.itemName = item;
        this.itemId = Id.idFromData(item.getBytes(StandardCharsets.UTF_8));
        this.sellerPbk = pbk;
    }
}

