package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;

import java.util.List;
import java.util.stream.Collectors;

public class CompoundNameGenerator implements NameGenerator {

    private final List<NameGenerator> parts;

    public CompoundNameGenerator(List<NameGenerator> parts) {
        this.parts = parts;
    }

    @Override
    public String generate(LootMaterial lootMaterial, LootRarity rarity) {
        return parts.stream()
                    .map(part -> part.generate(lootMaterial, rarity))
                    .collect(Collectors.joining(" "));
    }

    @Override
    public long countNumberOfNames() {
        return parts.stream().mapToLong(NameGenerator::countNumberOfNames).reduce(1, (a, b) -> a * b);
    }
}
