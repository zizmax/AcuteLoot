package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;
import acute.loot.Util;

import java.util.List;

public interface NamePool {

    List<String> getNames(LootMaterial lootMaterial, LootRarity lootRarity);

    default String drawName(LootMaterial lootMaterial, LootRarity lootRarity) {
        return Util.drawRandom(getNames(lootMaterial, lootRarity));
    }
}
