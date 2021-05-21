package pt.up.fc.dcc.ssd.p2p.common.util;

public interface Observable {
    void registerObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers();
}
