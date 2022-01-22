package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Optional;

/**
 * NameGenerator that wraps another NameGenerator and applies a "transformation"
 * to its output. A transformation can be any function String {@literal ->} String, typically
 * a small change such as adjusting capitalization.
 */
@AllArgsConstructor
public abstract class TransformationNameGenerator implements NameGenerator {

    private final @NonNull NameGenerator baseGenerator;

    /**
     * The transform, called with the result of the base NameGenerator.
     *
     * @param input the string to transform, likely the result of invoking the base NameGenerator
     * @return the transformed string
     */
    protected abstract String transform(final String input);

    @Override
    public String generate(LootMaterial lootMaterial, LootRarity rarity) {
        return transform(baseGenerator.generate(lootMaterial, rarity));
    }

    @Override
    public Optional<Long> countNumberOfNames() {
        return baseGenerator.countNumberOfNames();
    }

    /**
     * Wrap the given NameGenerator in a TransformationNameGenerator that
     * ensures the first character in the generated name is uppercase.
     *
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
