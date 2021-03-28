package acute.loot.commands;

import acute.loot.AcuteLoot;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Rename command class.
 */
public class RenameCommand extends AcuteLootCommand<Player> {

    public RenameCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    @Override
    protected void doHandle(Player sender, String[] args) {
        ItemStack item = sender.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR) {
            ItemMeta meta = item.getItemMeta();
            if (args.length > 1) {
                String name = "";
                for (int i = 1; i < args.length - 1; i++) {
                    name = name + args[i] + " ";
                }
                name = name + args[args.length - 1];
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            } else {
                // Setting to null will force the default material name
                meta.setDisplayName(null);
            }
            item.setItemMeta(meta);

        } else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "You must be holding something");
        }

    }
}
