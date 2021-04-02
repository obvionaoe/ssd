package pt.up.fc.dcc.ssd.p2p.rt;

import pt.up.fc.dcc.ssd.p2p.node.ID;

import java.util.ArrayList;
import java.util.HashMap;

public class KBucket {
    private ID id;
    private ArrayList<ID> nodeIds;
    private HashMap<ID, ConnectionInfo> nodeMap;

    public KBucket(ID id) {
        this.id = id;
        this.nodeIds = new ArrayList<>();
        this.nodeMap = new HashMap<>();
    }
}
