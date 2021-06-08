package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.auction.Client;
import pt.up.fc.dcc.ssd.auction.ClientNode;
import pt.up.fc.dcc.ssd.p2p.node.Id;

import javax.net.ssl.SSLException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;

public class Miner {

    private static final Logger logger = Logger.getLogger(Client.class.getName());


    public static void main(String[] args) throws Exception {

        MinerNode minerNode = null;
        try {
            minerNode = new MinerNode();
        } catch (SSLException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        minerNode.kademlia.start();
            minerNode.kademlia.bootstrap();

            while(true){
                System.out.println("Waiting for transaction to occur");
                if(!minerNode.transactionRepo.transactionsList.isEmpty()){
                    System.out.println("Received Transaction!!!");
                    Transaction newTransaction = minerNode.transactionRepo.transactionsList.get(0);
                    minerNode.transactionRepo.transactionsList.remove(0);
                    System.out.println("Time to mine it!");
                    minerNode.Mine(newTransaction);
                }
            }
    }
}
