package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;

import java.util.Objects;

public abstract class TransformationNameGenerator implements NameGenerator {

    private final NameGenerator baseGenerator;

    public TransformationNameGenerator(NameGenerator baseGenerator) {
        this.baseGenerator = baseGenerator;
    }

    public abstract String transform(final String input);

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

    public static TransformationNameGenerator uppercaser(final NameGenerator baseGenerator) {
        return new TransformationNameGenerator(baseGenerator) {
            @Override
            public String transform(String input) {
                return input.substring(0, 1).toUpperCase() + input.substring(1);
            }
        };
    }
}
