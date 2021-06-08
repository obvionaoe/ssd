package pt.up.fc.dcc.ssd.p2p.conn;

import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;

public class ConnectionInfo {
    private final Id id;
    private final String address;
    private final int port;
    private BigInteger trust;
    private BigInteger risk;
    private int numberOfInteractions;
    private Date joiningDate;

    public ConnectionInfo(Id id, String address, int port) {
        this.id = id;
        this.address = address;
        this.port = port;
        this.trust = null;
        this.risk = null;
        this.numberOfInteractions = 0;
    }

    public ConnectionInfo(Id id, String address, int port, BigInteger risk, BigInteger trust) {
        this.id = id;
        this.address = address;
        this.port = port;
        this.trust = trust;
        this.risk = risk;
        this.numberOfInteractions = 0;
    }

    public Id getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public BigInteger getTrust() {
        return trust;
    }

    public void changeTrust(BigInteger change) {
        trust = trust.add(change);
    }

    public BigInteger getRisk() {
        return trust;
    }

    public void changeRisk(BigInteger change) {
        trust = trust.add(change);
    }

    protected BigInteger reliabilityFactor() {
        BigInteger wRr = joiningDate.
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
