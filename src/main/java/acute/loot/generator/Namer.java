package acute.loot.generator;

import acute.loot.LootItem;
import org.bukkit.inventory.ItemStack;

interface Namer {

    void nameLoot(ItemStack itemStack, LootItem lootItem);

}
