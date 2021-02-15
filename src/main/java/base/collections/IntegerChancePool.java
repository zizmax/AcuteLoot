package base.collections;

import base.util.Checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * An IntegerChancePool allows for a set of elements to be drawn with
 * "relative chance". For example, if you add "A" with chance "2", "B"
 * with chance "3", and "C" with chance "5", calling the simple draw()
 * method will produce A 20% of the time, B 30% of the time, and C 50% of
 * the time. In general the chance of drawing a given element is equal to
 * the element's chance divided by the sum of all element chances.
 * This later value is referred to as the chance pool's maximum or max.
 *
 * The chance pool works by partitioning the integers 0 <= x < max
 * into blocks for each added element with respect to their chance. For
 * example, the situation above with "A", "B", and "C" would result
 * in {0, 1} being associated with "A", {2, 3, 4} being associated
 * with B, and {5, 6, 7, 8, 9} being associated with C. This representation
 * must be kept in mind while working with the parameterized draw() methods,
 * one of which maps the double range [0, 1) to integers 0 <= x < max and
 * the other of which returns the element associated with the integer 0 <= x < max.
 *
 * The chance pool respects the order of added elements, meaning for example that
 * invoking draw with parameter 0 will always draw the first element added and
 * invoking draw with parameter (max - 1) will always draw the last element added.
 */
public class IntegerChancePool<T> {

    // Random instance for draws
    private final Random random;

    // The chance pool maximum, elements will partition the range [0, max).
    private int max;
    private final List<Element<T>> elements;

    /**
     * An element in the chance pool.
     */
    private static class Element<T> {
        public Element(T val, int lower, int upper) {
            this.val = val;
            this.lower = lower;
            this.upper = upper;
        }

        public T val;
        public int lower; // Lower partition bound, inclusive
        public int upper; // Upper partition bound, exclusive
    }

    /**
     * Construct a new IntegerChancePool with a new
     * Random instance.
     */
    public IntegerChancePool() {
        this(new Random());
    }

    /**
     * Construct a new IntegerChancePool with the given
     * Random instance. The Random instance must be non-nul.
     * @param random the Random instance
     */
    public IntegerChancePool(Random random) {
        this.random = Objects.requireNonNull(random);
        max = 0;
        elements = new ArrayList<>();
    }

    /**
     * Add an element to the chance pool with the given relative chance.
     * @param val the value to add
     * @param relativeChance the relative chance to draw the element, must be positive
     */
    public void add(T val, int relativeChance) {
        Checks.requirePositive(relativeChance, "Relative chance must be positive.");
        elements.add(new Element<>(val, max, max + relativeChance));
        max += relativeChance;
    }

    /**
     * Add the value to the pool with the given relative chance.
     * If the chance is not positive, the element is silently ignored
     * and not added.
     * @param val the value to add to the chance pool
     * @param relativeChance the relative chance for the value to be drawn
     */
    public void addDiscardingInvalid(T val, int relativeChance) {
        if (relativeChance > 0) {
            add(val, relativeChance);
        }
    }

    /**
     * Draw a random element from the chance pool. The element is selected
     * with respect to the relative chance it was added with.
     * @return a random element from the chance pool
     */
    public T draw() {
        Checks.requireNonEmpty(elements, "Cannot draw from empty pool.");
        return draw(random.nextInt(max));
    }

    /**
     * Draw the element from the chance pool that is associated with the given double in [0.0, 1.0].
     * This operation is NOT random, see class documentation.
     * @param d the double to get the element for
     * @return the element in the chance pool associated with the double
     */
    public T draw(double d) {
        Checks.requireInUnitInterval(d, "Parameter of draw() must be non-negative and less or equal to 1.0");
        int x = (int) Math.floor(max * d);
        x = x == max ? max - 1 : x; // x will be max if rarity = 1
        return draw(x);
    }

    /**
     * Draw the element from the chance pool that is associated with the given integer in 0 <= x < max.
     * This operation is NOT random, see class documentation.
     * @param x the integer to get the element for
     * @return the element in the chance pool associated with the integer
     */
    public T draw(int x) {
        Checks.requireNonEmpty(elements, "Cannot draw from empty pool.");
        Checks.requireInInterval(x, 0, max - 1, "Parameter of draw() must be positive and less than max().");

        return elements.stream()
                       .filter(e -> e.lower <= x && e.upper > x)
                       .findFirst()
                       .orElseThrow(() -> new IllegalStateException("Value " + x + " not in range of any pool elements."))
                       .val;
    }

    /**
     * Draw an element from the subset of elements matching the predicate.
     * This is equivalent to filter(predicate).draw().
     * @param predicate the predicate to test items with
     * @return an element drawn from the subset of elements matching the predicate
     */
    public T drawWithPredicate(Predicate<T> predicate) {
        return filter(predicate).draw();
    }

    /**
     * Return a filtered version of the chance pool containing only elements that
     * match the predicate. The original object is unmodified. Note that the returned
     * chance pool will have a different max() unless all elements match the predicate.
     * @param predicate the predicate to test elements with
     * @return a filtered version of this chance pool containing only elements that match the predicate
     */
    public IntegerChancePool<T> filter(Predicate<T> predicate) {
        Objects.requireNonNull(predicate);
        final List<Element<T>> effectiveElements = elements.stream()
                                                           .filter(e -> predicate.test(e.val))
                                                           .collect(Collectors.toList());

        final IntegerChancePool<T> effectivePool = new IntegerChancePool<>();
        for (Element<T> e : effectiveElements) {
            effectivePool.add(e.val, e.upper - e.lower); // Relative chance is always Upper - Lower
        }
        return effectivePool;
    }

    /**
     * Remove all elements from the chance pool that DO match the given predicate.
     * This is similar to the negation of filter(), but will modify this object instead
     * of returning another one.
     * @param predicate the predicate to test element with
     */
    public void removeWithPredicate(Predicate<T> predicate) {
        Objects.requireNonNull(predicate);
        final IntegerChancePool<T> newPool = filter(predicate.negate()); // Get items that DO NOT match the predicate
        elements.clear();
        elements.addAll(newPool.elements);
        max = newPool.max;
    }

    /**
     * Return the chance pool maximum, that is, the integer one
     * more than the greatest value accepted by draw(). This is
     * generally used to bound a random number generator being used
     * with draw().
     * @return the chance pool maximum
     */
    public int max() {
        return max;
    }

    /**
     * Return a list of the values in the chance pool. No
     * guarantee is made as to the order of returned values.
     * @return the values in the chance pool
     */
    public List<T> values() {
        return elements.stream().map(e -> e.val).collect(Collectors.toList());
    }

    /**
     * Clear the chance pool. This will also reset max() to 0.
     */
    public void clear() {
        elements.clear();
        max = 0;
    }

    @Override
    public String toString() {
        String str = elements.stream()
                             .map(e -> String.valueOf(e.lower) + '-' + (e.upper - 1) + ':' + e.val)
                             .collect(Collectors.joining(", "));
        return "IntegerChancePool[" + str + "]";
    }

}
