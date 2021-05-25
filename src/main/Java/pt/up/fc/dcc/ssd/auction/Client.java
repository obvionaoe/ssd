package pt.up.fc.dcc.ssd.auction;
import pt.up.fc.dcc.ssd.p2p.node.Id;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.Scanner;
public class Client {
    private static final Logger logger = Logger.getLogger(Client.class.getName());

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.err.println("ERROR: Please provide a topic");
            return;
        }

        // Buy or sell
        String operation = args[0];

        Id topic = Id.idFromData(args[1].getBytes(StandardCharsets.UTF_8));

        String item = args[2];

        String bid = args[3];

        ClientNode clientNode = new ClientNode(operation, topic, bid, item);

        clientNode.kademlia.start();

        if (operation == "SELL")
            clientNode.kademlia.store(
                    topic, clientNode.item.toByteArray()
            );
        else if (operation == "BUY") {
            ClientItem sellerID = ClientItem.toObject(
                    clientNode.kademlia.findValue(topic)
            );
        }
        else
            throw new Exception("Role not found!");

        while(true){
            // Biding...
            System.out.println("Let the biding begin!");

            Scanner scan = new Scanner(System.in);
            String newBid = scan.nextLine();

            clientNode.kademlia.

        }

    }
}
