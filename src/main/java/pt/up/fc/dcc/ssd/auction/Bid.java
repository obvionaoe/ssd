package pt.up.fc.dcc.ssd.auction;


import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.sql.Timestamp;
public class Bid {
    private long timestamp;
    private String bid;
    private Id topic;

    Bid(String bid, Id topic){
        this.bid = bid;
        timestamp = new Timestamp(System.currentTimeMillis()).getTime();
        this.topic = topic;
    }
}
