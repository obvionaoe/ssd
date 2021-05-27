package pt.up.fc.dcc.ssd.auction;

import java.util.ArrayList;
import java.util.List;

public class Bid implements Observable {
    private List<Observer> observers = new ArrayList<>();
    private long timestamp;
    private String bid;
    private String topic;

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        observers.forEach(observer -> observer.update(bid));
    }

    public void setBid(String bid) {
        this.bid = bid;
        notifyObservers();
    }
}
