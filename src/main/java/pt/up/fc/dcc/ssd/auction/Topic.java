package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.p2p.node.Id;

public class Topic {
    private Id topic;
    private String item;

    Topic(Id topic, String item){
        this.topic = topic;
        this.item = item;
    }
}
