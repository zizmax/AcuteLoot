package acute.loot.generator;

import static acute.loot.Util.stream;

import acute.loot.*;
import acute.loot.namegen.NameGenerator;
import com.github.phillip.h.acutelib.collections.IntegerChancePool;
import com.github.phillip.h.acutelib.util.Checks;
import com.github.phillip.h.acutelib.util.Util;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class for generating new pieces of loot.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LootItemGenerator {

    private static final Random random = AcuteLoot.random;

    private final @NonNull IntegerChancePool<LootRarity> rarityPool;
    private final @NonNull IntegerChancePool<LootSpecialEffect> effectPool;
    private final @NonNull Namer namer;
    private final @NonNull AcuteLoot plugin;

    /**
     * Generate a loot item using the given rarity and material.
     * Rarity must be in [0, 1]
     */
    public LootItem generate(double rarity, LootMaterial material) {
        return generate(rarityPool.draw(Checks.requireInUnitInterval(rarity)), material);
    }

    /**
     * Generate a loot item with the provided rarity and material.
     *
     * @param lootRarity Rarity of the item generated
     * @param material   Material of the item generated
     * @return A randomly generated LootItem with the provided rarity
     */
    public LootItem generate(@NonNull LootRarity lootRarity, LootMaterial material) {
        final Optional<LootSpecialEffect> effect;
        if (random.nextDouble() <= lootRarity.getEffectChance()) {
            effect = effectPool.tryDrawWithPredicate(l -> l.getValidMaterials().contains(material));
        } else {
            effect = Optional.empty();
        }

        return new LootItem(lootRarity, stream(effect).collect(Collectors.toList()));
    }

    /**
     * Create a loot item with a random material.
     *
     * @return a loot item with a random material.
     */
    public ItemStack createLoot() {
        return createLoot(getNewRandomLootItemStack(), random.nextDouble());
    }

    /**
     * Create a loot item from the given item stack with the given rarity.
     * Rarity must be in [0.0, 1.0]
     */
    public ItemStack createLoot(ItemStack item, double rarity) {
        return createLoot(item, generate(rarity, LootMaterial.lootMaterialForMaterial(item.getType())));
    }

    /**
     * Create a loot item from the given item stack and LootRarity.
     */
    @SuppressWarnings("UnusedReturnValue")
    public ItemStack createLoot(ItemStack item, LootRarity rarity) {
        return createLoot(item, generate(rarity, LootMaterial.lootMaterialForMaterial(item.getType())));
    }

    /**
     * Create a loot item with the given ItemStack and LootItem loot data.
     * This will create a name for the item as well as attaching the LootItem
     * to the item's persistent data.
     *
     * @param item the item to turn into a loot item
     * @param loot the LootItem loot data for the item
     * @return the item with the LootItem data added along with a generated name
     */
    public ItemStack createLoot(ItemStack item, final LootItem loot) {
        final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(item.getType());
        if (lootMaterial.equals(LootMaterial.UNKNOWN)) {
            return item;
        }

        namer.nameLoot(item, loot);

        // Add loot info to lore and display name
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();

        // Store lootCode in metadata using PersistentDataHolder API
        NamespacedKey key = new NamespacedKey(plugin, "lootCodeKey");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, loot.lootCode());

        // Add loot info to lore
        if (!Boolean.FALSE.equals(plugin.getConfig().getBoolean(("display-rarities")))) {
            lore.add(loot.rarity().getRarityColor() + loot.rarity().getName());
        }
        for (LootSpecialEffect effect : loot.getEffects()) {
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

        item.setItemMeta(meta);

        return item;
    }

    /**
     * Return a new random item stack from the loot materials list
     * in the AcuteLoot instance. If it is damageable, the item will
     * be given random damage.
     *
     * @return a random new item stack
     */
    public ItemStack getNewRandomLootItemStack() {
        ItemStack item = new ItemStack(Util.drawRandom(plugin.lootMaterials), 1);

        // Set random damage if Material is damageable
        if (item.getItemMeta() instanceof Damageable && item.getType().getMaxDurability() > 0) {
            ((Damageable) item.getItemMeta()).setDamage(random.nextInt(item.getType().getMaxDurability()));
        }
        return item;
    }

    public static LootItemGeneratorBuilder builder(final @NonNull AcuteLoot plugin) {
        return new LootItemGeneratorBuilder().plugin(plugin);
    }

    /**
     * Builder for a LootItemGenerator.
     */
    public static class LootItemGeneratorBuilder {

        private LootItemGeneratorBuilder() {}

        /**
         * Set the name generator source for this builder. Note this will overwrite
         * any previously set Namer instance.
         *
         * @param namePool the name pool
         * @param overwriteCustom if true existing custom names will be overwritten by loot generation
         * @return the builder instance
         */
        public LootItemGeneratorBuilder namePool(final IntegerChancePool<NameGenerator> namePool,
                                                 final boolean overwriteCustom) {
            namer = new NamePoolNamer(namePool, plugin);
            if (!overwriteCustom) {
                namer = new PreserveCustomNameNamer(namer);
            }

            return this;
        }

    }

}
