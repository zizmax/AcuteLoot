package base.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @SafeVarargs
    public static <T> T[] concat(IntFunction<T[]> generator, T[]...items) {
        return Stream.of(items).flatMap(Stream::of).toArray(generator);
    }

    /**
     * Strip out any legacy formatting codes from the given string.
     * If the string is null, shorter than two characters, or contains
     * no formatting codes the original string is returned.
     *
     * @param input the string to strip
     * @return the input stripped of any legacy formatting codes
     */
    public static String stripLegacyFormattingCodes(final String input) {
        if (input == null || input.length() < 2) {
            return input;
        }
        return input.charAt(0) == '§' ? stripLegacyFormattingCodes(input.substring(2))
                                      : input.substring(0, 1) + stripLegacyFormattingCodes(input.substring(1));
    }

    /**
     * Replace any key in the variable map that occurs in the pattern with
     * its corresponding value. The return value of the method is a LinkedHashMap
     * whose items are the tokens in the pattern, where the pattern is tokenized
     * around any variables. E.g., if the variable map contains [foo] = bar, then
     * the pattern "[foo] bar baz hello, [foo] 123456789" would tokenize as
     * "bar", " bar baz hello, "bar" " 123456789". The entries of the returned map
     * are keyed by pairs of (Index, Value | Variable Name).
     *
     * @param pattern the pattern to substitute using the variable map, must be non-null
     * @param variableMap the variable map, must be non-null
     * @return the substituted pattern
     */
    public static LinkedHashMap<Pair<Integer, String>, String> substituteVariables(final String pattern,
                                                                    final Map<String, String> variableMap) {
        Objects.requireNonNull(pattern);
        Objects.requireNonNull(variableMap);

        final LinkedHashMap<Pair<Integer, String>, String> result = new LinkedHashMap<>();
        int cursor = 0;
        int cut = 0;
        outer: while (cursor < pattern.length()) {
            for (Map.Entry<String, String> var : variableMap.entrySet()) {
                if (pattern.substring(cursor).startsWith(var.getKey())) {
                    if (cursor - cut != 0) {
                        final String word = pattern.substring(cut, cursor);
                        result.put(new Pair<>(result.size(), word), word);
                    }

                    result.put(new Pair<>(result.size(), var.getKey()), var.getValue());
                    cursor += var.getKey().length();
                    cut = cursor;
                    continue outer;
                }
            }
            cursor++;
        }

        if (cut != cursor || cursor == 0) {
            result.put(new Pair<>(result.size(), pattern.substring(cut)), pattern.substring(cut));
        }

        return result;
    }

    /**
     * Remove trailing newlines from the given string. If the string is
     * null null is returned.
     *
     * @param s the string to trim
     * @return the string with trailing newlines removed
     */
    public static String trimTrailingNewlines(String s) {
        if (s != null && s.endsWith("\n")) {
            return trimTrailingNewlines(s.substring(0, s.length() - 1));
        }
        return s;
    }

    /**
     * Apply the given player selector from the given sender. @p, @a, @r, and a specific player
     * are supported. If no player matches the selector an empty list is returned. @p is only supported
     * if the sender is a Player.
     *
     * @param selector the selector, must be non-empty
     * @param sender the sender, must be non-null
     * @return the result of the given selector, possibly empty
     */
    public static List<? extends Player> handlePlayerSelector(final String selector, final CommandSender sender) {
        Checks.requireNonEmpty(selector);
        Objects.requireNonNull(sender);
        if (selector.equals("@p") && sender instanceof Player) {
            return sender.getServer()
                         .getOnlinePlayers()
                         .stream()
                         .filter(p -> p.getWorld().equals(((Player) sender).getWorld()))
                         .filter(p -> !p.equals(sender))
                         .min(Comparator.comparingDouble(a -> a.getLocation().distanceSquared(((Player) sender).getLocation())))
                         .map(Collections::singletonList)
                         .orElse(Collections.emptyList());
        } else if (selector.equals("@r")) {
            return Collections.singletonList(drawRandom(new ArrayList<>(sender.getServer().getOnlinePlayers())));
        } else if (selector.equals("@a")) {
            return new ArrayList<>(sender.getServer().getOnlinePlayers());
        } else if (sender.getServer().getPlayerExact(selector) != null) {
            return Collections.singletonList(sender.getServer().getPlayerExact(selector));
        }
        return Collections.emptyList();
    }
}
