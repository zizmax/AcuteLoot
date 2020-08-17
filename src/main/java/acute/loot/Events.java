package acute.loot;

import acute.loot.namegen.NameGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Events implements Listener {

    private final AcuteLoot plugin;

    public Events(AcuteLoot plugin) {
        this.plugin = plugin;
    }

    static List<Material> materials = new ArrayList<Material>();

    public static void createMaterials(AcuteLoot plugin, String path) {
        materials = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(path))) {
            List<String> lines = stream.collect(Collectors.toList());
            for (String line : lines) {
                if (!line.contains("#") && !line.trim().equals("")) {
                    String[] materialStrings = line.split(",");
                    for (String material : materialStrings) {
                        material = material.trim();
                        if (!material.equals(null) && !material.equals("")) {
                            try {
                                Material mat = Material.matchMaterial(material);
                                if (mat != null) materials.add(mat);
                                else {
                                    throw new NullPointerException();
                                }
                            } catch (IllegalArgumentException | NullPointerException e) {
                                plugin.getLogger().warning(material + " not valid material for server version: " + Bukkit.getBukkitVersion() + ". Skipping...");
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Fatal IO exception while initializing materials.txt. Is file missing or corrupted?");
        }
        plugin.getLogger().info("Initialized " + materials.size() + " materials");
    }

    @EventHandler
    public void anvilListener(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        if (event.getViewers().isEmpty() || inv.getItem(0) == null) return;
        if (inv.getItem(0).hasItemMeta() && inv.getItem(0).getItemMeta().hasDisplayName()) {
            String origName = getDisplayName(inv.getItem(0));
            ItemStack result = event.getResult();

            if (result != null && !result.getType().equals(Material.AIR) && origName.contains(String.valueOf('§'))) {
                ItemMeta meta = result.getItemMeta();
                meta.setDisplayName(origName);
                result.setItemMeta(meta);
                event.setResult(result);
            }
        }
    }

    public String getDisplayName(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return "";
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        if (!plugin.getConfig().getBoolean("use-permissions") || (plugin.getConfig().getBoolean("use-permissions") && player.hasPermission("acuteloot.enchant"))) {
            Map<Enchantment, Integer> enchantments = event.getEnchantsToAdd();
            int enchantRarity = getEnchantRarity(enchantments);
            ItemStack item = event.getItem();
            if (getLootCode(plugin, item) == null) {
                double seed = AcuteLoot.random.nextDouble();
                double chance = (seed + (enchantRarity / 300.0)) / 2.0;
                item = createLootItem(item, chance);
                if (AcuteLoot.debug) {
                    player.sendMessage(ChatColor.GOLD + "You enchanted a " + ChatColor.AQUA + item.getType().toString());
                    player.sendMessage(ChatColor.GOLD + "It is called " + item.getItemMeta().getDisplayName());
                    player.sendMessage(ChatColor.GOLD + "Enchant Score: " + ChatColor.AQUA + enchantRarity);
                    player.sendMessage(ChatColor.GOLD + "Enchant Score Percentage: " + ChatColor.AQUA + String.format("%.2f%%", ((enchantRarity / 300.0) * 100.0)));
                    player.sendMessage(ChatColor.GOLD + "Seed: " + ChatColor.AQUA + String.format("%.2f%%", seed * 100.0));
                    player.sendMessage(ChatColor.GOLD + "Final Rarity Score: " + ChatColor.AQUA + String.format("%.2f%%", chance * 100.0));
                    player.sendMessage(ChatColor.GOLD + "Rarity: " + ChatColor.AQUA + item.getItemMeta().getLore().get(0));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerHitMob(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                if (plugin.getConfig().getBoolean("effects.enabled") && ((Player) arrow.getShooter()).getInventory()
                                                                                                     .getItemInMainHand()
                                                                                                     .getType() == Material.BOW) {
                    String lootCode = getLootCode(plugin, ((Player) arrow.getShooter()).getInventory()
                                                                                       .getItemInMainHand());
                    if (lootCode != null) {
                        LootItem loot = new LootItem(lootCode);
                        List<LootSpecialEffect> effects = loot.getEffects();
                        effects.forEach(e -> e.apply(event));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Populate loot chest
        Random random = new Random();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block.getType() == Material.CHEST) {
                Chest chest = (Chest) block.getState();
                if (AcuteLoot.debug || chest.getLootTable() != null) {
                    // Only naturally-generated chests have lootTables
                    // And they only have the lootTable the very first time they are opened
                    if (plugin.getConfig().getBoolean("loot-sources.chests.enabled")) {
                        double roll = AcuteLoot.random.nextDouble();
                        double chance = plugin.getConfig().getDouble("loot-sources.chests.chance") / 100.0;
                        if (AcuteLoot.debug) {
                            event.getPlayer().sendMessage("Chance: " + chance);
                            event.getPlayer().sendMessage("Roll: " + roll);
                        }
                        if (roll <= chance) {
                            List<Integer> emptySlots = new ArrayList<>(); // Array of the indexes of the empty slots
                            int slotIndex = 0;
                            for (ItemStack itemStack : chest.getInventory().getContents()) {
                                // Only add null (empty) slots so they can later be filled with loot
                                if (itemStack == null) emptySlots.add(slotIndex);
                                slotIndex++;
                            }
                            int origEmptySlotsSize = emptySlots.size();
                            if (emptySlots.size() > 0) {
                                ItemStack newLoot = createLootItem();
                                slotIndex = random.nextInt(emptySlots.size());
                                int slotToFill = emptySlots.get(slotIndex);
                                chest.getInventory().setItem(slotToFill, newLoot);
                                emptySlots.remove(slotIndex);

                                for (int i = 1; i <= origEmptySlotsSize - 1; i++) {
                                    roll = AcuteLoot.random.nextDouble();
                                    if (AcuteLoot.debug) event.getPlayer().sendMessage("Roll: " + roll);
                                    if (roll <= chance) {
                                        newLoot = createLootItem();
                                        slotIndex = random.nextInt(emptySlots.size());
                                        slotToFill = emptySlots.get(slotIndex);
                                        chest.getInventory().setItem(slotToFill, newLoot);
                                        emptySlots.remove(slotIndex);
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() != Material.AIR && item.getItemMeta().getLore() != null) {
                if (plugin.getConfig().getBoolean("effects.enabled")) {
                    String lootCode = getLootCode(plugin, item);
                    if (lootCode != null) {
                        LootItem loot = new LootItem(lootCode);
                        List<LootSpecialEffect> effects = loot.getEffects();
                        effects.forEach(e -> e.apply(event));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Entity caught = event.getCaught();
        if (caught instanceof Item) {
            if (plugin.getConfig().getBoolean("loot-sources.fishing.enabled")) {
                double roll = AcuteLoot.random.nextDouble();
                double chance = plugin.getConfig().getDouble("loot-sources.fishing.chance") / 100.0;
                if (AcuteLoot.debug) {
                    event.getPlayer().sendMessage("Roll: " + roll);
                    event.getPlayer().sendMessage("Raw chance: " + chance);
                }
                // 2.1% is ~amount that chance of treasure goes up for each Luck of the Sea level
                chance = chance + event.getPlayer()
                                       .getInventory()
                                       .getItemInMainHand()
                                       .getEnchantmentLevel(Enchantment.LUCK) * .021;
                if (AcuteLoot.debug)
                    event.getPlayer().sendMessage("Enchanted chance: " + chance);
                if (roll <= chance) {
                    AcuteLoot.random.nextInt();
                    Item itemEntity = (Item) caught;
                    ItemStack item = createLootItem();
                    // Turns out that unlike all the other trees, BAMBOO_SAPLING is NOT the material type
                    // BAMBOO_SAPLING appears to be the block material ONLY, so will error when applied to an ItemStack
                    // The original material lists include BAMBOO_SAPLING instead of the correct BAMBOO
                    // This is a sanity check in case someone is running on the old version
                    if(item.getType().equals(Material.BAMBOO_SAPLING)) item.setType(Material.BAMBOO);
                    itemEntity.setItemStack(item);
                }
            }
        }
    }

    //XP Boost Effect
    @EventHandler
    public void onExpGain(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        if (plugin.getConfig().getBoolean("effects.enabled") && player.getInventory().getHelmet() != null) {
            String lootCode = getLootCode(plugin, player.getInventory().getHelmet());
            if (lootCode != null) {
                LootItem loot = new LootItem(lootCode);
                List<LootSpecialEffect> effects = loot.getEffects();
                effects.forEach(e -> e.apply(event));
            }
        }
    }

    public int getEnchantRarity(Map<Enchantment, Integer> enchantments) {
        double total_levels = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            total_levels = total_levels + (float) entry.getValue() / entry.getKey().getMaxLevel() * 100.0;
        }
        return Math.min((int) total_levels, 300);

    }


    public static String getLootCode(AcuteLoot plugin, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey key = new NamespacedKey(plugin, "lootCodeKey");
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(key, PersistentDataType.STRING)) {
            String foundValue = container.get(key, PersistentDataType.STRING);
            if (foundValue != null && foundValue.contains("#AL")) {
                return foundValue;
            }
        }
        return null;
    }

    // Create Loot Item with RANDOM type
    public ItemStack createLootItem() {
        Random random = AcuteLoot.random;
        int materialIndex = random.nextInt(materials.size());
        ItemStack item = new ItemStack(materials.get(materialIndex), 1);

        // Set random damage if Material is damageable
        if (item.getItemMeta() instanceof Damageable && item.getType().getMaxDurability() > 0) {
            Damageable dmgItemMeta = (Damageable) item.getItemMeta();
            int damage = random.nextInt(item.getType().getMaxDurability());
            dmgItemMeta.setDamage(damage);
            item.setItemMeta((ItemMeta) dmgItemMeta);
        }

        return createLootItem(item, random.nextDouble());
    }


    // Create Loot Item with GIVEN type
    public ItemStack createLootItem(ItemStack item, double rarity) {
        // Generate loot: name, rarity and effects
        LootItemGenerator generator = new LootItemGenerator(AcuteLoot.rarityChancePool, AcuteLoot.effectChancePool);
        final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(item.getType());
        if (lootMaterial.equals(LootMaterial.UNKNOWN)) {
            return item;
        }
        return createLootItem(item, generator.generate(rarity, lootMaterial));
    }

    public ItemStack createLootItem(ItemStack item, int rarity, List<Integer> effects) {
        return createLootItem(item, new LootItem(rarity, effects));
    }

    public ItemStack createLootItem(ItemStack item, final LootItem loot) {
        return createLootItem(item, loot, AcuteLoot.nameGenChancePool.draw());
    }

    public ItemStack createLootItem(ItemStack item, final LootItem loot, final NameGenerator nameGenerator) {
        final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(item.getType());
        if (lootMaterial.equals(LootMaterial.UNKNOWN)) {
            return item;
        }

        String name = null;
        int attempts = 100;
        do {
            try {
                name = nameGenerator.generate(lootMaterial, loot.rarity());
            } catch (NoSuchElementException e) {
                // Couldn't draw a name for some reason, try again
                attempts--;
            }
        } while (name == null && attempts > 0);
        if (attempts == 0) {
            plugin.getLogger().severe("Could not generate a name in 100 attempts! Are name files empty or corrupted?");
        }

        // Add loot info to lore and display name
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();

        // Store lootCode in metadata using PersistentDataHolder API
        NamespacedKey key = new NamespacedKey(plugin, "lootCodeKey");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, loot.lootCode());

        // Add loot info to lore and display name
        lore.add(loot.rarity().getRarityColor() + loot.rarity().getName());
        for (LootSpecialEffect effect : loot.getEffects()) {
            String effectName = plugin.getConfig().getString("effects." + effect.getName().replace("_", ".") + ".name");
            lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("loot-effect-color")) + effectName);
        }
        meta.setLore(lore);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("loot-name-color")) + name);
        item.setItemMeta(meta);

        return item;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (plugin.getConfig().getBoolean("effects.enabled") && player.getInventory().getBoots() != null){
            String lootCode = getLootCode(plugin, player.getInventory().getBoots());
            if (lootCode != null) {
                LootItem loot = new LootItem(lootCode);
                List<LootSpecialEffect> effects = loot.getEffects();
                // Block Trail Effect and Time Walk Effect
                // Check if player moves between a block
                if (player.isOnGround() && (event.getFrom().getBlockX() != (event.getTo().getBlockX())
                        || event.getFrom().getBlockZ() != event.getTo().getBlockZ()
                        || event.getFrom().getBlockY() != event.getTo().getBlockY())) {
                        effects.forEach(e -> e.apply(event));
                }
            }
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player && plugin.getConfig().getBoolean("effects.enabled")) {
            Player player = (Player) event.getEntity();
            String lootCode = getLootCode(plugin, player.getInventory().getItemInMainHand());
            if (lootCode != null) {
                LootItem loot = new LootItem(lootCode);
                List<LootSpecialEffect> effects = loot.getEffects();
                effects.forEach(e -> e.apply(event));
            }
        }
    }
}
