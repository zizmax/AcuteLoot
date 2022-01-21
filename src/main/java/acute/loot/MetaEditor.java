package acute.loot;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Decorator for an ItemMeta. Exposes a fluent interface that
 * immediately propagates all changes to the underlying ItemStack.
 */
public class MetaEditor {
    private final ItemStack item;
    private final ItemMeta metaInternal;

    private MetaEditor(final ItemStack item) {
        this.item = item;
        metaInternal = item.getItemMeta();
    }

    public static MetaEditor on(final ItemStack item) {
        return new MetaEditor(item);
    }

    /**
     * Set the item's display name.
     *
     * @param displayName the item's display name
     * @return this MetaEditor
     */
    public MetaEditor setDisplayName(final String displayName) {
        metaInternal.setDisplayName(displayName);
        item.setItemMeta(metaInternal);
        return this;
    }

    /**
     * Set the item's lore.
     *
     * @param lore the item's lore
     * @return this MetaEditor
     */
    public MetaEditor setLore(final List<String> lore) {
        metaInternal.setLore(lore);
        item.setItemMeta(metaInternal);
        return this;
    }
}
