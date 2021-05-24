package pt.up.fc.dcc.ssd.p2p.routing;

import pt.up.fc.dcc.ssd.p2p.conn.ConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.conn.DistancedConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.node.Id;
import pt.up.fc.dcc.ssd.p2p.routing.exceptions.InvalidDistanceException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static pt.up.fc.dcc.ssd.p2p.Config.MAX_BUCKET_SIZE;
import static pt.up.fc.dcc.ssd.p2p.Config.MAX_DISTANCE;

// TODO: implement replacement cache
public class KBucket {
    private final List<Id> nodeIds;
    private final ConcurrentHashMap<Id, ConnectionInfo> nodeMap;

    public KBucket() {
        this.nodeIds = new ArrayList<>(MAX_BUCKET_SIZE);
        this.nodeMap = new ConcurrentHashMap<>();
    }

    // TODO: needs robustness
    protected boolean update(Id id, ConnectionInfo connectionInfo) {
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

    // TODO: needs robustness, should return bool
    protected void remove(Id id) {
        nodeIds.remove(id);
        nodeMap.remove(id);
    }


    protected boolean contains(Id id) {
        return nodeMap.containsKey(id);
    }

    protected static BigInteger distance(Id x, Id y) throws InvalidDistanceException {
        Id clone = x.copy();
        clone.xor(y);

        BigInteger result = new BigInteger(clone.toBytes());
        if (result.compareTo(BigInteger.ZERO) < 0 || result.compareTo(MAX_DISTANCE) > 0)
            throw new InvalidDistanceException("The distance to the given ID is invalid");

        return result;
    }

    private static DistancedConnectionInfo distancedConnectionInfo(ConnectionInfo connectionInfo, Id destId) {
        try {
            return new DistancedConnectionInfo(connectionInfo, distance(connectionInfo.getId(), destId));
        } catch (InvalidDistanceException e) {
            return null;
        }
    }

    protected List<DistancedConnectionInfo> get(Id destId) {
        List<DistancedConnectionInfo> connectionInfos = new ArrayList<>();

        for (Id id : nodeIds) {
            DistancedConnectionInfo distancedConnectionInfo = distancedConnectionInfo(nodeMap.get(id), destId);
            if (distancedConnectionInfo == null) continue;

            connectionInfos.add(distancedConnectionInfo);
        }

        return connectionInfos;
    }

    protected ConnectionInfo getNodeConnectionInfo(Id id) {
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
