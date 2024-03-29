package pt.up.fc.dcc.ssd.p2p.conn;

import pt.up.fc.dcc.ssd.p2p.grpc.GrpcConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DistancedConnectionInfo {
    private final ConnectionInfo connectionInfo;
    private BigInteger distance;

    public DistancedConnectionInfo(ConnectionInfo connectionInfo, BigInteger distance) {
        this.connectionInfo = connectionInfo;
        this.distance = distance;
    }

    public DistancedConnectionInfo(Id id, String address, int port, BigInteger distance) {
        this.connectionInfo = new ConnectionInfo(id, address, port);
        this.distance = distance;
    }

    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public Id getId() {
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
            Id.idFromBinaryString(connectionInfo.getId()),
            connectionInfo.getAddress(),
            connectionInfo.getPort(),
            connectionInfo.getDistance().equals("") ? null : new BigInteger(connectionInfo.getDistance())
        );
    }

    public static List<DistancedConnectionInfo> fromGrpcConnectionInfo(List<GrpcConnectionInfo> list) {
        return list.stream().map(DistancedConnectionInfo::fromGrpcConnectionInfo).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "DistancedConnectionInfo{" +
            "connectionInfo=" + connectionInfo +
            ", distance=" + distance +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DistancedConnectionInfo info = (DistancedConnectionInfo) o;
        return connectionInfo.equals(info.connectionInfo) && Objects.equals(distance, info.distance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionInfo, distance);
    }
}
