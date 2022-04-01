package acute.loot.generator;

import acute.loot.LootItem;
import org.bukkit.inventory.ItemStack;

interface Lorer {

    void loreLoot(ItemStack itemStack, LootItem lootItem);

    default LootItem inverseLore(ItemStack item) {
        throw new UnsupportedOperationException("Lorer does not support inverse");
    }

}
