package pt.up.fc.dcc.ssd.auction;

public class BidsRepo implements Observer {
    String bid;

    @Override
    public void update(String bid) {
        this.bid = bid;
        System.out.println("New bid: " + bid);
    }
}
