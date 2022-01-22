package acute.loot.namegen;

import com.github.phillip.h.acutelib.util.Util;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * NameGenerator that draws from a fixed list of names regardless
 * of material and rarity, either of which may be null.
 */
@EqualsAndHashCode
@AllArgsConstructor
public class FixedListNameGenerator implements NameGenerator {

    private final @NonNull List<String> names;

    /**
     * Construct a new FixedListNameGenerator with the given names.
     *
     * @param names the names, must be non-null and non-empty
     */
    public FixedListNameGenerator(String... names) {
        this(Arrays.asList(names));
    }

    @Override
    public String generate(final Map<String, String> parameters) {
        return Util.drawRandom(names);
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
