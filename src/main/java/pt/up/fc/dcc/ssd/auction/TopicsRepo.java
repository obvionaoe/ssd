package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.common.Observer;

import java.util.ArrayList;
import java.util.List;

public class TopicsRepo implements Observer {
    String topic;

    @Override
    public void update(String topic) {
        this.topic = topic;
        System.out.println("New topic: " + topic);
    }
}
