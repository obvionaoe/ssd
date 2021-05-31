package pt.up.fc.dcc.ssd.p2p.routing;

import pt.up.fc.dcc.ssd.p2p.Config;
import pt.up.fc.dcc.ssd.p2p.conn.ConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.conn.DistancedConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.node.Id;
import pt.up.fc.dcc.ssd.p2p.routing.exceptions.InvalidDistanceException;
import pt.up.fc.dcc.ssd.p2p.routing.exceptions.RoutingTableException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import static pt.up.fc.dcc.ssd.p2p.Config.ID_N_BITS;
import static pt.up.fc.dcc.ssd.p2p.Config.MAX_BUCKET_SIZE;

// is this a tree? I dont think so my dude
// FIXME: duplicate nodes appearing in routing table sigh
public class RoutingTable {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final Id nodeId;
    private final List<KBucket> buckets;

    public RoutingTable(Id nodeId) {
        this.nodeId = nodeId;
        this.buckets = new ArrayList<>(ID_N_BITS);
        for (int i = 0; i < ID_N_BITS; i++) {
            buckets.add(new KBucket());
        }
    }

    public boolean update(Id destinationId, ConnectionInfo connectionInfo) {
        try {
            return buckets.get(findBucket(destinationId)).update(destinationId, connectionInfo);
        } catch (RoutingTableException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    public boolean update(DistancedConnectionInfo info) {
        return update(info.getId(), info.getConnectionInfo());
    }

    public void update(List<DistancedConnectionInfo> infos) {
        infos.forEach(this::update);
    }

    public void remove(Id id) throws RoutingTableException {
        buckets.get(findBucket(id)).remove(id);
    }

    public static BigInteger distance(Id x, Id y) throws InvalidDistanceException {
        return KBucket.distance(x, y);
    }

    private int findBucket(Id destinationId) throws RoutingTableException {
        BigInteger distance;

        if (nodeId.toString().equals(destinationId.toString()))
            throw new RoutingTableException("A node can't call itself");

        try {
            distance = distance(nodeId, destinationId);
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

    // Is this needed? might be slower than other methods
    public boolean contains(Id id) {
        try {
            return buckets.get(findBucket(id)).contains(id);
        } catch (RoutingTableException e) {
            return false;
        }
    }

    /**
     * This function peruses the routing table until it has found k closest nodes to id,
     * (k={@value Config#MAX_BUCKET_SIZE}) or reached the end of the table.
     * The list will be empty if the routing table is empty
     *
     * @param id the ID the routing table wants to find
     * @return a list of connection infos, if the node is present in the routing table,
     * it will be the first element of that list
     */
    public List<DistancedConnectionInfo> findClosest(Id id) throws RoutingTableException {
        int bucketPos = findBucket(id);
        List<DistancedConnectionInfo> connectionInfos = new ArrayList<>(buckets.get(bucketPos).get(id));
        // is this the correct way? No, but works for small networks and for the current state of affairs
        for (int i = 0; connectionInfos.size() < MAX_BUCKET_SIZE
            && ((bucketPos + i) < ID_N_BITS || (bucketPos - i) >= 0); i++) {
            if (bucketPos - i >= 0) {
                connectionInfos.addAll(buckets.get(bucketPos - i).get(id));
            }

            if (bucketPos + i < ID_N_BITS) {
                connectionInfos.addAll(buckets.get(bucketPos + i).get(id));
            }
        }

        connectionInfos.sort(Comparator.comparing(DistancedConnectionInfo::getDistance));

        if (connectionInfos.size() > MAX_BUCKET_SIZE) {
            connectionInfos.subList(MAX_BUCKET_SIZE, connectionInfos.size()).clear();
        }

        return connectionInfos;
    }

    public List<DistancedConnectionInfo> getAll() throws RoutingTableException {
        List<DistancedConnectionInfo> resultList = new ArrayList<>();
        buckets.forEach(kBucket -> resultList.addAll(kBucket.getAll()));
        return resultList;
    }

    @Override
    public String toString() {
        return "RoutingTable{\n" +
            "nodeId=" + nodeId +
            ",\nbuckets=" + buckets +
            "\n}";
    }
}
