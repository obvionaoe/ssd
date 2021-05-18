package pt.up.fc.dcc.ssd.p2p.routing.exceptions;

public class NonExistentNodeException extends Exception {
    public NonExistentNodeException(String s) {
        super(s);
    }

    public NonExistentNodeException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
