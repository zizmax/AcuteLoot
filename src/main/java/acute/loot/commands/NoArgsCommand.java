package acute.loot.commands;

import acute.loot.AcuteLoot;
import acute.loot.LootItem;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
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

        final ClickEvent clickEvent = new ClickEvent( ClickEvent.Action.OPEN_URL, "https://discord.gg/BXhUUQEymg" );
        final BaseComponent[] message = new ComponentBuilder()
                .append(AcuteLoot.CHAT_PREFIX)
                .append("Click [me] to join the AcuteLoot Discord")
                .color(net.md_5.bungee.api.ChatColor.GRAY)
                .event(clickEvent)
                .create();
        sender.spigot().sendMessage(message);
    }
}
