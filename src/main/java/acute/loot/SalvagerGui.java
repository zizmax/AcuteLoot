package acute.loot;

import com.github.phillip.h.acutelib.util.Util;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Salvager feature GUI class.
 */
public class SalvagerGui implements Listener {
    private final Inventory inv;
    private final AcuteLoot plugin;
    private ItemStack salvaged;
    private List<String> commandsToRun;
    private List<Integer> slotsToGive;

    //FIXME: What happens to items if player dies or gets teleported while looking at SalvageGUI?
    //FIXME: ...if InventoryCloseEvent is triggered, just catch it there. Otherwise will need to cover all events.
    //FIXME: ARMOR_STAND sound plays even when successfully salvaging

    /**
     * Initializes salvager inventory object.
     *
     * @param plugin AcuteLoot object
     */
    public SalvagerGui(AcuteLoot plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        inv = Bukkit.createInventory(null, 54, plugin.getUiString("salvage.inv-name"));
        this.plugin = plugin;
        configureItems();
    }

    private void configureItems() {
        salvaged = null;
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
        }

        inv.setItem(8, createItem(Material.OAK_SIGN, plugin.getUiString("salvage.help-name")));

        // Add help-lore from config
        List<String> loreLines = plugin.getConfig().getStringList("msg.salvage.help-lore");
        ItemMeta meta = inv.getItem(8).getItemMeta();
        List<String> lore = new ArrayList();
        for (String line : loreLines) {
            lore.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        inv.getItem(8).setItemMeta(meta);

        //ChatColor.LIGHT_PURPLE + "Click the " + salvagerName + ChatColor.LIGHT_PURPLE + " to see output",
        //ChatColor.LIGHT_PURPLE + "Then click the " + ChatColor.GREEN + "green" + ChatColor.LIGHT_PURPLE + " pane to confirm"));
        inv.setItem(13, new ItemStack(Material.AIR));

        for (int i = 27; i < 45; i++) {
            inv.setItem(i, createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " "));
        }
        for (int i = 45; i < 53; i++) {
            inv.setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
        }

        inv.setItem(53, createItem(Material.YELLOW_STAINED_GLASS_PANE, plugin.getUiString("salvage.pre-salvage-button")));
        inv.setItem(45, createItem(Material.RED_STAINED_GLASS_PANE, plugin.getUiString("salvage.cancel-button")));
        inv.setItem(22, createItem(Material.HOPPER, plugin.getUiString("salvage.name")));
    }

    protected ItemStack createItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public void openInventory(final HumanEntity entity) {
        entity.openInventory(inv);
    }

    private void denyUiClick(Player player, String reason) {
        player.sendMessage(AcuteLoot.CHAT_PREFIX + reason);
        player.playSound(player.getLocation(), Sound.ENTITY_ARMOR_STAND_HIT, 1, 1);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event.getInventory() != inv) {
            return;
        }
        final ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType() == Material.AIR) {
            return;
        }
        final Player player = (Player) event.getWhoClicked();
        if (plugin.debug) {
            player.sendMessage("Clicked slot: " + event.getRawSlot());
        }

        if (event.getRawSlot() == 22) {
            event.setCancelled(true);
            if (inv.getItem(13) != null) {
                if (salvaged == null) {
                    commandsToRun = new ArrayList<>();
                    slotsToGive = new ArrayList<>();
                    if (inv.getItem(13).getAmount() == 1) {
                        if (plugin.getLootCode(inv.getItem(13)) != null) {
                            for (String key : plugin.getConfig()
                                                    .getConfigurationSection("salvager-drops")
                                                    .getKeys(false)) {
                                int id;
                                try {
                                    id = Integer.parseInt(key);
                                } catch (Exception e) {
                                    player.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.RED + "Error: " +
                                                       ChatColor.GRAY + "Rarity ID error in salvager config. Contact server admin.");
                                    plugin.getLogger().severe("Salvager config error on rarity ID: " + key);
                                    plugin.getLogger().severe("Are you sure the ID is an integer?");
                                    event.setCancelled(true);
                                    return;
                                }

                            }
                            LootItem lootItem = new LootItem(plugin.getLootCode(inv.getItem(13)));
                            String node = "salvager-drops." + lootItem.rarityRaw() + ".";
                            if (plugin.debug) {
                                player.sendMessage(String.valueOf(plugin.getConfig()
                                                                        .getConfigurationSection(node)
                                                                        .getKeys(false)));
                            }
                            for (String outputId : plugin.getConfig().getConfigurationSection(node).getKeys(false)) {
                                String outputIdNode = node + outputId + ".";
                                if (plugin.debug) {
                                    plugin.getLogger().info("Output ID: " + outputId);
                                    plugin.getLogger().info("OutputIDNode: " + outputIdNode);
                                }
                                List<String> materialStrings = plugin.getConfig()
                                                                     .getStringList(outputIdNode + "loot-types");
                                for (String materialString : materialStrings) {
                                    materialString = materialString.toUpperCase();
                                    //TODO: Add check if string even exists in the enum, not only if null
                                    if (materialString != null) {
                                        if (LootMaterial.lootMaterialForMaterial(inv.getItem(13).getType())
                                                        .name()
                                                        .equals(materialString)) {
                                            for (String dropId : plugin.getConfig()
                                                                       .getConfigurationSection(outputIdNode + "drops.")
                                                                       .getKeys(false)) {
                                                String dropIdNode = outputIdNode + "drops." + dropId + ".";
                                                if (plugin.debug) {
                                                    plugin.getLogger().info("dropIdNode: " + dropIdNode);
                                                }
                                                if (plugin.debug) {
                                                    plugin.getLogger()
                                                          .info("Material String: " + plugin.getConfig()
                                                                                            .getString(dropIdNode + "material"));
                                                }
                                                Material material = Util.validateMaterial(plugin.getConfig()
                                                                                                .getString(dropIdNode +
                                                                                                          "material"));
                                                if (material != null) {
                                                    int slot = inv.first(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                                                    inv.setItem(slot, createItem(material,
                                                            ChatColor.translateAlternateColorCodes('&', ChatColor
                                                            .stripColor(plugin.getConfig()
                                                                              .getString(dropIdNode + "name")))));
                                                    if (plugin.getConfig().contains(dropIdNode + "commands")) {
                                                        List<String> commands = plugin.getConfig()
                                                                                      .getStringList(dropIdNode + "commands");
                                                        for (String command : commands) {
                                                            if (plugin.debug) {
                                                                plugin.getLogger().info("Found command: " + command);
                                                            }
                                                            commandsToRun.add(command);
                                                        }
                                                    }
                                                    if (plugin.getConfig().getBoolean(dropIdNode + "give-item")) {
                                                        slotsToGive.add(slot);
                                                    }
                                                    player.playSound(player.getLocation(), Sound.BLOCK_SMITHING_TABLE_USE, 1, 1);
                                                    inv.setItem(53, createItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN +
                                                            "Confirm"));
                                                    salvaged = inv.getItem(13);
                                                } else {
                                                    //FIXME: Better error string for players and admins
                                                    player.sendMessage("Error on drop output material! ID:" + dropIdNode);
                                                }
                                            }
                                        }
                                    } else {
                                        //FIXME: Better error string for players and admins
                                        player.sendMessage("Error on loot-type material! ID:" + outputIdNode);
                                    }
                                }
                            }
                            if (salvaged == null) {
                                denyUiClick(player, plugin.getUiString("salvage.not-salvagable"));
                            }
                        } else {
                            denyUiClick(player, plugin.getUiString("generic.not-acuteloot"));
                        }
                    } else {
                        denyUiClick(player, plugin.getUiString("salvage.not-single-item"));
                    }
                } else {
                    denyUiClick(player, plugin.getUiString("salvage.already-salvaged"));
                }
            } else {
                denyUiClick(player, plugin.getUiString("salvage.no-item"));
            }
            event.setCancelled(true);
        } else if (event.getRawSlot() == 53) {
            if (salvaged != null && salvaged.equals(inv.getItem(13))) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
                inv.setItem(13, null);
                for (int slot : slotsToGive) {
                    if (inv.getItem(slot) != null) {
                        player.getInventory().addItem(inv.getItem(slot));
                        inv.setItem(slot, createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " "));
                    }
                }
                for (String command : commandsToRun) {
                    if (plugin.debug) {
                        plugin.getLogger().info("AcuteLoot dispatching: " + command);
                    }
                    String parsedCommand = command.replace("{PLAYER}", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
                }
                slotsToGive.clear();
                commandsToRun.clear();
                event.setCancelled(true);
                configureItems();
            } else {
                denyUiClick(player, plugin.getUiString("salvage.no-item"));
                salvaged = null;
                if (inv.getItem(13) != null && !inv.getItem(13).getType().isAir()) {
                    player.getInventory().addItem(inv.getItem(13));
                }
                configureItems();
                event.setCancelled(true);
            }

        } else if (event.getRawSlot() == 45) {
            denyUiClick(player, plugin.getUiString("salvage.cancel"));
            player.closeInventory();
            event.setCancelled(true);
        } else if (event.getRawSlot() == 13) {
            // Moving salvage item around
            salvaged = null;
            //FIXME: Item will disappear if player's inventory is full
            player.getInventory().addItem(inv.getItem(13));
            configureItems();
            event.setCancelled(true);
        } else if (event.getRawSlot() >= 54) {
            // Do nothing because it's in the player's own inventory
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_ARMOR_STAND_HIT, 1, 1);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryDragEvent event) {
        if (event.getInventory() == inv) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (event.getInventory() == inv) {
            Player player = (Player) event.getPlayer();
            if (event.getInventory().getItem(13) != null) {
                player.getInventory().addItem(event.getInventory().getItem(13));
            }
        }
    }
}