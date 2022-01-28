package acute.loot.namegen;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.*;

/**
 * NameGenerator that draws from a fixed list of names regardless
 * of material and rarity, either of which may be null.
 */
@EqualsAndHashCode
@AllArgsConstructor
public class FixedListNameGenerator implements NameGenerator {

    private final @NonNull List<String> names;

    private final @EqualsAndHashCode.Exclude Random random = new Random();

    /**
     * Construct a new FixedListNameGenerator with the given names.
     *
     * @param names the names, must be non-null
     */
    public FixedListNameGenerator(String... names) {
        this(Arrays.asList(names));
    }

    @Override
    public String generate(final Map<String, String> parameters) {
        if (names.isEmpty()) {
            throw new NoSuchElementException();
        }
        return names.size() == 1 ? names.get(0) : names.get(random.nextInt(names.size()));
    }

    @Override
    public Optional<Long> countNumberOfNames() {
        return Optional.of((long) names.size());
    }

    /**
     * Get a FixedListNameGenerator for the given names file.
     *
     * @param namesFile the names file
     * @return a FixedListNameGenerator the given names file.
     */
    public static FixedListNameGenerator fromNamesFile(final String namesFile) {
        return new FixedListNameGenerator(NameGenerator.readNames(namesFile));
    }
}
