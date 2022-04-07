package acute.loot.generator;

import acute.loot.*;
import com.github.phillip.h.acutelib.decorators.MetaEditor;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.stripColor;

@AllArgsConstructor
class DefaultLorer implements Lorer {

    private final @NonNull AcuteLoot plugin;

    @Override
    public void loreLoot(ItemStack itemStack, LootItem lootItem) {
        final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(itemStack.getType());

        // Add loot info to lore and display name
        ItemMeta meta = itemStack.getItemMeta();
        final List<String> lore = new ArrayList<>();

        // Store lootCode in metadata using PersistentDataHolder API
        final NamespacedKey key = new NamespacedKey(plugin, "lootCodeKey");
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
        MetaEditor.on(itemStack).setLore(lore);
    }

    @Override
    public LootItem inverseLore(final @NonNull ItemStack item) {
        if (item.getItemMeta() == null || item.getItemMeta().getLore() == null) {
            throw new UnsupportedOperationException("Lore does not exist, cannot inverse");
        }

        final List<String> lore = item.getItemMeta().getLore();
        if (lore.isEmpty()) {
            throw new UnsupportedOperationException("Cannot inverse empty lore");
        }

        if (Boolean.FALSE.equals(plugin.getConfig().getBoolean(("display-rarities")))) {
            throw new UnsupportedOperationException("Cannot inverse lore without displayed rarity");
        }

        // Check that the category lore matches, if present
        final String category = LootMaterial.lootMaterialForMaterial(item.getType()).name().toLowerCase();
        final List<String> effectLore;
        if (plugin.getConfig().getBoolean("loot-category-lore.enabled") &&
                plugin.getConfig().contains("loot-category-lore." + category)) {
            final List<String> expectedCategoryLore = plugin.getConfig()
                                                            .getStringList("loot-category-lore." + category)
                                                            .stream()
                                                            .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                                                            .collect(Collectors.toList());

            final List<String> categoryLore = lore.subList(Math.max(0, lore.size() - expectedCategoryLore.size()), lore.size());
            if (!categoryLore.equals(expectedCategoryLore)) {
                throw new UnsupportedOperationException("Category lore section does not match");
            }

            effectLore = lore.subList(1, lore.size() - expectedCategoryLore.size());
        } else {
            effectLore = lore.subList(1, lore.size());
        }

        // Retrieve rarity and effects from the lore
        final String rarityName = stripColor(lore.get(0));
        final LootRarity rarity = LootRarity.getRarities()
                                            .values()
                                            .stream()
                                            .filter(r -> r.getName().equals(rarityName))
                                            .findAny()
                                            .orElseThrow(() -> new UnsupportedOperationException("Cannot find rarity inverse for '" + rarityName + "'"));

        final List<LootSpecialEffect> effects = effectLore.stream()
                                                          .map(ChatColor::stripColor)
                                                          .map(effectName -> LootSpecialEffect.findByUniqueName(effectName)
                                                                                              .orElseThrow(() -> new UnsupportedOperationException("Could not find unique effect with name " + effectName)))
                                                          .collect(Collectors.toList());

        return new LootItem(rarity, effects);
    }
}
