package acute.loot.generator;

import acute.loot.LootItem;
import com.github.phillip.h.acutelib.decorators.MetaEditor;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiFunction;

@AllArgsConstructor
class PrefixNamer implements Namer {

    private final @NonNull Namer delegate;
    private final @NonNull BiFunction<ItemStack, LootItem, String> prefixGenerator;

    @Override
    public void nameLoot(ItemStack itemStack, LootItem lootItem) {
        delegate.nameLoot(itemStack, lootItem);

        if (itemStack.getItemMeta() != null && itemStack.getItemMeta().hasDisplayName()) {
            final String name = itemStack.getItemMeta().getDisplayName();
            MetaEditor.on(itemStack).setDisplayName(prefixGenerator.apply(itemStack, lootItem) + name);
        }
    }

    static PrefixNamer fixed(final String prefix, final Namer delegate) {
        return new PrefixNamer(delegate, (i, l) -> prefix);
    }

    static PrefixNamer rarityColor(final Namer delegate) {
        return new PrefixNamer(delegate, (i, l) -> l.rarity().getRarityColor());
    }

}
