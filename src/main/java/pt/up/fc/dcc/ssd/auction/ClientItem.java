package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.io.*;

public class ClientItem implements Serializable {
    public Id topic;
    public float bid;
    public String item;
    public Id clientId;
    public final long serialVersionUID = 1L;


    ClientItem(Id clientId, Id topic, float bid, String item){
        this.clientId = clientId;
        this.topic = topic;
        this.bid = bid;
        this.item = item;
    }
}

