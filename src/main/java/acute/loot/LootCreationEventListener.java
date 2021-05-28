package acute.loot;

import base.util.UnorderedPair;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * Event listeners for creating loot.
 */
public class LootCreationEventListener implements Listener {

    private final AcuteLoot plugin;
    private final Random random = AcuteLoot.random;
    private final Map<Integer, ItemStack> anvilHistoryPairKey = new HashMap<>();
    private final Map<ItemStack, Integer> anvilHistoryItemKey = new HashMap<>();

    public LootCreationEventListener(AcuteLoot plugin) {
        this.plugin = plugin;
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
            if (!plugin.getConfig().getBoolean("use-permissions") || (plugin.getConfig()
                    .getBoolean("use-permissions") &&
                    player.hasPermission("acuteloot.enchant"))) {
                if (roll <= chance) {
                    Map<Enchantment, Integer> enchantments = event.getEnchantsToAdd();
                    int enchantRarity = getEnchantRarity(enchantments);
                    ItemStack item = event.getItem();
                    if (plugin.getLootCode(item) == null) {
                        double seed = random.nextDouble();
                        chance = (seed + (enchantRarity / 300.0)) / 2.0;
                        item = plugin.lootGenerator.createLootItem(item, chance);
                        if (plugin.debug) {
                            player.sendMessage(ChatColor.GOLD + "You enchanted a " + ChatColor.AQUA + item.getType()
                                                                                                          .toString());
                            player.sendMessage(ChatColor.GOLD + "It is called " + item.getItemMeta().getDisplayName());
                            player.sendMessage(ChatColor.GOLD + "Enchant Score: " + ChatColor.AQUA + enchantRarity);
                            player.sendMessage(ChatColor.GOLD + "Enchant Score Percentage: " + ChatColor.AQUA +
                                    String.format("%.2f%%", ((enchantRarity / 300.0) * 100.0)));
                            player.sendMessage(ChatColor.GOLD + "Seed: " + ChatColor.AQUA + String.format("%.2f%%", seed * 100.0));
                            player.sendMessage(ChatColor.GOLD + "Final Rarity Score: " + ChatColor.AQUA +
                                    String.format("%.2f%%", chance * 100.0));
                            player.sendMessage(ChatColor.GOLD + "Rarity: " + ChatColor.AQUA + item.getItemMeta()
                                                                                                  .getLore()
                                                                                                  .get(0));
                        }
                    }
                }
            }
        }
    }

    private int getEnchantRarity(Map<Enchantment, Integer> enchantments) {
        double totalLevels = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            totalLevels = totalLevels + (float) entry.getValue() / entry.getKey().getMaxLevel() * 100.0;
        }
        return Math.min((int) totalLevels, 300);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Populate loot chest
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block.getType() == Material.CHEST) {
            Chest chest = (Chest) block.getState();
            if (!player.getWorld().getBlockAt(chest.getLocation().add(0, 1, 0)).getType().isOccluding()) {
                // Chests can only be opened when no opaque (occluding) block exists above
                // Without this check AL chest-filling logic would run even if chest never actually opened

                // Get AL metadata from persisentDataContainer
                NamespacedKey key = new NamespacedKey(plugin, "chestMetadataKey");
                PersistentDataContainer container = chest.getPersistentDataContainer();
                String[] chestMetadataCode;
                if (container.has(key, PersistentDataType.STRING)) {
                    chestMetadataCode = container.get(key, PersistentDataType.STRING).split(":");
                } else {
                    chestMetadataCode = new String[]{"null", "-1000", "-1000"};
                }
                String version = chestMetadataCode[0];
                long timeStamp = Long.parseLong(chestMetadataCode[1]);
                int refillCooldown = Integer.parseInt(chestMetadataCode[2]);

                //refillCooldown >= 1: Standard cooldown measured in minutes
                //refillCooldown == -1: No cooldown, not yet opened (created with /al chest)
                //refillCooldown == -2: Expired chest


                // Only naturally-generated chests have lootTables
                // And they only have the lootTable the very first time they are opened
                if (plugin.debug || chest.getLootTable() != null || version != "null") {
                    if (version != "null") {
                        if (plugin.debug) {
                            player.sendMessage("Has metadata code: " + container.get(key, PersistentDataType.STRING));
                        }

                        if (refillCooldown >= 1) {
                            if (System.currentTimeMillis() < timeStamp + ((double) refillCooldown * 60000d)) {
                                double remainingCooldown = (timeStamp + ((double) refillCooldown * 60000d) - System.currentTimeMillis());
                                int seconds = (int) (remainingCooldown / 1000) % 60;
                                int minutes = (int) ((remainingCooldown / (1000 * 60)) % 60);
                                int hours = (int) (remainingCooldown / (1000 * 60 * 60));
                                if (plugin.getConfig().getBoolean("loot-sources.chests.show-cooldown-msg")) {
                                    player.sendMessage(String.format(AcuteLoot.CHAT_PREFIX +
                                                       plugin.getUiString("chests.cooldown-remaining") + " %d:%d:%d",
                                                       hours, minutes, seconds));
                                }
                                return;
                            } else {
                                // Opened AcuteLoot chest with expired cooldown
                                // Reset timestamp
                                //TODO: Add sound?
                                container.set(key, PersistentDataType.STRING, String.format("%s:%d:%d", version,
                                        System.currentTimeMillis(), refillCooldown));
                                chest.update();
                            }
                        } else if (refillCooldown == -2) {
                            // Opened expired AcuteLoot chest
                            return;
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
                                        if (itemStack == null) {
                                            emptySlots.add(slotIndex);
                                        }
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
                                            if (plugin.debug) {
                                                player.sendMessage("Roll: " + roll);
                                            }
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

                        // Create container to mark that chest has been looted, if it doesn't already exist
                        // Intended to fix Issue #21, possibly relating to the lootables config options in Paper
                        if (version == "null" || refillCooldown == -1) {
                            container.set(key, PersistentDataType.STRING, String.format("%s:%d:%d", version,
                                    System.currentTimeMillis(), -2));
                            chest.update();
                        }
                    }
                }
            }
        }
    }

    // Priority HIGH to avoid conflicts with other plugins that modify fishing
    @EventHandler(priority = EventPriority.HIGH)
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
                if (plugin.debug) {
                    event.getPlayer().sendMessage("Enchanted chance: " + chance);
                }
                if (roll <= chance) {
                    random.nextInt();
                    Item itemEntity = (Item) caught;
                    ItemStack item = plugin.lootGenerator.createLootItem();
                    // Turns out that unlike all the other trees, BAMBOO_SAPLING is NOT the material type
                    // BAMBOO_SAPLING appears to be the block material ONLY, so will error when applied to an ItemStack
                    // The original material lists include BAMBOO_SAPLING instead of the correct BAMBOO
                    // This is a sanity check in case someone is running on the old version
                    if (item.getType().equals(Material.BAMBOO_SAPLING)) {
                        item.setType(Material.BAMBOO);
                    }
                    itemEntity.setItemStack(item);
                }
            }
        }
    }


    @EventHandler
    public void anvilListener(PrepareAnvilEvent event) {
        Player player = (Player) event.getView().getPlayer();
        AnvilInventory inv = event.getInventory();
        if (event.getViewers().isEmpty() || inv.getItem(0) == null) {
            return;
        }

        String origName = getDisplayName(inv.getItem(0));
        ItemStack result = event.getResult();

        if (result != null && !result.getType().equals(Material.AIR)) {
            if (inv.getItem(0).hasItemMeta() && inv.getItem(0).getItemMeta().hasDisplayName()) {
                if (plugin.getLootCode(inv.getItem(0)) == null && origName.contains(String.valueOf('§'))) {
                    String newName = origName.substring(0, 2) + result.getItemMeta().getDisplayName();
                    ItemMeta meta = result.getItemMeta();
                    meta.setDisplayName(newName);
                    result.setItemMeta(meta);
                    event.setResult(result);
                }
            }
            if (plugin.getConfig().getBoolean("loot-sources.anvils.enabled")) {
                UnorderedPair pair = UnorderedPair.of(inv.getItem(0), inv.getItem(1));
                if (result.getType().equals(Material.SHIELD) || result.getType().equals(Material.ELYTRA)) {
                    //TODO Add configurable anvil chance
                    //TODO Check for anvil permission and register permission
                    //TODO Shield that is already AL gets overwritten since this block comes after first check

                    if (!anvilHistoryPairKey.containsKey(pair.hashCode())) {
                        double chance = AcuteLoot.random.nextDouble();
                        result = plugin.lootGenerator.createLootItem(result, chance);
                        anvilHistoryPairKey.put(pair.hashCode(), result);
                        anvilHistoryItemKey.put(result, pair.hashCode());
                        event.setResult(result);
                    } else {
                        event.setResult(anvilHistoryPairKey.get(pair.hashCode()));
                    }
                }
            }
        }
    }


    @EventHandler
    public void onPlayerFinishAnvil(InventoryClickEvent event) {
        Player player = (Player) event.getView().getPlayer();
        if (anvilHistoryItemKey.containsKey(event.getCurrentItem())) {
            anvilHistoryPairKey.remove(anvilHistoryItemKey.get(event.getCurrentItem()));
            anvilHistoryItemKey.remove(event.getCurrentItem());
        }
    }


    private String getDisplayName(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return "";
    }

    @EventHandler
    public void lootWellListener(PlayerDropItemEvent event) {
        if (plugin.debug) {
            plugin.lootWell.onWish(event);
        }
    }

}
