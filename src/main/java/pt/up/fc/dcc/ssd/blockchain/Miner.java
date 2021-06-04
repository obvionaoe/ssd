package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.auction.Client;
import pt.up.fc.dcc.ssd.auction.ClientNode;
import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.util.logging.Logger;

import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;

public class Miner {

    private static final Logger logger = Logger.getLogger(Client.class.getName());


    public static void main(String[] args) throws Exception {

        if (args.length > 0) {
            if (args[0] == "GENESIS") {
                Blockchain blockchain = new Blockchain(0);

                MinerNode minerNode = new MinerNode(blockchain);
                minerNode.kademlia.store(
                        new Id(), // TODO: ID??
                        toByteArray(minerNode.blockchain)
                        );
                minerNode.blockchainRepo.put(new Id(), toByteArray(minerNode.blockchain) );
                // TODO: minerNode.Mine();
            }
        }else{
            // TODO: find blockchain through bootstrap
        }

    }

}
