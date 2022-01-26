package acute.loot.generator;

import acute.loot.LootItem;
import com.github.phillip.h.acutelib.decorators.MetaEditor;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiFunction;

@AllArgsConstructor
class ColorNamer implements Namer {

    private final @NonNull Namer delegate;
    private final @NonNull BiFunction<ItemStack, LootItem, String> colorGenerator;
    private final boolean overwriteColors;

    @Override
    public void nameLoot(ItemStack itemStack, LootItem lootItem) {
        delegate.nameLoot(itemStack, lootItem);

        if (itemStack.getItemMeta() != null && itemStack.getItemMeta().hasDisplayName()) {
            final String name = itemStack.getItemMeta().getDisplayName();
            final String color = colorGenerator.apply(itemStack, lootItem);
            MetaEditor.on(itemStack).setDisplayName(colorName(name, color));
        }
    }

    protected String colorName(final String name, final String color) {
        final String strippedName = ChatColor.stripColor(name);
        return !strippedName.equals(name) && !overwriteColors ? name : color + strippedName;
    }

    static ColorNamer fixed(final String prefix, final Namer delegate, final boolean overwriteColors) {
        return new ColorNamer(delegate, (i, l) -> prefix, overwriteColors);
    }

    static ColorNamer rarityColor(final Namer delegate, final boolean overwriteColors) {
        return new ColorNamer(delegate, (i, l) -> l.rarity().getRarityColor(), overwriteColors);
    }

}
