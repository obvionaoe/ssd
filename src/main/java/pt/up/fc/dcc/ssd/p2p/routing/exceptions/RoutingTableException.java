package pt.up.fc.dcc.ssd.p2p.routing.exceptions;

public class RoutingTableException extends Exception {
    public RoutingTableException(String s) {
        super(s);
    }

    public RoutingTableException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
