package pt.up.fc.dcc.ssd.p2p.conn;

import pt.up.fc.dcc.ssd.p2p.node.ID;

import java.util.Objects;

public class ConnectionInfo {
    private final ID id;
    private final String address;
    private final int port;

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

    public DistancedConnectionInfo toDistancedConnectionInfo() {
        return new DistancedConnectionInfo(this, null);
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
        return id.equals(that.id) && port == that.port && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }
}
