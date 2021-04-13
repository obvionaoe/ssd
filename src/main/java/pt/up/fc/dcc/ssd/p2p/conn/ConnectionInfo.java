package pt.up.fc.dcc.ssd.p2p.conn;

import pt.up.fc.dcc.ssd.p2p.grpc.GrpcConnectionInfo;
import pt.up.fc.dcc.ssd.p2p.node.ID;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConnectionInfo {
    private final ID id;
    private final String address;
    private final int port;
    private BigInteger distance;

    public ConnectionInfo(ID id, String address, int port) {
        this.id = id;
        this.address = address;
        this.port = port;
    }

    public ID getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
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
                .setId(id.toBinaryString())
                .setPort(port)
                .setAddress(address);

        if (distance != null) {
            builder.setDistance(distance.toString());
        }

        return builder.build();
    }

    public static ConnectionInfo fromGrpcConnectionInfo(GrpcConnectionInfo connectionInfo) {
        ConnectionInfo result = new ConnectionInfo(ID.fromString(connectionInfo.getId()), connectionInfo.getAddress(), connectionInfo.getPort());

        if (!connectionInfo.getDistance().equals("")) {
            result.setDistance(new BigInteger(connectionInfo.getDistance()));
        }

        return result;
    }

    public static List<ConnectionInfo> fromGrpcConnectionInfo(List<GrpcConnectionInfo> list) {
        return list.stream().map(ConnectionInfo::fromGrpcConnectionInfo).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ConnectionInfo{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionInfo that = (ConnectionInfo) o;
        return port == that.port && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }
}
