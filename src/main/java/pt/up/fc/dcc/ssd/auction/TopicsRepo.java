package pt.up.fc.dcc.ssd.auction;

public class TopicsRepo implements Observer {
    String topic;

    @Override
    public void update(String topic) {
        this.topic = topic;
        System.out.println("New topic: " + topic);
    }
}
