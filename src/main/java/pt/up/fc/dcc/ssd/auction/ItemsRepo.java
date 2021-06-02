package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.common.Repository;
import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;

public class ItemsRepo implements Repository {
    Scanner scan = new Scanner(System.in);
    HashMap<Id, List<byte[]>> repo = new HashMap<>();

    @Override
    public boolean containsKey(Id key) {
        return repo.containsKey(key);
    }

    @Override
    public byte[] get(Id key) {
        if(repo.containsKey(key))
            return toByteArray(repo.get(key));
        return null;
    }

    @Override
    public boolean put(Id key, byte[] byteArray) {
        if(containsKey(key)){
            List<byte[]> prev = repo.get(key);
            prev.add(byteArray);
            repo.put(key, prev);
            return true;
        }else{
            List<byte[]> newTopic = Arrays.asList(byteArray);
            repo.put(key, newTopic);
            return true;
        }
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
