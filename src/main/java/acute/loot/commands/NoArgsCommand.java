package acute.loot.commands;

import acute.loot.AcuteLoot;
import acute.loot.LootItem;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/** Command with no arguments class.
 *
 */
public class NoArgsCommand extends AcuteLootCommand<CommandSender> {

    public NoArgsCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    @Override
    protected void doHandle(CommandSender sender, String[] args) {
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "AcuteLoot version: " + ChatColor.YELLOW + plugin().getDescription()
                                                                                                      .getVersion());
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "LootCode version: " + LootItem.currentLootcodeVersion());
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Use " + ChatColor.AQUA + "/al help" + ChatColor.GRAY + " to learn more.");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Join the AcuteLoot Discord at: https://discord.gg/BXhUUQEymg");
    }
}
