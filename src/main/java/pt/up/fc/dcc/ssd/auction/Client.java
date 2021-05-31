package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Logger;

import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;

public class Client {
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private static ClientNode clientNode;

    public static void main(String[] args) throws Exception {


        // Show available topics
        System.out.println("Buyer or Seller?");
        Scanner scan = new Scanner(System.in);
        String operation = scan.nextLine();

        if (!operation.equals("Buyer") && !operation.equals("Seller")) {
            throw new Exception("Role " + operation + " not found! You can only be Buyer or Seller!");
        }

        clientNode = new ClientNode(operation);

        clientNode.kademlia.start();

        if (operation.equals("Seller")) {
            System.out.println("In what topic are you selling?");
            String topic = scan.nextLine();

            System.out.println("What item are you selling?");
            String item = scan.nextLine();

            System.out.println("What's the minimum price'?");
            float bid = Float.parseFloat(scan.nextLine());

            clientNode.setItem(
                Id.idFromData(topic.getBytes(StandardCharsets.UTF_8)),
                bid,
                item);

            clientNode.kademlia.store(
                Id.idFromData(topic.getBytes(StandardCharsets.UTF_8)),
                toByteArray(item)
            );

            while(true){

            }

        } else {
            System.out.println("From what topic do you want to buy?");
            String topic = scan.nextLine();

            clientNode.kademlia.findValue(
                Id.idFromData(topic.getBytes(StandardCharsets.UTF_8))
            );

            // TODO: Display items from topic

            System.out.println("What item you want to buy?");
            String item = scan.nextLine();
            Id itemId = Id.idFromData(topic.getBytes(StandardCharsets.UTF_8));


            System.out.println("What's your bid'?");
            float bid = Float.parseFloat(scan.nextLine());

            clientNode.setItem(
                itemId,
                bid,
                item);

            // Talk to seller
            Id sellerId =  Id.toObject(clientNode.kademlia.findValue(
                    itemId
            ));

            clientNode.kademlia.bid(sellerId, itemId, bid);

        }
    }
}
