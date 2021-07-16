package base.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A class representing an object that is either one type or another.
 *
 * @param <A> the left type
 * @param <B> the right type
 */
public final class Either<A, B> {

    private final A left;
    private final B right;

    private Either(A left, B right) {
        this.left = left;
        this.right = right;
    }

    public Optional<A> left() {
        return Optional.ofNullable(left);
    }

    public Optional<B> right() {
        return Optional.ofNullable(right);
    }

    public void acceptOr(final Consumer<A> aConsumer, final Consumer<B> bConsumer) {
        left().ifPresent(aConsumer);
        right().ifPresent(bConsumer);
    }

    public static <A, B> Either<A, B> ofLeft(final A a) {
        return new Either<>(Objects.requireNonNull(a), null);
    }

    public static <A, B> Either<A, B> ofRight(final B b) {
        return new Either<>(null, Objects.requireNonNull(b));
    }

}
