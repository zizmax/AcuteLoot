package acute.loot;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Helper class to provide a convenient way of adjusting item metadata.
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
     * Set the item's display name
     *
     * @param displayName the item's display name
     * @return this MetaEditor
     */
    public MetaEditor setDisplayName(final String displayName) {
        metaInternal.setDisplayName(displayName);
        item.setItemMeta(metaInternal);
        return this;
    }
}
