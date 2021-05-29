package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.common.Pair;
import pt.up.fc.dcc.ssd.p2p.grpc.DataType;
import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Logger;

import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;
import static pt.up.fc.dcc.ssd.common.Serializable.toObject;

public class Client {
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private static ClientNode clientNode;
    private static Pair<Boolean, String> isBidValid(String bid, Id topic){
        String currentBid = (String) toObject(clientNode.bidsRepo.get(topic));

        return Pair.pair((Float.parseFloat(bid) > Float.parseFloat(currentBid)), currentBid);
    }

    public static void main(String[] args) throws Exception {

        if (args.length >= 1) {
            System.err.println("ERROR: Please provide a topic");
            return;
        }

        // Buy or sell
        String operation = args[0];

        Id topic = Id.idFromData(args[1].getBytes(StandardCharsets.UTF_8));

        String item = args[2];

        String bid = args[3];

        Pair<Boolean, String> isValid = isBidValid(bid, topic);

        if(!isValid.first()) {
            System.out.println("Sorry, current bid is higher: " + isValid.second());
            System.exit(0);
        }

        clientNode = new ClientNode(operation, topic, bid, item);

        clientNode.kademlia.start();

        if (operation == "SELL") {
            clientNode.kademlia.store(
                    topic, toByteArray(clientNode.item), DataType.TOPIC
            );

        } else if (operation == "BUY") {
            ClientItem sellerID = (ClientItem) toObject(
                    clientNode.kademlia.findValue(topic, DataType.TOPIC)
            );
        }
        else
            throw new Exception("Role not found!");

        while(true){
            // Biding...
            System.out.println("Let the biding begin!");

            Scanner scan = new Scanner(System.in);
            String newBid = scan.nextLine();
            isValid = isBidValid(newBid, topic);
            if(!(boolean)isValid.first()){
                System.out.println("Sorry, current bid is higher: " + isValid.second());
            }

        }

    }
}
