package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;

import java.util.Objects;

/**
 * NameGenerator that wraps another NameGenerator and applies a "transformation"
 * to its output. A transformation can be any function String -> String, typically
 * a small change such as adjusting capitalization.
 */
public abstract class TransformationNameGenerator implements NameGenerator {

    private final NameGenerator baseGenerator;

    /**
     * Construct a new TransformationNameGenerator with the given base NameGenerator.
     * @param baseGenerator the base NameGenerator, must not be null.
     */
    public TransformationNameGenerator(NameGenerator baseGenerator) {
        this.baseGenerator = Objects.requireNonNull(baseGenerator);
    }

    /**
     * The transform, called with the result of the base NameGenerator.
     * @param input the string to transform, likely the result of invoking the base NameGenerator
     * @return the transformed string
     */
    protected abstract String transform(final String input);

    @Override
    public String generate(LootMaterial lootMaterial, LootRarity rarity) {
        return transform(baseGenerator.generate(lootMaterial, rarity));
    }

    @Override
    public long countNumberOfNames() {
        return baseGenerator.countNumberOfNames();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransformationNameGenerator that = (TransformationNameGenerator) o;
        return Objects.equals(baseGenerator, that.baseGenerator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseGenerator);
    }

    /**
     * Wrap the given NameGenerator in a TransformationNameGenerator that
     * ensures the first character in the generated name is uppercase.
     * @param baseGenerator the base NameGenerator to wrap
     * @return a TransformationNameGenerator that ensures the first character of the name is uppercase
     */
    public static TransformationNameGenerator uppercaser(final NameGenerator baseGenerator) {
        return new TransformationNameGenerator(baseGenerator) {
            @Override
            public String transform(String input) {
                return input.substring(0, 1).toUpperCase() + input.substring(1);
            }
        };
    }
}
