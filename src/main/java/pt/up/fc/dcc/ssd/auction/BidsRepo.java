package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.common.Observable;
import pt.up.fc.dcc.ssd.common.Observer;

import java.util.ArrayList;
import java.util.List;

public class BidsRepo implements Observer {
    String bid;

    @Override
    public void update(String bid) {
        this.bid = bid;
        System.out.println("New bid: " + bid);
    }
}
