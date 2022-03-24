package acute.loot.commands;

import acute.loot.AcuteLoot;
import acute.loot.LootItem;
import acute.loot.Util;
import acute.loot.namegen.PermutationCounts;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
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

        final ClickEvent clickEvent = new ClickEvent( ClickEvent.Action.OPEN_URL, "https://git.io/JtgCf" );
        final BaseComponent[] link = new ComponentBuilder()
                .append("https://git.io/JtgCf")
                .color(net.md_5.bungee.api.ChatColor.UNDERLINE)
                .event(clickEvent)
                .create();
        final BaseComponent[] stats = new ComponentBuilder()
                .append("AcuteLoot will work correctly, but will use the defaults for missing options.\n")
                .append("To use the new options:\n")
                .append("   1) Either paste in the new options or delete the current config (a backup has been made for you called config.bak)\n")
                .append("   2) Restart the server or use /al reload.\n")
                .append("   3) Enjoy the new features! :)")
                .create();

        final HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(stats));
        final BaseComponent[] message = new ComponentBuilder()
                .append(AcuteLoot.CHAT_PREFIX)
                .append("CONFIG OUT OF DATE: ")
                .color(net.md_5.bungee.api.ChatColor.RED)
                .append(String.format("installed v%d < latest v%d\n", 10, 20))
                .color(net.md_5.bungee.api.ChatColor.GRAY)
                .append("  - Click for latest config: ")
                .append(link)
                .append("\n  - Hover for instructions")
                .event(hoverEvent)
                .color(net.md_5.bungee.api.ChatColor.GRAY)
                .create();
        sender.spigot().sendMessage(message);
    }
}
