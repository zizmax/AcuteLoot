package acute.loot.generator;

import acute.loot.AcuteLoot;
import acute.loot.LootItem;
import acute.loot.LootMaterial;
import acute.loot.LootSpecialEffect;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
class DefaultLorer implements Lorer {

    private final @NonNull AcuteLoot plugin;

    @Override
    public void loreLoot(ItemStack itemStack, LootItem lootItem) {
        final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(itemStack.getType());

        // Add loot info to lore and display name
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>();

        // Store lootCode in metadata using PersistentDataHolder API
        NamespacedKey key = new NamespacedKey(plugin, "lootCodeKey");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, lootItem.lootCode());

        // Add loot info to lore
        if (!Boolean.FALSE.equals(plugin.getConfig().getBoolean(("display-rarities")))) {
            lore.add(lootItem.rarity().getRarityColor() + lootItem.rarity().getName());
        }
        for (LootSpecialEffect effect : lootItem.getEffects()) {
            //String effectName = plugin.getConfig().getString("effects." + effect.getName().replace("_", ".") + ".name");
            String effectName = effect.getDisplayName();
            lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
                                                                       .getString("loot-effect-color")) + effectName);
        }

        // Add category lore
        if (plugin.getConfig().getBoolean("loot-category-lore.enabled")) {
            String category = lootMaterial.name().toLowerCase();
            if (plugin.getConfig().contains("loot-category-lore." + category)) {
                List<String> loreLines = plugin.getConfig().getStringList("loot-category-lore." + category);
                for (String line : loreLines) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            } else {
                plugin.getLogger().warning("ERROR: Failed to add lore from config: loot-category-lore." + lootMaterial.name());
            }
        }
        meta.setLore(lore);

        itemStack.setItemMeta(meta);
    }
}
