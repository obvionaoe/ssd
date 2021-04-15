package pt.up.fc.dcc.ssd.p2p.conn;

import pt.up.fc.dcc.ssd.p2p.grpc.GrpcConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.node.ID;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

public class DistancedConnectionInfo {
    private final ConnectionInfo connectionInfo;
    private BigInteger distance;

    public DistancedConnectionInfo(ConnectionInfo connectionInfo, BigInteger distance) {
        this.connectionInfo = connectionInfo;
        this.distance = distance;
    }

    public DistancedConnectionInfo(ID id, String address, int port, BigInteger distance) {
        this.connectionInfo = new ConnectionInfo(id, address, port);
        this.distance = distance;
    }

    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public ID getId() {
        return connectionInfo.getId();
    }

    public String getAddress() {
        return connectionInfo.getAddress();
    }

    public int getPort() {
        return connectionInfo.getPort();
    }

    public BigInteger getDistance() {
        return distance;
    }

    public void setDistance(BigInteger distance) {
        this.distance = distance;
    }

    public GrpcConnectionInfo toGrpcConnectionInfo() {
        GrpcConnectionInfo.Builder builder = GrpcConnectionInfo
                .newBuilder()
                .setId(getId().toBinaryString())
                .setAddress(getAddress())
                .setPort(getPort());

        if (distance != null) {
            builder.setDistance(distance.toString());
        }

        return builder.build();
    }

    public static DistancedConnectionInfo fromGrpcConnectionInfo(GrpcConnectionInfo connectionInfo) {
        return new DistancedConnectionInfo(
                ID.fromString(connectionInfo.getId()),
                connectionInfo.getAddress(),
                connectionInfo.getPort(),
                connectionInfo.getDistance().equals("") ? null : new BigInteger(connectionInfo.getDistance())
        );
    }

    public static List<DistancedConnectionInfo> fromGrpcConnectionInfo(List<GrpcConnectionInfo> list) {
        return list.stream().map(DistancedConnectionInfo::fromGrpcConnectionInfo).collect(Collectors.toList());
    }
}
