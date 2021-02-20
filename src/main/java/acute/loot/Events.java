package acute.loot;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Events implements Listener {

    private static final Random random = AcuteLoot.random;
    private final AcuteLoot plugin;

    public Events(AcuteLoot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void anvilListener(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        if (event.getViewers().isEmpty() || inv.getItem(0) == null || Events.getLootCode(plugin, inv.getItem(0)) == null) return;
        if (inv.getItem(0).hasItemMeta() && inv.getItem(0).getItemMeta().hasDisplayName()) {
            String origName = getDisplayName(inv.getItem(0));
            ItemStack result = event.getResult();

            if (result != null && !result.getType().equals(Material.AIR) && origName.contains(String.valueOf('§'))) {
                String newName = origName.substring(0,2) + result.getItemMeta().getDisplayName();
                ItemMeta meta = result.getItemMeta();
                meta.setDisplayName(newName);
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
            double roll = random.nextDouble();
            double chance = plugin.getConfig().getDouble("loot-sources.enchanting.chance") / 100.0;
            if (plugin.debug) {
                player.sendMessage("Roll: " + roll);
                player.sendMessage("Raw chance: " + chance);
            }
            if (!plugin.getConfig().getBoolean("use-permissions") || (plugin.getConfig().getBoolean("use-permissions") && player.hasPermission("acuteloot.enchant"))) {
                if (roll <= chance) {
                    Map<Enchantment, Integer> enchantments = event.getEnchantsToAdd();
                    int enchantRarity = getEnchantRarity(enchantments);
                    ItemStack item = event.getItem();
                    if (getLootCode(plugin, item) == null) {
                        double seed = random.nextDouble();
                        chance = (seed + (enchantRarity / 300.0)) / 2.0;
                        item = plugin.lootGenerator.createLootItem(item, chance);
                        if (plugin.debug) {
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
                if (!player.getWorld().getBlockAt(chest.getLocation().add(0,1,0)).getType().isOccluding()){
                    // Chests can only be opened when no opaque (occluding) block exists above
                    // Without this check AL chest-filling logic would run even if chest never actually opened
                    NamespacedKey key = new NamespacedKey(plugin, "chestMetadataKey");
                    PersistentDataContainer container = chest.getPersistentDataContainer();
                    // Only naturally-generated chests have lootTables
                    // And they only have the lootTable the very first time they are opened
                    if (plugin.debug || chest.getLootTable() != null || container.has(key, PersistentDataType.STRING)) {
                        if(container.has(key, PersistentDataType.STRING)){
                            if (plugin.debug) player.sendMessage("Has metadata code: "
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
                                                        + plugin.getUIString("chests.cooldown-remaining")
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
                            // Runs chest-filling logic on next tick to account for items placed by vanilla loot table
                            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    double roll = random.nextDouble();
                                    double chance = plugin.getConfig().getDouble("loot-sources.chests.chance") / 100.0;
                                    if (plugin.debug) {
                                        player.sendMessage("Chance: " + chance);
                                        player.sendMessage("Roll: " + roll);
                                    }
                                    if (roll <= chance) {
                                        List<Integer> emptySlots = new ArrayList<>(); // Array of the empty slot indices
                                        int slotIndex = 0;
                                        for (ItemStack itemStack : chest.getInventory().getContents()) {
                                            // Only add null (empty) slots so they can later be filled with loot
                                            if (itemStack == null) emptySlots.add(slotIndex);
                                            slotIndex++;
                                        }
                                        int origEmptySlotsSize = emptySlots.size();
                                        if (emptySlots.size() > 0) {
                                            ItemStack newLoot = plugin.lootGenerator.createLootItem();
                                            slotIndex = random.nextInt(emptySlots.size());
                                            int slotToFill = emptySlots.get(slotIndex);
                                            chest.getInventory().setItem(slotToFill, newLoot);
                                            emptySlots.remove(slotIndex);

                                            for (int i = 1; i <= origEmptySlotsSize - 1; i++) {
                                                roll = random.nextDouble();
                                                if (plugin.debug) player.sendMessage("Roll: " + roll);
                                                if (roll <= chance) {
                                                    newLoot = plugin.lootGenerator.createLootItem();
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
                            });
                        }

                    }
                }
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
            applyPlayerEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    // Priority HIGH to avoid conflicts with other plugins that modify fishing
    public void onPlayerFish(PlayerFishEvent event) {
        Entity caught = event.getCaught();
        if (caught instanceof Item && event.getExpToDrop() > 0) {
            if (plugin.getConfig().getBoolean("loot-sources.fishing.enabled")) {
                double roll = random.nextDouble();
                double chance = plugin.getConfig().getDouble("loot-sources.fishing.chance") / 100.0;
                if (plugin.debug) {
                    event.getPlayer().sendMessage("Roll: " + roll);
                    event.getPlayer().sendMessage("Raw chance: " + chance);
                }
                // 2.1% is ~amount that chance of treasure goes up for each Luck of the Sea level
                chance = chance + event.getPlayer()
                                       .getInventory()
                                       .getItemInMainHand()
                                       .getEnchantmentLevel(Enchantment.LUCK) * .021;
                if (plugin.debug)
                    event.getPlayer().sendMessage("Enchanted chance: " + chance);
                if (roll <= chance) {
                    random.nextInt();
                    Item itemEntity = (Item) caught;
                    ItemStack item = plugin.lootGenerator.createLootItem();
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

    public int getEnchantRarity(Map<Enchantment, Integer> enchantments) {
        double total_levels = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            total_levels = total_levels + (float) entry.getValue() / entry.getKey().getMaxLevel() * 100.0;
        }
        return Math.min((int) total_levels, 300);

    }

    public static String getLootCode(AcuteLoot plugin, ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
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

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isOnGround() && (event.getFrom().getBlockX() != (event.getTo().getBlockX())
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()
                || event.getFrom().getBlockY() != event.getTo().getBlockY())) {
            applyPlayerEvent(event);
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            applyEventWithPlayer(event, (Player) event.getEntity());
        }

        if (event.getEntity() instanceof Skeleton &&
            event.getEntity().hasPotionEffect(PotionEffectType.SLOW) &&
            event.getEntity().hasMetadata("deadEyeSlowness")) {
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

    //XP Boost Effect
    @EventHandler
    public void onExpGain(PlayerExpChangeEvent event) {
        applyPlayerEvent(event);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event){
        applyPlayerEvent(event);
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
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            applyEventWithPlayer(event, (Player) event.getEntity());
        }
    }

    @EventHandler
    public void onPlayerStatisticIncrement(PlayerStatisticIncrementEvent event){
        applyPlayerEvent(event);
    }

    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        applyPlayerEvent(event);
    }

    @EventHandler
    public void onPlayerEnterPortal(PlayerPortalEvent event) {
        applyPlayerEvent(event);
    }

    @EventHandler
    public void onPlayerHoldItem(PlayerItemHeldEvent event) {
        applyPlayerEvent(event);
    }

    @EventHandler
    public void onPlayerClickTonight(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            applyEventWithPlayer(event, (Player) event.getWhoClicked());
        }
    }

    private void applyPlayerEvent(final PlayerEvent event) {
        applyEventWithPlayer(event, event.getPlayer());
    }

    private void applyEventWithPlayer(final Event event, final Player player) {
        applyEventToItem(event, player.getInventory().getItemInMainHand());
        applyEventToItem(event, player.getInventory().getItemInOffHand());
        applyEventToItem(event, player.getInventory().getHelmet());
        applyEventToItem(event, player.getInventory().getChestplate());
        applyEventToItem(event, player.getInventory().getLeggings());
        applyEventToItem(event, player.getInventory().getBoots());
    }

    private void applyEventToItem(final Event event, final ItemStack item) {
        if (!plugin.getConfig().getBoolean("effects.enabled")) {
            return;
        }
        String lootCode = getLootCode(plugin, item);
        if (lootCode != null) {
            new LootItem(lootCode).getEffects().forEach(e -> e.apply(event));
        }
    }

}
