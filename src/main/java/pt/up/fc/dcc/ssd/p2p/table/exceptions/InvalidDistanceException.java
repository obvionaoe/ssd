package pt.up.fc.dcc.ssd.p2p.table.exceptions;

public class InvalidDistanceException extends Exception {
    public InvalidDistanceException(String s) {
        super(s);
    }

    public InvalidDistanceException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
