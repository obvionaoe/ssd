package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.auction.Client;

import java.util.logging.Logger;

public class Miner {

    private static final Logger logger = Logger.getLogger(Client.class.getName());

    private static void makeGenesis() {

    }

    public static void main(String[] args) throws Exception {

        if (args.length > 0) {
            if (args[0] == "GENESIS") {
                makeGenesis();
            }
        }

        while (true) {
            System.out.println("Waiting for transactions to occur... ");
        }
    }

}
