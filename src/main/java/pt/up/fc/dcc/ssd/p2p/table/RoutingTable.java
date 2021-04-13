package pt.up.fc.dcc.ssd.p2p.table;

import pt.up.fc.dcc.ssd.p2p.conn.ConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.node.ID;
import pt.up.fc.dcc.ssd.p2p.table.exceptions.InvalidDistanceException;
import pt.up.fc.dcc.ssd.p2p.table.exceptions.RoutingTableException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Logger;

import static pt.up.fc.dcc.ssd.p2p.Config.ID_N_BITS;
import static pt.up.fc.dcc.ssd.p2p.Config.MAX_BUCKET_SIZE;

// is this a tree? I dont think so my dude
public class RoutingTable {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final ID nodeId;
    private final ArrayList<KBucket> buckets;

    public RoutingTable(ID nodeId) {
        this.nodeId = nodeId;
        this.buckets = new ArrayList<>(ID_N_BITS);
        for (int i = 0; i < ID_N_BITS; i++) {
            buckets.add(new KBucket());
        }
    }

    public boolean update(ID id, ConnectionInfo connectionInfo) {
        try {
            if (connectionInfo.getDistance() == null)
                connectionInfo.setDistance(distance(nodeId, id));
            return buckets.get(findBucket(id)).update(id, connectionInfo);
        } catch (RoutingTableException | InvalidDistanceException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    public void remove(ID id) throws RoutingTableException {
        buckets.get(findBucket(id)).remove(id);
    }

    public static BigInteger distance(ID x, ID y) throws InvalidDistanceException {
        return KBucket.distance(x, y);
    }

    private int findBucket(ID destId) throws RoutingTableException {
        BigInteger distance;

        if (nodeId.toString().equals(destId.toString())) throw new RoutingTableException("A node can't call itself");

        try {
            distance = distance(nodeId, destId);
        } catch (InvalidDistanceException e) {
            throw new RoutingTableException("Invalid distance", e.getCause());
        }

        int i;
        for (i = 0; i < ID_N_BITS; i++) {
            BigInteger upperBound = BigDecimal.valueOf(Math.pow(2, ID_N_BITS - i)).toBigInteger();
            BigInteger lowerBound = BigDecimal.valueOf(Math.pow(2, ID_N_BITS - i - 1)).toBigInteger();

            if (distance.compareTo(upperBound) < 0 && distance.compareTo(lowerBound) >= 0) {
                break;
            }
        }

        return i;
    }

    public boolean contains(ID id) {
        try {
            return buckets.get(findBucket(id)).contains(id);
        } catch (RoutingTableException e) {
            return false;
        }
    }

    public ArrayList<ConnectionInfo> findClosest(ID destId) throws RoutingTableException {
        int bucketPos = findBucket(destId);
        ArrayList<ConnectionInfo> connectionInfos = new ArrayList<>(buckets.get(bucketPos).get(destId));
        // is this the correct way?
        for (int i = 0; connectionInfos.size() < MAX_BUCKET_SIZE
                && ((bucketPos + i) < ID_N_BITS || (bucketPos - i) >= 0); i++) {
            if (bucketPos - i >= 0) {
                connectionInfos.addAll(buckets.get(bucketPos - i).get(destId));
            }

            if (bucketPos + i < ID_N_BITS) {
                connectionInfos.addAll(buckets.get(bucketPos + i).get(destId));
            }
        }

        connectionInfos.sort(Comparator.comparing(ConnectionInfo::getDistance));

        if (connectionInfos.size() > MAX_BUCKET_SIZE) {
            connectionInfos.subList(MAX_BUCKET_SIZE, connectionInfos.size()).clear();
        }

        return connectionInfos;
    }

    @Override
    public String toString() {
        return "RoutingTable{\n" +
                "nodeId=" + nodeId +
                ",\nbuckets=" + buckets +
                "\n}";
    }
}
