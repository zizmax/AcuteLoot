package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;

public class NamePoolNameGenerator implements NameGenerator {

    final NamePool namePool;

    public NamePoolNameGenerator(NamePool namePool) {
        this.namePool = namePool;
    }

    @Override
    public String generate(LootMaterial lootMaterial, LootRarity rarity) {
        return namePool.drawName(lootMaterial, rarity);
    }

    @Override
    public long countNumberOfNames() {
        // TODO
        return 0;
    }
}
