package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;
import com.github.phillip.h.acutelib.util.Checks;
import com.github.phillip.h.acutelib.util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * NameGenerator that draws from a fixed list of names regardless
 * of material and rarity, either of which may be null.
 */
public class FixedListNameGenerator implements NameGenerator {

    private final List<String> names;

    /**
     * Construct a new FixedListNameGenerator with the given names.
     *
     * @param names the names, must be non-null and non-empty
     */
    public FixedListNameGenerator(String... names) {
        this(Arrays.asList(names));
    }

    /**
     * Construct a new FixedListNameGenerator with the given names.
     *
     * @param names the names, must be non-null and non-empty
     */
    public FixedListNameGenerator(List<String> names) {
        this.names = Checks.requireNonEmpty(names);
    }

    @Override
    public String generate(LootMaterial lootMaterial, LootRarity rarity) {
        return Util.drawRandom(names);
    }

    @Override
    public long countNumberOfNames() {
        return names.size();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FixedListNameGenerator that = (FixedListNameGenerator) o;
        return Objects.equals(names, that.names);
    }

    @Override
    public int hashCode() {
        return Objects.hash(names);
    }
}
