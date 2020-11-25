package acute.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IntegerChancePool<T> {

    private final Random random;

    private int max;
    private final List<Element<T>> elements;

    private static class Element<T> {
        public Element(T val, int lower, int upper) {
            this.val = val;
            this.lower = lower;
            this.upper = upper;
        }

        public T val;
        public int lower;
        public int upper;
    }

    public IntegerChancePool() {
        this(new Random());
    }

    public IntegerChancePool(Random random) {
        this.random = random;
        max = 0;
        elements = new ArrayList<>();
    }

    public void add(T val, int relativeChance) {
        if (relativeChance < 1) throw new IllegalArgumentException("Relative chance must be positive.");
        elements.add(new Element<>(val, max, max + relativeChance));
        max += relativeChance;
    }

    public T draw() {
        if (elements.isEmpty()) throw new NoSuchElementException();
        return draw(random.nextInt(max));
    }

    public T draw(double rarity) {
        if (rarity < 0 || rarity > 1) throw new IllegalArgumentException("Parameter of draw() must be non-negative and less or equal to 1.0");
        int x = (int) Math.floor(max * rarity);
        x = x == max ? max - 1 : x; // x will be max if rarity = 1
        return draw(x);
    }

    public T draw(int x) {
        if (elements.isEmpty()) throw new NoSuchElementException();
        if (x < 0 || x >= max) throw new IllegalArgumentException("Parameter of draw() must be positive and less than max().");

        return elements.stream()
                       .filter(e -> e.lower <= x && e.upper > x)
                       .findFirst()
                       .orElseThrow(() -> new IllegalStateException("Value " + x + " not in range of any pool elements."))
                       .val;
    }

    public T drawWithPredicate(Function<T, Boolean> predicate) {
        return filter(predicate).draw();
    }

    public IntegerChancePool<T> filter(Function<T, Boolean> predicate) {
        final List<Element<T>> effectiveElements = elements.stream()
                                                           .filter(e -> predicate.apply(e.val))
                                                           .collect(Collectors.toList());

        final IntegerChancePool<T> effectivePool = new IntegerChancePool<>();
        for (Element<T> e : effectiveElements) {
            effectivePool.add(e.val, e.upper - e.lower);
        }
        return effectivePool;
    }

    public void removeWithPredicate(Function<T, Boolean> predicate) {
        final IntegerChancePool<T> newPool = filter(x -> !predicate.apply(x)); // Get items that DO NOT match the predicate
        elements.clear();
        elements.addAll(newPool.elements);
        max = newPool.max;
    }

    public int max() {
        return max;
    }

    public List<T> values() {
        return elements.stream().map(e -> e.val).collect(Collectors.toList());
    }

    public void clear() {
        elements.clear();
        max = 0;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("IntegerChancePool[");
        for (int i = 0; i < elements.size(); i++) {
            final Element<T> e = elements.get(i);
            str.append(e.lower).append('-').append(e.upper - 1).append(':').append(e.val);
            if (i != elements.size() - 1) str.append(", ");
        }
        str.append(']');
        return str.toString();
    }

}
