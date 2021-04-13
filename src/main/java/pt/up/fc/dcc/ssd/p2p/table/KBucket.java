package pt.up.fc.dcc.ssd.p2p.table;

import pt.up.fc.dcc.ssd.p2p.conn.ConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.node.ID;
import pt.up.fc.dcc.ssd.p2p.table.exceptions.InvalidDistanceException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import static pt.up.fc.dcc.ssd.p2p.Config.MAX_BUCKET_SIZE;
import static pt.up.fc.dcc.ssd.p2p.Config.MAX_DISTANCE;

public class KBucket {
    private final ArrayList<ID> nodeIds;
    private final HashMap<ID, ConnectionInfo> nodeMap;

    public KBucket() {
        this.nodeIds = new ArrayList<>(MAX_BUCKET_SIZE);
        this.nodeMap = new HashMap<>();
    }

    // needs robustness
    protected boolean update(ID id, ConnectionInfo connectionInfo) {
        if (!nodeIds.remove(id)) {
            if (nodeIds.size() >= MAX_BUCKET_SIZE) {
                return false;
            } else {
                nodeIds.add(id);
                nodeMap.put(id, connectionInfo);
                return true;
            }
        }

        nodeIds.add(id);
        return true;
    }

    // needs robustness
    protected void remove(ID id) {
        nodeIds.remove(id);
        nodeMap.remove(id);
    }

    protected boolean contains(ID id) {
        return nodeMap.containsKey(id);
    }

    protected static BigInteger distance(ID x, ID y) throws InvalidDistanceException {
        ID clone = (ID) x.clone();
        clone.xor(y);

        BigInteger result = new BigInteger(clone.toByteArray());
        if (result.compareTo(BigInteger.ZERO) < 0 || result.compareTo(MAX_DISTANCE) > 0)
            throw new InvalidDistanceException("The distance to the given ID is invalid");

        return result;
    }

    private static ConnectionInfo distancedConnectionInfo(ConnectionInfo connectionInfo, ID destId) {
        try {
            connectionInfo.setDistance(distance(connectionInfo.getId(), destId));
        } catch (InvalidDistanceException e) {
            return null;
        }

        return connectionInfo;
    }

    protected ArrayList<ConnectionInfo> get(ID destId) {
        ArrayList<ConnectionInfo> connectionInfos = new ArrayList<>();

        for (ID id : nodeIds) {
            ConnectionInfo distancedConnectionInfo = distancedConnectionInfo(nodeMap.get(id), destId);
            if (distancedConnectionInfo == null) continue;

            connectionInfos.add(distancedConnectionInfo);
        }

        return connectionInfos;
    }

    protected ConnectionInfo getNodeConnectionInfo(ID id) {
        return nodeMap.get(id);
    }

    @Override
    public String toString() {
        return "KBucket{\n" +
                "nodeIds=" + nodeIds +
                ",\nnodeMap=" + nodeMap +
                '}';
    }
}
