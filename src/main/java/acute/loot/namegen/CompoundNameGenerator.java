package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CompoundNameGenerator implements NameGenerator {

    private final List<NameGenerator> parts;
    private final String joiningString;

    public CompoundNameGenerator(NameGenerator... parts) {
        this(Arrays.asList(parts));
    }

    public CompoundNameGenerator(List<NameGenerator> parts) {
        this(parts, " ");
    }

    public CompoundNameGenerator(List<NameGenerator> parts, String joiningString) {
        this.parts = parts;
        this.joiningString = joiningString;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompoundNameGenerator that = (CompoundNameGenerator) o;
        return Objects.equals(parts, that.parts) &&
                Objects.equals(joiningString, that.joiningString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parts, joiningString);
    }
}
