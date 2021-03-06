package base.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Various utilities.
 */
public final class Util {

    private static Random random = new Random();

    private Util() {
    }

    /**
     * Set the Random instance used for operations requiring randomness.
     *
     * @param random the random instance, must be non-null
     */
    public static void setRandom(final Random random) {
        Util.random = Objects.requireNonNull(random);
    }

    /**
     * Return the given items as a queue.
     *
     * @param items the items
     * @param <T>   type of the items and the resulting queue
     * @return the items as a queue
     */
    @SafeVarargs
    public static <T> Queue<T> asQueue(final T... items) {
        return new LinkedList<>(Arrays.asList(items));
    }

    /**
     * Draw a random element from a list. If the list is empty, throw a NoSuchElementException.
     *
     * @param <T>  type of the list
     * @param list the List to draw from
     * @return a random element from the list
     */
    public static <T> T drawRandom(List<T> list) {
        Objects.requireNonNull(list);
        Checks.requireNonEmpty(list);
        if (list.size() == 1) {
            return list.get(0);
        }
        return list.get(random.nextInt(list.size()));
    }

    /**
     * Raytrace a line from the given Location going distance with step size
     * of addition.
     *
     * @param from     the origin vector
     * @param distance the distance to trace the line
     * @param addition the step size
     * @return a list of Locations less than distance away from from with step size addition
     */
    public static List<Location> getLine(Location from, double distance, double addition) {
        Objects.requireNonNull(from);
        List<Location> locations = new ArrayList<>();
        final Vector direction = from.getDirection(); // End - Begin | length to 1
        for (double d = addition; d < distance; d += addition) {
            locations.add(from.clone().add(direction.clone().normalize().multiply(d)));
        }
        return locations;
    }

    /**
     * Return the Material for the given string, if it exists. If it does not
     * exist, or the String is null or empty, return null.
     *
     * @param materialString the string for the material to be returned
     * @return the material for the string
     */
    public static Material validateMaterial(String materialString) {
        if (Checks.isEmpty(materialString)) {
            return null;
        }
        try {
            return Material.matchMaterial(materialString.trim());
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    /**
     * Return the keys of a map that match a predicate on their value.
     *
     * @param map the map to filter, must be non-null
     * @param predicate the predicate, must be non-null
     * @param <K> the key type
     * @param <V> the value type
     * @return the keys of the map whose value passes the predicate
     */
    public static <K, V> List<K> matchingKeys(final Map<K, V> map, Predicate<V> predicate) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(predicate);
        return map.entrySet()
                  .stream()
                  .filter(e -> predicate.test(e.getValue()))
                  .map(Map.Entry::getKey)
                  .collect(Collectors.toList());
    }

    /**
     * Get the chunks in a give radius around the provided entity. The chunks returned are
     * those in grid of side length [-radius, radius] around the entity.
     *
     * @param e the entity
     * @param radius the radius
     * @return the chinks around the entity
     */
    public static List<Chunk> chunksAround(final Entity e, final int radius) {
        Objects.requireNonNull(e);
        if (radius < 1) {
            return Collections.emptyList();
        }

        final int effectiveRadius = radius - 1;
        final List<Chunk> chunks = new ArrayList<>();
        for (int x = -effectiveRadius; x <= effectiveRadius; x++) {
            for (int z = -effectiveRadius; z <= effectiveRadius; z++) {
                chunks.add(e.getLocation().add(new Vector(x * 16, 0, z * 16)).getChunk());
            }
        }
        return chunks;
    }

    /**
     * Parse the provided string to an Integer if possible, otherwise
     * return empty.
     *
     * @param s the string to parse
     * @return the parsed string if possible, otherwise empty
     */
    public static Optional<Integer> parseIntOptional(final String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
