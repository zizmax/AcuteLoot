package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;

public class ConstantNameGenerator implements NameGenerator {

    private final String constantName;

    public ConstantNameGenerator(String constantName) {
        this.constantName = constantName;
    }

    @Override
    public String generate(LootMaterial lootMaterial, LootRarity rarity) {
        return constantName;
    }

    @Override
    public long countNumberOfNames() {
        return 1;
    }
}
