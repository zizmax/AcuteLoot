package acute.loot.commands;

import acute.loot.AcuteLoot;
import acute.loot.LootItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import static acute.loot.AcuteLoot.*;

public class RepairCommand extends AcuteLootCommand<Player> {

    public RepairCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    @Override
    protected void doHandle(Player sender, String[] args) {
        final ItemStack item = sender.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR) {

            if (plugin().getLootCode(item) != null) {
                sender.sendMessage(CHAT_PREFIX + "Item is already AcuteLoot!");
                return;
            }

            try {
                final LootItem lootItem = plugin().lootGenerator.repairItem(item);

                sender.sendMessage(CHAT_PREFIX + ChatColor.YELLOW + "Repaired AcuteLoot:");
                sender.sendMessage(CHAT_PREFIX + ChatColor.YELLOW + "Rarity: " + ChatColor.RESET + lootItem.rarity().getName());
                sender.sendMessage(CHAT_PREFIX + ChatColor.YELLOW + "Effects:");
                if (lootItem.getEffects().isEmpty()) {
                    sender.sendMessage("[NONE]");
                } else {
                    lootItem.getEffects().forEach(e -> sender.sendMessage(CHAT_PREFIX + " - " + e.getDisplayName()));
                }

                NamespacedKey key = new NamespacedKey(plugin(), "lootCodeKey");
                final ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, lootItem.lootCode());
                item.setItemMeta(meta);

            } catch (UnsupportedOperationException e) {
                sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Failed to repair item:");
                sender.sendMessage(CHAT_PREFIX + ChatColor.RED + e.getMessage());
            }
        } else {
            sender.sendMessage(CHAT_PREFIX + "You must be holding something");
        }
    }
}