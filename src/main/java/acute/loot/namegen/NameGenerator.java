package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;

public interface NameGenerator {

    String generate(LootMaterial lootMaterial, LootRarity rarity);

}
