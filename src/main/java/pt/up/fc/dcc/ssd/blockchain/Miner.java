package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.auction.Client;
import pt.up.fc.dcc.ssd.blockchain.transactions.Transaction;

import javax.net.ssl.SSLException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class Miner {

    private static final Logger logger = Logger.getLogger(Client.class.getName());


    public static void main(String[] args) throws Exception {

        MinerNode minerNode = null;
        try {
            minerNode = new MinerNode();
        } catch (SSLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

            minerNode.kademlia.start();
            minerNode.kademlia.bootstrap();

            while(true){
                // Make the while true loop less intense
                sleep(60000);
                if(!minerNode.kademlia.getTransactionRepo().transactionsList.isEmpty()){
                    Transaction newTransaction = minerNode.kademlia.getTransactionRepo().get();
                    minerNode.kademlia.getTransactionRepo().transactionsList.remove(newTransaction);
                    minerNode.mine(newTransaction);
                }
            }
    }
}
