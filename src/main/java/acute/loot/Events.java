package acute.loot;

import acute.loot.namegen.NameGenerator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

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

    static List<Material> materials = new ArrayList<>();

    public static void createMaterials(AcuteLoot plugin, String path) {
        materials = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(path))) {
            List<String> lines = stream.collect(Collectors.toList());
            for (String line : lines) {
                if (!line.contains("#") && !line.trim().equals("")) {
                    String[] materialStrings = line.split(",");
                    for (String material : materialStrings) {
                        material = material.trim();
                        if (!material.equals("")) {
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
        if (plugin.getConfig().getBoolean("loot-sources.enchanting.enabled")) {
            double roll = AcuteLoot.random.nextDouble();
            double chance = plugin.getConfig().getDouble("loot-sources.enchanting.chance") / 100.0;
            if (AcuteLoot.debug) {
                player.sendMessage("Roll: " + roll);
                player.sendMessage("Raw chance: " + chance);
            }
            if (!plugin.getConfig().getBoolean("use-permissions") || (plugin.getConfig().getBoolean("use-permissions") && player.hasPermission("acuteloot.enchant"))) {
                if (roll <= chance) {
                    Map<Enchantment, Integer> enchantments = event.getEnchantsToAdd();
                    int enchantRarity = getEnchantRarity(enchantments);
                    ItemStack item = event.getItem();
                    if (getLootCode(plugin, item) == null) {
                        double seed = AcuteLoot.random.nextDouble();
                        chance = (seed + (enchantRarity / 300.0)) / 2.0;
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
        }
    }

    @EventHandler
    public void onPlayerHitMob(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Player){
            if(DeadEyeEffect.deadEyeArrowsShot.containsKey(event.getDamager())){
                // Cancel player's attacks while in Dead Eye
                event.setCancelled(true);
            }
        }

        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                if (plugin.getConfig().getBoolean("effects.enabled") && ((Player) arrow.getShooter()).getInventory()
                                                                                                     .getItemInMainHand()
                                                                                                     .getType() == Material.BOW) {
                    if(event.getEntity() instanceof LivingEntity) {
                        if(arrow.hasMetadata("deadEye")) {
                            LivingEntity livingEntity = (LivingEntity) event.getEntity();
                            livingEntity.setMaximumNoDamageTicks(0);
                            livingEntity.setNoDamageTicks(0);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    livingEntity.setNoDamageTicks(0);
                                }
                            }.runTaskLater(plugin, 1L);
                            // Set maximumNoDamageTicks back to default after all arrows have hit
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    livingEntity.setMaximumNoDamageTicks(20);
                                }

                            }.runTaskLater(plugin, 20L);
                        }
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
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(event.getEntity().hasMetadata("turnedToStone")) {
            String message = plugin.getConfig().getString("effects.medusa.death-message");
            event.setDeathMessage(event.getEntity().getDisplayName() + " " + message);
            event.getEntity().removeMetadata("turnedToStone", plugin);
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Populate loot chest
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block.getType() == Material.CHEST) {
                Chest chest = (Chest) block.getState();
                NamespacedKey key = new NamespacedKey(plugin, "chestMetadataKey");
                PersistentDataContainer container = chest.getPersistentDataContainer();
                // Only naturally-generated chests have lootTables
                // And they only have the lootTable the very first time they are opened
                if (AcuteLoot.debug || chest.getLootTable() != null || container.has(key, PersistentDataType.STRING)) {
                    if(container.has(key, PersistentDataType.STRING)){
                        if (AcuteLoot.debug) player.sendMessage("Has metadata code: "
                                + container.get(key, PersistentDataType.STRING));
                        String[] chestMetadataCode = container.get(key, PersistentDataType.STRING).split(":");
                        String version = chestMetadataCode[0];
                        long timeStamp = Long.parseLong(chestMetadataCode[1]);
                        int refillCooldown = Integer.parseInt(chestMetadataCode[2]);
                        if(refillCooldown != -1){
                            if(System.currentTimeMillis() < timeStamp + ((double) refillCooldown * 60000d)){
                                double remainingCooldown = (timeStamp + ((double) refillCooldown * 60000d)
                                        - System.currentTimeMillis());
                                int seconds = (int) (remainingCooldown / 1000) % 60 ;
                                int minutes = (int) ((remainingCooldown / (1000*60)) % 60);
                                int hours   = (int) (remainingCooldown / (1000*60*60));
                                if(plugin.getConfig().getBoolean("loot-sources.chests.show-cooldown-msg")){
                                    player.sendMessage(String.format(AcuteLoot.CHAT_PREFIX
                                                    + Util.getUIString("chests.cooldown-remaining", plugin)
                                                    + " %d:%d:%d",
                                            hours, minutes, seconds));
                                }
                                return;
                            }
                            else {
                                // Opened AcuteLoot chest with expired cooldown
                                // Reset timestamp
                                //TODO: Add sound?
                                container.set(key, PersistentDataType.STRING, String.format("%s:%d:%d", version,
                                        System.currentTimeMillis(), refillCooldown));
                                chest.update();
                            }
                        }
                        else {
                            // Opened AcuteLoot chest with no cooldown
                            // Remove persistentDataContainer
                            container.remove(key);
                            chest.update();
                        }
                    }

                    if (plugin.getConfig().getBoolean("loot-sources.chests.enabled")) {
                        double roll = AcuteLoot.random.nextDouble();
                        double chance = plugin.getConfig().getDouble("loot-sources.chests.chance") / 100.0;
                        if (AcuteLoot.debug) {
                            player.sendMessage("Chance: " + chance);
                            player.sendMessage("Roll: " + roll);
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
                                slotIndex = AcuteLoot.random.nextInt(emptySlots.size());
                                int slotToFill = emptySlots.get(slotIndex);
                                chest.getInventory().setItem(slotToFill, newLoot);
                                emptySlots.remove(slotIndex);

                                for (int i = 1; i <= origEmptySlotsSize - 1; i++) {
                                    roll = AcuteLoot.random.nextDouble();
                                    if (AcuteLoot.debug) player.sendMessage("Roll: " + roll);
                                    if (roll <= chance) {
                                        newLoot = createLootItem();
                                        slotIndex = AcuteLoot.random.nextInt(emptySlots.size());
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

            if (plugin.getConfig().getBoolean("effects.enabled") && player.getInventory().getChestplate() != null) {
                String lootCode = getLootCode(plugin, player.getInventory().getChestplate());
                if (lootCode != null) {
                    LootItem loot = new LootItem(lootCode);
                    List<LootSpecialEffect> effects = loot.getEffects();
                    effects.forEach(e -> e.apply(event));
                }
            }
        }
    }

    @EventHandler()
    public void onDropItem(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        if (plugin.getConfig().getBoolean("effects.enabled") && player.getInventory().getChestplate() != null) {
            String lootCode = getLootCode(plugin, player.getInventory().getChestplate());
            if (lootCode != null) {
                LootItem loot = new LootItem(lootCode);
                List<LootSpecialEffect> effects = loot.getEffects();
                effects.forEach(e -> e.apply(event));
            }
        }
        /*
        // May be used in the future to prevent Dead Eye from activating while viewing inventory
        // This "bug" could potentially be fixed by putting the entire Dead Eye logic into a Runnable that
        // executes in the next tick instead of the current tick. This would allow this event to mark that
        // PlayerInteractEvent was actually associated with dropping an item out of inventory and NOT a true
        // interaction. This event is called after PlayerInteractEvent so without delaying it by a tick,
        // setting the association won't do anything.

        // Example of what this event could do to mark its association with PlayerInteractEvent
        deadEyeArrowsShot.putIfAbsent((Player) event.getPlayer(), -3);
         */
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

    public static ItemStack chooseLootMaterial(){
        int materialIndex = AcuteLoot.random.nextInt(materials.size());
        ItemStack item = new ItemStack(materials.get(materialIndex), 1);

        // Set random damage if Material is damageable
        if (item.getItemMeta() instanceof Damageable && item.getType().getMaxDurability() > 0) {
            Damageable dmgItemMeta = (Damageable) item.getItemMeta();
            int damage = AcuteLoot.random.nextInt(item.getType().getMaxDurability());
            dmgItemMeta.setDamage(damage);
            item.setItemMeta((ItemMeta) dmgItemMeta);
        }
        return item;
    }

    // Create Loot Item with RANDOM material
    public ItemStack createLootItem() {
        ItemStack item = chooseLootMaterial();
        return createLootItem(item, AcuteLoot.random.nextDouble());
    }


    // Create Loot Item with GIVEN material
    public ItemStack createLootItem(ItemStack item, double rarity) {
        // Generate loot: name, rarity and effects
        LootItemGenerator generator = new LootItemGenerator(AcuteLoot.rarityChancePool, AcuteLoot.effectChancePool);
        final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(item.getType());
        if (lootMaterial.equals(LootMaterial.UNKNOWN)) {
            return item;
        }
        return createLootItem(item, generator.generate(rarity, lootMaterial));
    }

    //public ItemStack createLootItem(ItemStack item, int rarityID, List<Integer> effects) {
    //    return createLootItem(item, new LootItem(rarityID, effects));
    //}

    public ItemStack createLootItem(ItemStack item, LootRarity rarity) {
        LootItemGenerator generator = new LootItemGenerator(AcuteLoot.rarityChancePool, AcuteLoot.effectChancePool);
        final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(item.getType());
        if (lootMaterial.equals(LootMaterial.UNKNOWN)) {
            return item;
        }
        return createLootItem(item, generator.generateWithRarity(rarity, lootMaterial));
    }

    public ItemStack createLootItem(ItemStack item, final LootItem loot) {
        final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(item.getType());
        if (lootMaterial.equals(LootMaterial.UNKNOWN)) {
            return item;
        }

        String name = null;
        int attempts = 100;
        NameGenerator nameGenerator = null;
        do {
            try {
                nameGenerator = AcuteLoot.nameGenChancePool.draw();
                name = nameGenerator.generate(lootMaterial, loot.rarity());
            } catch (NoSuchElementException e) {
                // Couldn't draw a name for some reason, try again
                attempts--;
            }
        } while (name == null && attempts > 0);
        if (attempts == 0) {
            plugin.getLogger().severe("Could not generate a name in 100 attempts! Are name files empty or corrupted?");
            plugin.getLogger().severe("Name Generator: " + nameGenerator.toString());
        }

        // Add loot info to lore and display name
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();

        // Store lootCode in metadata using PersistentDataHolder API
        NamespacedKey key = new NamespacedKey(plugin, "lootCodeKey");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, loot.lootCode());

        // Add loot info to lore
        lore.add(loot.rarity().getRarityColor() + loot.rarity().getName());
        for (LootSpecialEffect effect : loot.getEffects()) {
            //String effectName = plugin.getConfig().getString("effects." + effect.getName().replace("_", ".") + ".name");
            String effectName = effect.getDisplayName();
            lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("loot-effect-color")) + effectName);
        }

        // Add category lore
        if(plugin.getConfig().getBoolean("loot-category-lore.enabled")){
            String category = lootMaterial.name().toLowerCase();
            if(plugin.getConfig().contains("loot-category-lore." + category)){
                List<String> loreLines = plugin.getConfig().getStringList("loot-category-lore." + category);
                for (String line : loreLines){
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
            else{
                plugin.getLogger().warning("ERROR: Failed to add lore from config: loot-category-lore." +
                        lootMaterial.name());
            }
        }
        meta.setLore(lore);

        // Set display name
        if(plugin.getConfig().getBoolean("global-loot-name-color"))
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("loot-name-color")) + name);
        else
            meta.setDisplayName(loot.rarity().getRarityColor() + name);
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
                // Block Trail Effect and Time Walk Effect and Midas Effect
                // Check if player moves between a block
                if (player.isOnGround() && (event.getFrom().getBlockX() != (event.getTo().getBlockX())
                        || event.getFrom().getBlockZ() != event.getTo().getBlockZ()
                        || event.getFrom().getBlockY() != event.getTo().getBlockY())) {
                        effects.forEach(e -> e.apply(event));
                }
            }
        }
        if (plugin.getConfig().getBoolean("effects.enabled") && player.getInventory().getChestplate() != null) {
            String lootCode = getLootCode(plugin, player.getInventory().getChestplate());
            if (lootCode != null) {
                LootItem loot = new LootItem(lootCode);
                List<LootSpecialEffect> effects = loot.getEffects();
                effects.forEach(e -> e.apply(event));
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
        if(event.getEntity() instanceof Skeleton && event.getEntity().hasPotionEffect(PotionEffectType.SLOW) && event.getEntity().hasMetadata("deadEyeSlowness")) {
            // It ruins the Dead Eye slo-mo effect when skeletons can shoot you during it
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if(DeadEyeEffect.deadEyeArrowsShot.get(player) != null && event.getItemDrop().getItemStack().getType().equals(Material.BOW) && event.getItemDrop().getItemStack().hasItemMeta() && event.getItemDrop().getItemStack().getItemMeta().hasLore()){
            ItemMeta bowMeta = event.getItemDrop().getItemStack().getItemMeta();
            List<String> bowLore = bowMeta.getLore();
            if(bowLore.get(bowLore.size() - 1 ).contains("Activated")) {
                bowLore.remove(bowLore.size() - 1);
                bowMeta.setLore(bowLore);
                event.getItemDrop().getItemStack().setItemMeta(bowMeta);
            }
        }
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (plugin.getConfig().getBoolean("effects.enabled") && player.getInventory().getChestplate() != null) {
                String lootCode = getLootCode(plugin, player.getInventory().getChestplate());
                if (lootCode != null) {
                    LootItem loot = new LootItem(lootCode);
                    List<LootSpecialEffect> effects = loot.getEffects();
                    effects.forEach(e -> e.apply(event));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerStatisticIncrement(PlayerStatisticIncrementEvent event){
        Player player = event.getPlayer();
        if (plugin.getConfig().getBoolean("effects.enabled") && player.getInventory().getBoots() != null) {
            String lootCode = getLootCode(plugin, player.getInventory().getBoots());
            if (lootCode != null) {
                LootItem loot = new LootItem(lootCode);
                List<LootSpecialEffect> effects = loot.getEffects();
                effects.forEach(e -> e.apply(event));
            }
        }
    }

    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        if (plugin.getConfig().getBoolean("effects.enabled") && player.getInventory().getChestplate() != null) {
            String lootCode = getLootCode(plugin, player.getInventory().getChestplate());
            if (lootCode != null) {
                LootItem loot = new LootItem(lootCode);
                List<LootSpecialEffect> effects = loot.getEffects();
                effects.forEach(e -> e.apply(event));
            }
        }
    }

    @EventHandler
    public void onPlayerEnterPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        if (plugin.getConfig().getBoolean("effects.enabled") && player.getInventory().getChestplate() != null) {
            String lootCode = getLootCode(plugin, player.getInventory().getChestplate());
            if (lootCode != null) {
                LootItem loot = new LootItem(lootCode);
                List<LootSpecialEffect> effects = loot.getEffects();
                effects.forEach(e -> e.apply(event));
            }
        }
    }

    @EventHandler
    public void onPlayerHoldItem(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (plugin.getConfig().getBoolean("effects.enabled") && player.getInventory().getChestplate() != null) {
            String lootCode = getLootCode(plugin, player.getInventory().getChestplate());
            if (lootCode != null) {
                LootItem loot = new LootItem(lootCode);
                List<LootSpecialEffect> effects = loot.getEffects();
                effects.forEach(e -> e.apply(event));
            }
        }
    }

    @EventHandler
    public void onPlayerClickTonight(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (plugin.getConfig().getBoolean("effects.enabled") && player.getInventory().getChestplate() != null) {
            String lootCode = getLootCode(plugin, player.getInventory().getChestplate());
            if (lootCode != null) {
                LootItem loot = new LootItem(lootCode);
                List<LootSpecialEffect> effects = loot.getEffects();
                effects.forEach(e -> e.apply(event));
            }
        }
    }

}
