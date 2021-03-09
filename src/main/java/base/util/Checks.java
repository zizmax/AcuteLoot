package base.util;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public final class Checks {

    private Checks() {}

    public static boolean isEmpty(final String s) {
        return Objects.requireNonNull(s).trim().isEmpty();
    }

    public static <T extends Collection<?>> boolean isEmpty(final T collection) {
        return Objects.requireNonNull(collection).isEmpty();
    }

    public static String requireNonEmpty(final String s) {
        return requireNonEmpty(s, "String must be non-empty");
    }

    public static String requireNonEmpty(final String s, final String message) {
        return checkNot(s, Checks::isEmpty, message);
    }

    public static <T extends Collection<?>> T requireNonEmpty(final T collection) {
        return requireNonEmpty(collection, "Collection must be non-empty");
    }

    public static <T extends Collection<?>> T requireNonEmpty(final T collection, final String message) {
        return checkNot(collection, Checks::isEmpty, message, NoSuchElementException::new);
    }

    public static int requirePositive(final int x) {
        return requirePositive(x, "Integer must be positive");
    }

    public static int requirePositive(final int x, final String message) {
        return check(x, i -> i > 0, message);
    }

    public static double requireInInterval(final int x, final int lower, final int upper) {
        return requireInInterval(x, lower, upper, String.format("Integer must be in [%d, %d]", lower, upper));
    }

    public static double requireInInterval(final int x, final int lower, final int upper, final String message) {
        return check(x, i -> i >= lower && i <= upper, message);
    }

    public static double requireInUnitInterval(final double x) {
        return requireInUnitInterval(x, "Double must be in [0, 1]");
    }

    public static double requireInUnitInterval(final double x, final String message) {
        return checkNot(x, i -> i < 0 || i > 1, message);
    }

    public static <K> Map<K, ?> requireDoesNotHaveKey(final K key, final Map<K, ?> map) {
        return requireDoesNotHaveKey(key, map, "Map must not have key " + key);
    }

    public static <K> Map<K, ?> requireDoesNotHaveKey(final K key, final Map<K, ?> map, final String message) {
        return checkNot(map, m -> m.containsKey(key), message);
    }

    public static <T> T checkNot(final T t, final Predicate<T> predicate, final String message) {
        return checkNot(t, predicate, message, IllegalArgumentException::new);
    }

    public static <T> T checkNot(final T t,
                                 final Predicate<T> predicate,
                                 final String message,
                                 final Function<String, RuntimeException> exceptionFunction) {
        return check(t, predicate.negate(), message, exceptionFunction);
    }

    public static <T> T check(final T t, final Predicate<T> predicate, final String message) {
        return check(t, predicate, message, IllegalArgumentException::new);
    }

    public static <T> T check(final T t,
                              final Predicate<T> predicate,
                              final String message,
                              final Function<String, RuntimeException> exceptionFunction) {
        if (predicate.negate().test(t)) {
            throw exceptionFunction.apply(message);
        }
        return t;
    }

}
