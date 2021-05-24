package pt.up.fc.dcc.ssd.p2p.grpc;

import java.util.Objects;

public class ResponsePair<T, R> {
    private final T status;
    private final R response;

    private ResponsePair(T status, R response) {
        this.status = status;
        this.response = response;
    }

    public static <T, R> ResponsePair<T, R> pair(T first, R second) {
        return new ResponsePair<>(first, second);
    }

    public T status() {
        return status;
    }

    public R response() {
        return response;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, response);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponsePair<?, ?> pair = (ResponsePair<?, ?>) o;
        return Objects.equals(status, pair.status) && Objects.equals(response, pair.response);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @SuppressWarnings("unchecked")
    public static <T, R> ResponsePair<T, R> cast(ResponsePair<?, ?> pair, Class<T> pClass, Class<R> qClass) {
        return (ResponsePair<T, R>) pair;
    }
}
