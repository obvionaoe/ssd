package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.blockchain.Transaction;
import pt.up.fc.dcc.ssd.common.Repository;
import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Scanner;

public class ItemsRepo implements Repository {
    Scanner scan = new Scanner(System.in);
    HashMap<Id, byte[]> repo = new HashMap<Id, byte[]>();

    @Override
    public boolean containsKey(Id key) {
        return repo.containsKey(key);
    }

    @Override
    public byte[] get(Id key) {
        return repo.get(key);
    }

    @Override
    public void put(Id key, byte[] byteArray) {
        repo.put(key, byteArray);
    }

    /**
     * Bids on an item in this repo
     *
     * @param itemId the Id of the item to bid on
     * @param bid    the bid amount
     * @return true if the seller accepts the bid, false otherwise
     */
    public boolean bid(Id itemId, float bid) {
        System.out.println("New bid for " + itemId + "\nBid: " + bid);
        System.out.println("Accept? (Y/N)");
        String response = scan.nextLine();
        if(response.equals("Y")){
            return true;
        }else if(response.equals("N")){
            return false;
        }
        return true;
    }
}
