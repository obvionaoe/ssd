package pt.up.fc.dcc.ssd.p2p;

import pt.up.fc.dcc.ssd.p2p.common.util.Observable;
import pt.up.fc.dcc.ssd.p2p.common.util.Observer;

import java.util.ArrayList;
import java.util.List;

public class ObserverPatternMockup {
    static class ObserverClass implements Observer {
        String bid;

        @Override
        public void update(String bid) {
            this.bid = bid;
            System.out.println("do stuff");
        }
    }

    static class ObservableClass implements Observable {
        List<Observer> observers = new ArrayList<>();
        String bid;

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

    public static void main(String[] args) {
        ObserverClass observer = new ObserverClass();
        ObservableClass observable = new ObservableClass();

        observable.registerObserver(observer);

        observable.setBid("example");

        System.out.println(observer.bid);
    }
}
