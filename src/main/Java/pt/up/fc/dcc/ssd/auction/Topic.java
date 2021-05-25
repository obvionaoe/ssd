package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.common.Observable;
import pt.up.fc.dcc.ssd.common.Observer;

import java.util.ArrayList;
import java.util.List;

public class Topic implements Observable {

    private List<Observer> observers = new ArrayList<>();
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
        observers.forEach(observer -> observer.update(topic));
    }

    public void setTopic(String topic) {
        this.topic = topic;
        notifyObservers();
    }
}
