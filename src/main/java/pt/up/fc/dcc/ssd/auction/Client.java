package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.blockchain.Transaction;
import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;
import static pt.up.fc.dcc.ssd.common.Serializable.toObject;

public class Client {
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private static ClientNode clientNode;

    public static void main(String[] args) throws Exception {

        // Get Role
        System.out.println("Buyer or Seller?");
        Scanner scan = new Scanner(System.in);
        String operation = scan.nextLine();

        if (!operation.equals("Buyer") && !operation.equals("Seller")) {
            throw new Exception("Role " + operation + " not found! You can only be Buyer or Seller!");
        }

        clientNode = new ClientNode(operation);

        clientNode.kademlia.start();
        clientNode.kademlia.bootstrap();

        if (operation.equals("Seller")) {
            System.out.println("In what topic are you selling?");
            String topic = scan.nextLine();

            System.out.println("What item are you selling?");
            String item = scan.nextLine();

            System.out.println("What's the minimum price'?");
            float bid = Float.parseFloat(scan.nextLine());

            clientNode.setItem(
                topic,
                bid,
                item);

            // Add item associated with topic Id to the network
            clientNode.kademlia.store(
                clientNode.item.topicId,
                toByteArray(clientNode.item)
            );

            // Wait for biding
            System.out.println("Waiting for bids....");
            while(true){
            }

        } else {
            // TODO: List of topics
            System.out.println("From what topic do you want to buy?");
            String topic = scan.nextLine();
            Id topicId = Id.idFromData(topic.getBytes(StandardCharsets.UTF_8));

            // Get items in the network through the topic Id
            List<byte[]> itemsList =  clientNode.kademlia.findItems(
                topicId
            );

            for (byte[] item: itemsList) {
                SellerItem sellerItem = (SellerItem) toObject(item);
                System.out.println("Name: " + sellerItem.itemName + "\nCurrent bid: " + sellerItem.bid);
            }

            System.out.println("What item you want to buy?");
            String chosenItemName = scan.nextLine();

            System.out.println("What's your bid?");
            float bid = Float.parseFloat(scan.nextLine());

            // Find item and store it in clientNode
            for (byte[] item: itemsList) {
                SellerItem sellerItem = (SellerItem) toObject(item);
                if(sellerItem.itemName == chosenItemName){
                    clientNode.item = sellerItem;
                    break;
                }
            }

            if(clientNode.item == null){
                throw new Exception("Chosen item didn't match any item in the list");
            }

            boolean accepted  = clientNode.kademlia.bid(
                    clientNode.item.SellerId,
                    clientNode.item.itemId,
                    bid
            );

            if(accepted){
                Transaction transaction = new Transaction(
                        clientNode.pbk,
                        clientNode.item.sellerPbk,
                        bid,
                        null
                );

                transaction.processTransaction();
            }
        }
    }
}
