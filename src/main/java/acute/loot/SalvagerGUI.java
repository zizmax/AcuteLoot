package acute.loot;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SalvagerGUI implements Listener {
    private final Inventory inv;
    private final AcuteLoot plugin;
    private ItemStack salvaged;
    private HashMap<Integer, String> commandsToRun;

    //FIXME: What happens to items if player dies or gets teleported while looking at SalvageGUI?
    //FIXME: ...if InventoryCloseEvent is triggered, just catch it there. Otherwise will need to cover all events.
    //FIXME: ARMOR_STAND sound plays even when successfully salvaging

    public SalvagerGUI(AcuteLoot plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        inv = Bukkit.createInventory(null, 54, AcuteLoot.CHAT_PREFIX + "Salvager");
        ItemStack salvaged = null;
        HashMap<Integer, String> commandsToRun = new HashMap<Integer, String>();
        this.plugin = plugin;
        configureItems();
    }

    // You can call this whenever you want to put the items in
    public void configureItems() {
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE," "));

        }
        String salvagerName = ChatColor.BOLD.toString() + ChatColor.DARK_RED + "Salvager";
        inv.setItem(8, createItem(Material.OAK_SIGN, ChatColor.RESET + "Salvager Help",
                ChatColor.LIGHT_PURPLE + "Click the " + salvagerName + ChatColor.LIGHT_PURPLE + " to see output",
                ChatColor.LIGHT_PURPLE + "Then click the " + ChatColor.GREEN + "green" + ChatColor.LIGHT_PURPLE + " pane to confirm"));
        inv.setItem(13, new ItemStack(Material.AIR));

        for (int i = 27; i < 45; i++) {
            inv.setItem(i, createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " "));
        }
        for (int i = 45; i < 53; i++) {
            inv.setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE," "));
        }

        inv.setItem(53, createItem(Material.YELLOW_STAINED_GLASS_PANE,ChatColor.LIGHT_PURPLE + "Click " + salvagerName + ChatColor.LIGHT_PURPLE + " first"));
        inv.setItem(45, createItem(Material.RED_STAINED_GLASS_PANE,ChatColor.RED + "Cancel"));
        inv.setItem(22, createItem(Material.HOPPER, salvagerName));
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

    private void denyUIClick(Player player, String reason){
        player.sendMessage(AcuteLoot.CHAT_PREFIX + reason);
        player.playSound(player.getLocation(), Sound.ENTITY_ARMOR_STAND_HIT, 1, 1);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event.getInventory() != inv) return;
        final ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType() == Material.AIR) return;
        final Player player = (Player) event.getWhoClicked();
        if (AcuteLoot.debug) player.sendMessage("Clicked slot: " + event.getRawSlot());

        if (event.getRawSlot() == 22){
            event.setCancelled(true);
            if (inv.getItem(13) != null) {
                if (salvaged == null) {
                    commandsToRun = new HashMap<>();
                    if (Events.getLootCode(plugin, inv.getItem(13)) != null) {
                        for (String key : plugin.getConfig().getConfigurationSection("salvager-drops").getKeys(false)) {
                            int id;
                            try {
                                id = Integer.parseInt(key);
                            } catch (Exception e) {
                                player.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.RED + "Error: " + ChatColor.GRAY
                                        + "Rarity ID error in salvager config. Contact server admin.");
                                plugin.getLogger().severe("Salvager config error on rarity ID: " + key);
                                plugin.getLogger().severe("Are you sure the ID is an integer?");
                                event.setCancelled(true);
                                return;
                            }

                        }
                        String node = "salvager-drops.100.";
                        if (AcuteLoot.debug) player.sendMessage(String.valueOf(plugin.getConfig().getConfigurationSection(node).getKeys(false)));
                        for (String outputID : plugin.getConfig().getConfigurationSection(node).getKeys(false)) {
                            String outputIDNode = node + outputID + ".";
                            if (AcuteLoot.debug) plugin.getLogger().info("Output ID: " + outputID);
                            if (AcuteLoot.debug) plugin.getLogger().info("OutputIDNode: " + outputIDNode);
                            String[] materialStrings = plugin.getConfig().getString(outputIDNode + "loot-types").split(",");
                            for (String materialString : materialStrings) {
                                Material material = Util.validateMaterial(materialString);
                                if (material != null) {
                                    if (material == inv.getItem(13).getType()) {
                                        for (String dropID : plugin.getConfig().getConfigurationSection(outputIDNode + "drops.").getKeys(false)) {
                                            String dropIDNode = outputIDNode + "drops." + dropID + ".";
                                            if (AcuteLoot.debug) plugin.getLogger().info("dropIDNode: " + dropIDNode);
                                            if (AcuteLoot.debug) plugin.getLogger().info("Material String: " + plugin.getConfig().getString(dropIDNode + "material"));
                                            material = Util.validateMaterial(plugin.getConfig().getString(dropIDNode + "material"));
                                            if (material != null) {
                                                int slot = inv.first(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                                                inv.setItem(slot, createItem(material, ChatColor.translateAlternateColorCodes('&', ChatColor.stripColor(plugin.getConfig().getString(dropIDNode + "name")))));
                                                if (plugin.getConfig().getString(dropIDNode + "command") != null
                                                        && plugin.getConfig().getString(dropIDNode + "command") != "") {
                                                    if (AcuteLoot.debug) plugin.getLogger().info("Found command: " + plugin.getConfig().getString(dropIDNode + "command") + "|");
                                                    commandsToRun.put(slot, plugin.getConfig().getString(dropIDNode + "command"));
                                                }
                                                player.playSound(player.getLocation(), Sound.BLOCK_SMITHING_TABLE_USE, 1, 1);
                                                inv.setItem(53, createItem(Material.LIME_STAINED_GLASS_PANE,ChatColor.GREEN + "Confirm"));
                                                salvaged = inv.getItem(13);
                                            } else {
                                                //FIXME: Better error string for players and admins
                                                player.sendMessage("Error on drop output material! ID:" + dropIDNode);
                                            }
                                        }
                                    }
                                } else {
                                    //FIXME: Better error string for players and admins
                                    player.sendMessage("Error on loot-type material! ID:" + outputIDNode);
                                }
                            }
                        }
                        if (salvaged == null){
                            denyUIClick(player, "Input not salvagable");
                        }
                    } else {
                        denyUIClick(player, "Item is not AcuteLoot!");
                    }
                } else {
                    denyUIClick(player, "Already salvaged!");
                }
            }
            else{
                denyUIClick(player, "No item in salvage slot!");
            }
            event.setCancelled(true);
        }

        else if (event.getRawSlot() == 53){
            if (salvaged != null && salvaged.equals(inv.getItem(13) )){
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
                inv.setItem(13, null);
                for (int i = 27; i < 45; i++) {
                    if(inv.getItem(i) != null) {
                        if(commandsToRun.get(i) == null && inv.getItem(i).getType() != Material.LIGHT_GRAY_STAINED_GLASS_PANE) {
                            player.getInventory().addItem(inv.getItem(i));
                        }
                        inv.setItem(i, createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " "));
                    }
                }
                for (String command : commandsToRun.values()){
                    if (AcuteLoot.debug) plugin.getLogger().info("AcuteLoot running: " + command);
                    String parsedCommand = command.replace("{PLAYER}", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
                }
                commandsToRun.clear();
                event.setCancelled(true);
            }
            else{
                denyUIClick(player, "Nothing to salvage!");
                salvaged = null;
                if (inv.getItem(13) != null && !inv.getItem(13).getType().isAir())
                    player.getInventory().addItem(inv.getItem(13));
                configureItems();
                event.setCancelled(true);
            }

        }
        else if (event.getRawSlot() == 45){
            denyUIClick(player, "Salvage cancelled");
            player.closeInventory();
            event.setCancelled(true);
        }
        else if (event.getRawSlot() == 13){
            // Moving salvage item around
            salvaged = null;
            //FIXME: Item will disappear if player's inventory is full
            player.getInventory().addItem(inv.getItem(13));
            configureItems();
            event.setCancelled(true);
        }
        else if (event.getRawSlot() >= 54){
            // Do nothing because it's in the player's own inventory
        }

        else{
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
            if(event.getInventory().getItem(13) != null)
                player.getInventory().addItem(event.getInventory().getItem(13));
        }
    }
}