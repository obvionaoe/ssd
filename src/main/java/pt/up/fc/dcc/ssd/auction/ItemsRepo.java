package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.common.Repository;
import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class ItemsRepo implements Repository {
    Scanner scan = new Scanner(System.in);
    HashMap<Id, List<byte[]>> repo = new HashMap<>();

    @Override
    public boolean containsKey(Id key) {
        System.out.println("contains?");
        return repo.containsKey(key);
    }

    public List<byte[]> get(Id key) {
        if (repo.containsKey(key)) {
            return repo.get(key);
        }
        return null;
    }

    @Override
    public boolean put(Id key, byte[] byteArray) {
        if (containsKey(key)) {
            List<byte[]> prev = repo.get(key);
            prev.add(byteArray);
            repo.put(key, prev);
        } else {
            System.out.println("in the repo");
            List<byte[]> newTopicList = new ArrayList<>();
            newTopicList.add(byteArray);
            repo.put(key, newTopicList);
        }
        return true;
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
        if (response.equals("Y")) {
            return true;
        } else if (response.equals("N")) {
            return false;
        }
        return true;
    }
}
