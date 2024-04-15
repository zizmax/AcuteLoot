package acute.loot.commands;

import acute.loot.AcuteLoot;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

/**
 * Help command class.
 */
public class HelpCommand extends AcuteLootCommand<CommandSender> {

    private final List<HelpEntry> helpEntries = Arrays.asList(
            new HelpEntry("reload", "acuteloot.reload", "/al reload" + ChatColor.GRAY + " Reload AL config and names"),
            new HelpEntry("add", "acuteloot.add", "/al add <rarity> [effect]" + ChatColor.GRAY + " Add AcuteLoot to item"),
            new HelpEntry("remove", "acuteloot.remove", "/al remove" + ChatColor.GRAY + " Remove AcuteLoot from an item"),
            new HelpEntry("new", "acuteloot.new", "/al new" + ChatColor.GRAY + " Create new random AcuteLoot"),
            new HelpEntry("rename", "acuteloot.rename", "/al rename [name]" + ChatColor.GRAY + " Supports '&' codes!"),
            new HelpEntry("reroll", "acuteloot.reroll", "/al reroll" + ChatColor.GRAY + " Reroll your AcuteLoot for a price"),
            new HelpEntry("name", "acuteloot.name", "/al name [generator]" + ChatColor.GRAY + " Name item using generator"),
            new HelpEntry("stats", "acuteloot.stats", "/al stats" + ChatColor.GRAY + " Stats about an item or general stats"),
            new HelpEntry("chest", "acuteloot.chest", "/al chest [time] [area] [force?]" +
                ChatColor.GRAY + " Set AL chests"),
            new HelpEntry("rmchest", "acuteloot.rmchest", "/al rmchest [area]" + ChatColor.GRAY + " Unset AL chests"),
            new HelpEntry("salvage", "acuteloot.salvage", "/al salvage [player]" + ChatColor.GRAY + " Open the salvaging GUI"),
            new HelpEntry("share", "acuteloot.share", "/al share" + ChatColor.GRAY + " Share AcuteLoot with the server"),
            new HelpEntry("repair", "acuteloot.repair", "/al repair" + ChatColor.GRAY + " Repair broken AcuteLoot"),
            new HelpEntry("append", "acuteloot.append", "/al append [file] [name]" + ChatColor.GRAY + " Append names to files")
    );

    public HelpCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    @Override
    protected void doHandle(CommandSender sender, String[] args) {
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.YELLOW + "==========| " + ChatColor.GRAY + "AcuteLoot Help" +
                ChatColor.YELLOW + " |==========");
        helpEntries.stream()
                   .filter(h -> sender.hasPermission(h.permission))
                   .forEachOrdered(h -> sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + h.usage));
    }

    private static final class HelpEntry {
        final String command;
        final String permission;
        final String usage;

        public HelpEntry(String command, String permission, String usage) {
            this.command = command;
            this.permission = permission;
            this.usage = usage;
        }
    }
}
