package acute.loot.generator;

import acute.loot.LootItem;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
class PreserveCustomNameNamer implements Namer {

    private final @NonNull Namer delegate;

    @Override
    public void nameLoot(ItemStack itemStack, LootItem lootItem) {
        if (!(itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())) {
            delegate.nameLoot(itemStack, lootItem);
        }
    }
}
