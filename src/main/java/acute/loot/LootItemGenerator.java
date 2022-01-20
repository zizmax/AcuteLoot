package acute.loot;

import acute.loot.namegen.NameGenerator;
import com.github.phillip.h.acutelib.collections.IntegerChancePool;
import com.github.phillip.h.acutelib.util.Checks;
import com.github.phillip.h.acutelib.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Class for generating new pieces of loot.
 */
public class LootItemGenerator {

    private static final Random random = AcuteLoot.random;

    private final IntegerChancePool<LootRarity> rarityPool;
    private final IntegerChancePool<LootSpecialEffect> effectPool;
    private final IntegerChancePool<NameGenerator> namePool;
    private final AcuteLoot plugin;

    /**
     * Construct a new LootItemGenerator.
     *
     * @param rarityPool the rarity pool for loot
     * @param effectPool the effect pool for loot
     * @param namePool the name pool for loot
     * @param plugin the AcuteLoot instance
     */
    public LootItemGenerator(IntegerChancePool<LootRarity> rarityPool,
                             IntegerChancePool<LootSpecialEffect> effectPool,
                             IntegerChancePool<NameGenerator> namePool,
                             AcuteLoot plugin) {
        this.rarityPool = rarityPool;
        this.effectPool = effectPool;
        this.namePool = namePool;
        this.plugin = plugin;
    }

    /**
     * Generate a loot item using the given rarity and material.
     *
     * @param rarity   the rarity of the item generated, higher is rarer, must be in [0, 1]
     * @param material the material of the item generated
     * @return A randomly generated LootItem
     */
    public LootItem generate(double rarity, LootMaterial material) {
        Checks.requireInUnitInterval(rarity);
        final LootRarity lootRarity = rarityPool.draw(rarity);
        return generateWithRarity(lootRarity, material);
    }

    /**
     * Generate a loot item with the provided rarity and material.
     *
     * @param lootRarity Rarity of the item generated
     * @param material   Material of the item generated
     * @return A randomly generated LootItem with the provided rarity
     */
    public LootItem generateWithRarity(LootRarity lootRarity, LootMaterial material) {
        int itemRarity = lootRarity.getId();

        List<EffectId> effects = new ArrayList<>();
        try {
            if (random.nextDouble() <= lootRarity.getEffectChance()) {
                final LootSpecialEffect effect = effectPool.drawWithPredicate(l -> l.getValidMaterials()
                                                                                    .contains(material));
                effects.add(effect.effectId());
            }
        } catch (NoSuchElementException e) {
            // No effects for this material, ignore.
        }

        return new LootItem(itemRarity, effects);
    }

    /**
     * Create a loot item with a random material.
     *
     * @return a loot item with a random material.
     */
    public ItemStack createLootItem() {
        return createLootItem(getNewRandomLootItemStack(), random.nextDouble());
    }

    /**
     * Create a loot item from the given item stack with the given rarity.
     *
     * @param item   the item stack to turn into a loot item
     * @param rarity the rarity of the item, in [0.0, 1.0]
     * @return a loot item made from the given item stack
     */
    public ItemStack createLootItem(ItemStack item, double rarity) {
        // Generate loot: name, rarity and effects
        final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(item.getType());
        if (lootMaterial.equals(LootMaterial.UNKNOWN)) {
            return item;
        }
        return createLootItem(item, generate(rarity, lootMaterial));
    }

    /**
     * Create a loot item from the given item stack and LootRarity.
     *
     * @param item   the item stack to turn into a loot item
     * @param rarity the rarity of the item
     * @return a loot item made from the given item stack
     */
    @SuppressWarnings("UnusedReturnValue")
    public ItemStack createLootItem(ItemStack item, LootRarity rarity) {
        final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(item.getType());
        if (lootMaterial.equals(LootMaterial.UNKNOWN)) {
            return item;
        }
        return createLootItem(item, generateWithRarity(rarity, lootMaterial));
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
    public ItemStack createLootItem(ItemStack item, final LootItem loot) {
        final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(item.getType());
        if (lootMaterial.equals(LootMaterial.UNKNOWN)) {
            return item;
        }

        String name = rollName(lootMaterial, loot.rarity());

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

        // Set display name
        if (plugin.getConfig().getBoolean("global-loot-name-color")) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
                                                                                  .getString("loot-name-color")) + name);
        } else {
            meta.setDisplayName(loot.rarity().getRarityColor() + name);
        }
        item.setItemMeta(meta);

        return item;
    }

    protected String rollName(LootMaterial lootMaterial, LootRarity rarity) {
        String name = null;
        int attempts = 100;
        NameGenerator nameGenerator = null;
        do {
            try {
                nameGenerator = namePool.draw();
                name = nameGenerator.generate(lootMaterial, rarity);
            } catch (NoSuchElementException e) {
                // Couldn't draw a name for some reason, try again
                attempts--;
            }
        } while (name == null && attempts > 0);
        if (attempts == 0) {
            plugin.getLogger().severe("Could not generate a name in 100 attempts! Are name files empty or corrupted?");
            plugin.getLogger().severe("Name Generator: " + nameGenerator.toString());
        }
        return name;
    }

    /**
     * Return a new random item stack from the loot materials list
     * in the AcuteLoot instance. If it is damageable, the item will
     * be given a random damage.
     *
     * @return a random new item stack
     */
    public ItemStack getNewRandomLootItemStack() {
        ItemStack item = new ItemStack(Util.drawRandom(plugin.lootMaterials), 1);

        // Set random damage if Material is damageable
        if (item.getItemMeta() instanceof Damageable && item.getType().getMaxDurability() > 0) {
            Damageable dmgItemMeta = (Damageable) item.getItemMeta();
            dmgItemMeta.setDamage(random.nextInt(item.getType().getMaxDurability()));
            item.setItemMeta((ItemMeta) dmgItemMeta);
        }
        return item;
    }

}
