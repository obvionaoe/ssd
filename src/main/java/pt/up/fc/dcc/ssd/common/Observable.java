package pt.up.fc.dcc.ssd.common;

public interface Observable {
    void registerObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers();
}
