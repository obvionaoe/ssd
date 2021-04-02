package pt.up.fc.dcc.ssd.p2p.rt;

import pt.up.fc.dcc.ssd.p2p.node.ID;
import pt.up.fc.dcc.ssd.p2p.Config;

import java.util.ArrayList;

public class RoutingTable extends ArrayList<KBucket> {

    public RoutingTable() {
        super(Config.ID_N_BITS);
    }

    private static long distance(ID x, ID y) {
        ID clone = (ID) x.clone();
        clone.xor(y);
        System.out.println(clone.toBinaryString());
        return clone.toLong();
    }

    public void update() {

    }
}
