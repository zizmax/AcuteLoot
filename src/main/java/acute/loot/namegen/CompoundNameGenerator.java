package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;
import base.util.Checks;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * NameGenerator that invokes one or more other NameGenerators,
 * joining the results with a possibly-empty joining string.
 */
public class CompoundNameGenerator implements NameGenerator {

    private final List<NameGenerator> parts;
    private final String joiningString;

    /**
     * Construct a new CompoundNameGenerator with the given NameGenerator
     * parts and " " as the joining string.
     *
     * @param parts the NameGenerator parts, must be non-null and non-empty
     */
    public CompoundNameGenerator(NameGenerator... parts) {
        this(Arrays.asList(parts));
    }

    /**
     * Construct a new CompoundNameGenerator with the given NameGenerator
     * parts and " " as the joining string.
     *
     * @param parts the NameGenerator parts, must be non-null and non-empty
     */
    public CompoundNameGenerator(List<NameGenerator> parts) {
        this(parts, " ");
    }

    /**
     * Construct a new CompoundNameGenerator with the given NameGenerator
     * parts and joining string.
     *
     * @param parts         the NameGenerator parts, must be non-null and non-empty
     * @param joiningString the joining string, must be non-null
     */
    public CompoundNameGenerator(List<NameGenerator> parts, String joiningString) {
        this.parts = Checks.requireNonEmpty(parts);
        this.joiningString = Objects.requireNonNull(joiningString);
    }

    @Override
    public String generate(LootMaterial lootMaterial, LootRarity rarity) {
        return parts.stream()
                    .map(part -> part.generate(lootMaterial, rarity))
                    .collect(Collectors.joining(joiningString));
    }

    @Override
    public long countNumberOfNames() {
        return parts.stream().mapToLong(NameGenerator::countNumberOfNames).reduce(1, (a, b) -> a * b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompoundNameGenerator that = (CompoundNameGenerator) o;
        return Objects.equals(parts, that.parts) &&
                Objects.equals(joiningString, that.joiningString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parts, joiningString);
    }
}
