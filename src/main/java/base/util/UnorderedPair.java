package base.util;

import java.util.Objects;

/**
 * Class for an unordered pair of values.
 *
 * @param <T> the type of the elements
 */
public final class UnorderedPair<T> {

    public final T first;
    public final T second;

    private UnorderedPair(final T left, final T right) {
        this.first = left;
        this.second = right;
    }

    /**
     * Construct a new unordered pair. The order of the elements
     * of the resulting pair may differ from the order of the elements
     * passed in.
     *
     * @param first the first element
     * @param second the second element
     * @param <T> the type of the pair
     * @return new unordered pair
     */
    public static <T> UnorderedPair<T> of(final T first, final T second) {
        if (Objects.hashCode(first) <= Objects.hashCode(second)) {
            return new UnorderedPair<>(first, second);
        } else {
            return new UnorderedPair<>(second, first);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UnorderedPair<?> that = (UnorderedPair<?>) o;
        return Objects.equals(first, that.first) &&
                Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
