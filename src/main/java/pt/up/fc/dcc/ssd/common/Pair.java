package pt.up.fc.dcc.ssd.common;

import java.util.Objects;

public class Pair<T, R> {
    private final T first;
    private final R second;

    private Pair(T first, R second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Creates a new pair
     *
     * @param first  the first item in the pair
     * @param second the second item in the pair
     * @param <T>    the type of the first item in the pair
     * @param <R>    the type of the second item in the pair
     * @return a Pair<T, R>
     */
    public static <T, R> Pair<T, R> pair(T first, R second) {
        return new Pair<>(first, second);
    }

    /**
     * Gets the first item in the pair
     *
     * @return first item in the pair
     */
    public T first() {
        return first;
    }

    /**
     * Gets the second item in the pair
     *
     * @return second item in the pair
     */
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
