package acute.loot.generator;

import acute.loot.LootRarity;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
class PreserveCustomNameNamer implements Namer {

    private final @NonNull Namer delegate;

    @Override
    public void nameLoot(ItemStack lootItem, LootRarity rarity) {
        if (!(lootItem.hasItemMeta() && lootItem.getItemMeta().hasDisplayName())) {
            delegate.nameLoot(lootItem, rarity);
        }
    }
}
