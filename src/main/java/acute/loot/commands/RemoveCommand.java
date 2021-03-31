package acute.loot.commands;

import acute.loot.AcuteLoot;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Remove command class.
 */
public class RemoveCommand extends AcuteLootCommand<Player> {

    public RemoveCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    @Override
    protected void doHandle(Player sender, String[] args) {
        ItemStack item = sender.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR) {
            if (plugin().getLootCode(item) != null) {
                ItemMeta meta = item.getItemMeta();
                meta.setLore(new ArrayList<>());
                meta.setDisplayName(null);
                NamespacedKey key = new NamespacedKey(plugin(), "lootCodeKey");
                meta.getPersistentDataContainer().remove(key);
                item.setItemMeta(meta);
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "AcuteLoot removed");
            } else {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Item is not AcuteLoot");
            }
        } else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "You must be holding something");
        }
    }
}
