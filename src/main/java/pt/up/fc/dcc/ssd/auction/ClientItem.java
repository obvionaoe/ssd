package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.io.Serializable;
import java.security.PublicKey;

public class ClientItem implements Serializable {
    public Id topic;
    public float bid;
    public String item;
    public Id clientId;
    public final long serialVersionUID = 1L;
    public PublicKey pbk;


    ClientItem(Id clientId, Id topic, float bid, String item, PublicKey pbk) {
        this.clientId = clientId;
        this.topic = topic;
        this.bid = bid;
        this.item = item;
        this.pbk = pbk;
    }
}

