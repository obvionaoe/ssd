package pt.up.fc.dcc.ssd.common;

import java.util.Objects;

public class Pair<T, R> {
    private final T first;
    private final R second;

    private Pair(T first, R second) {
        this.first = first;
        this.second = second;
    }

    public static <T, R> Pair<T, R> pair(T first, R second) {
        return new Pair<>(first, second);
    }

    public T first() {
        return first;
    }

    public R second() {
        return second;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
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
    public static <T, R> Pair<T, R> cast(Pair<?, ?> pair, Class<T> pClass, Class<R> qClass) {
        return (Pair<T, R>) pair;
    }
}
