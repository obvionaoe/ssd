package pt.up.fc.dcc.ssd.p2p.table.exceptions;

public class NonExistentNodeException extends Exception {
    public NonExistentNodeException(String s) {
        super(s);
    }

    public NonExistentNodeException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
