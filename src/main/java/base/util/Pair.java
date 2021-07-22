package base.util;

import java.util.Objects;

/**
 * Class representing a pair of values.
 *
 * @param <A> type of the left value
 * @param <B> type of the righ value
 */
public final class Pair<A, B> {

    private final A left;
    private final B right;

    /**
     * Construct a new pair.
     *
     * @param left the left item, must be non-null
     * @param right the right item, must be non-null
     */
    public Pair(A left, B right) {
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);
        this.left = left;
        this.right = right;
    }

    public A left() {
        return left;
    }

    public B right() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return left.equals(pair.left) &&
                right.equals(pair.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
