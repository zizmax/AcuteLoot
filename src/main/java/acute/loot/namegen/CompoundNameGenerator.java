package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;

import java.util.List;

public class CompoundNameGenerator implements NameGenerator {

    private final List<NameGenerator> parts;

    public CompoundNameGenerator(List<NameGenerator> parts) {
        this.parts = parts;
    }

    @Override
    public String generate(LootMaterial lootMaterial, LootRarity rarity) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i != 0) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(parts.get(i).generate(lootMaterial, rarity));
        }
        return stringBuilder.toString();
    }
}
