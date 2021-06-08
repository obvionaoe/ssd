package pt.up.fc.dcc.ssd.auction;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import pt.up.fc.dcc.ssd.blockchain.Block;
import pt.up.fc.dcc.ssd.blockchain.Blockchain;
import pt.up.fc.dcc.ssd.blockchain.transactions.Transaction;
import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;
import static pt.up.fc.dcc.ssd.common.Serializable.toObject;
import static pt.up.fc.dcc.ssd.p2p.grpc.DataType.BLOCK;
import static pt.up.fc.dcc.ssd.p2p.grpc.DataType.TRANSACTION;

public class Client {
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private static ClientNode clientNode;

    // TODO: Auction in loop
    // TODO: Exceptions handling
    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

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
            System.out.println(clientNode.item.topicId);
            // Add item associated with topic Id to the network
            clientNode.kademlia.store(
                clientNode.item.topicId,
                toByteArray(clientNode.item)
            );

            // Wait for biding
            System.out.println("Waiting for bids....");
            sleep(3_600_000);

        } else {
            if (args[0].equals("GENESIS")) {
                Block newBlock =
                    clientNode.kademlia.getBlockchain().MakeGenesisBuyer(clientNode, clientNode.kademlia.getBlockchain());
                clientNode.kademlia.gossip(new Id(), toByteArray(newBlock), BLOCK);
            }

            System.out.println("From what topic do you want to buy?");
            String topic = scan.nextLine();
            Id topicId = Id.idFromData(topic.getBytes(StandardCharsets.UTF_8));

            // Get items in the network through the topic Id
            List<byte[]> itemsList = clientNode.kademlia.findItems(
                topicId
            );

            for (byte[] item : itemsList) {
                SellerItem sellerItem = (SellerItem) toObject(item);
                System.out.println("Name: " + sellerItem.itemName + "\nCurrent bid: " + sellerItem.bid);
            }

            System.out.println("What item you want to buy?");
            String chosenItemName = scan.nextLine();

            System.out.println("What's your bid?");
            float bid = Float.parseFloat(scan.nextLine());

            // Find item and store it in clientNode
            for (byte[] item : itemsList) {
                SellerItem sellerItem = (SellerItem) toObject(item);
                if (sellerItem.itemName.equals(chosenItemName)) {
                    clientNode.item = sellerItem;
                    break;
                }
            }

            if (clientNode.item == null) {
                throw new Exception("Chosen item didn't match any item in the list");
            }

            boolean accepted = clientNode.kademlia.bid(
                clientNode.item.SellerId,
                clientNode.item.itemId,
                bid
            );

            if (accepted) {
                System.out.println("\nBuyer's balance is: " + clientNode.getBalance());
                System.out.println("\nBuyer is Attempting to send funds" + bid + " to Seller...");

                Transaction transaction = clientNode.sendFunds(clientNode.item.sellerPbk, bid);

                // Gossip da transação
                clientNode.kademlia.gossip(
                    new Id(),
                    toByteArray(transaction),
                    TRANSACTION
                );
            }
        }
    }
}
