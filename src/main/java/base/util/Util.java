package base.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Various utilities.
 */
public final class Util {

    private static Random random = new Random();

    private Util() {}

    /**
     * Set the Random instance used for operations requiring randomness.
     * @param random the random instance, must be non-null
     */
    public static void setRandom(final Random random) {
        Util.random = Objects.requireNonNull(random);
    }

    /**
     * Return the given items as a queue.
     * @param items the items
     * @param <T> type of the items and the resulting queue
     * @return the items as a queue
     */
    @SafeVarargs
    public static <T> Queue<T> asQueue(final T... items) {
        return new LinkedList<>(Arrays.asList(items));
    }

    /**
     * Draw a random element from a list. If the list is empty, throw a NoSuchElementException.
     *
     * @param <T> type of the list
     * @param list the List to draw from
     * @return a random element from the list
     */
    public static <T> T drawRandom(List<T> list) {
        Objects.requireNonNull(list);
        Checks.requireNonEmpty(list);
        if (list.size() == 1) return list.get(0);
        return list.get(random.nextInt(list.size()));
    }

    /**
     * Raytrace a line from the given Location going distance with step size
     * of addition.
     * @param from the origin vector
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
     * @param materialString the string for the material to be returned
     * @return the material for the string
     */
    public static Material validateMaterial(String materialString) {
        if (materialString == null || materialString.trim().isEmpty()) return null;
        try {
            return Material.matchMaterial(materialString.trim());
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}