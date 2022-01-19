package acute.loot.generator;

import acute.loot.LootRarity;
import org.bukkit.inventory.ItemStack;

interface Namer {

    void nameLoot(ItemStack lootItem, LootRarity rarity);

}
