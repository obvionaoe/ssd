package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.common.Observable;

public class Blockchain implements Observable {

    public void addBlock(Block block) {
        notifyObservers();
    }
}
