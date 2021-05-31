package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.common.Repository;
import pt.up.fc.dcc.ssd.p2p.node.Id;

public class ItemsRepo implements Repository {

    @Override
    public boolean containsKey(Id key) {
        // TODO: Ana
        return false;
    }

    @Override
    public byte[] get(Id key) {
        // TODO: Ana
        return new byte[0];
    }

    @Override
    public boolean put(Id key, byte[] byteArray) {
        // TODO: Ana
        return false;
    }

    /**
     * Bids on an item in this repo
     *
     * @param itemId the Id of the item to bid on
     * @param bid    the bid amount
     * @return true if the seller accepts the bid, false otherwise
     */
    public boolean bid(Id itemId, float bid) {
        // TODO: Ana, descobrir qual a melhor maneira de perguntar ao vendedor se aceita
        return true;
    }
}
