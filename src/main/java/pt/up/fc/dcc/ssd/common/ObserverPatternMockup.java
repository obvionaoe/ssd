package pt.up.fc.dcc.ssd.common;

import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;

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
        Blockchain observable = new Blockchain();

        observable.registerObserver(observer);

        KademliaNode node = KademliaNode.newBuilder().addBlockchainRepo(observable).build();

        observable.addBlock(new Block());

        System.out.println(observer.bid);
    }
}
