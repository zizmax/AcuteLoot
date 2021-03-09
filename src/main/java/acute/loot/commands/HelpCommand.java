package acute.loot.commands;

import acute.loot.AcuteLoot;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCommand extends AcuteLootCommand<CommandSender>  {

    public HelpCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    @Override
    protected void doHandle(CommandSender sender, String[] args) {
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.YELLOW + "==========| " + ChatColor.GRAY + "AcuteLoot Help" + ChatColor.YELLOW + " |==========");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al reload" + ChatColor.GRAY + " Reload AL config and names");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al add <rarity> [effect]" + ChatColor.GRAY + " Add AcuteLoot to item");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al remove" + ChatColor.GRAY + " Remove AcuteLoot from an item");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al new" + ChatColor.GRAY + " Create new random AcuteLoot");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al rename [name]" + ChatColor.GRAY + " Supports '&' codes!");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al name [generator]" + ChatColor.GRAY + " Name item using generator");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al stats" + ChatColor.GRAY + " Stats about an item or general stats");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al chest [minutes]" + ChatColor.GRAY + " Set AL chests");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al salvage [player]" + ChatColor.GRAY + " Open the salvaging GUI");
    }
}
