package acute.loot.commands;

import acute.loot.AcuteLoot;
import acute.loot.SalvagerGui;
import base.commands.CommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Salvage command class.
 */
public class SalvageCommand implements CommandHandler<CommandSender> {

    private final AcuteLoot plugin;

    public SalvageCommand(AcuteLoot plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    @Override
    public void handle(CommandSender sender, String[] args) {
        if (!plugin.getConfig().getBoolean("salvager.enabled")) {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Salvaging is not enabled");
            return;
        }

        if (args.length >= 2) {
            if (!plugin.hasPermission(sender, "acuteloot.salvage-force-open")) {
                sender.sendMessage(AcuteLoot.PERM_DENIED_MSG);
                return;
            }

            if (sender.getServer().getPlayerExact(args[1]) != null) {
                SalvagerGui inv = new SalvagerGui(plugin);
                inv.openInventory(sender.getServer().getPlayerExact(args[1]));
            } else {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Player is not online!");
            }
        } else if (sender instanceof Player) {
            if (!plugin.hasPermission(sender, "acuteloot.salvage")) {
                sender.sendMessage(AcuteLoot.PERM_DENIED_MSG);
                return;
            }
            Player player = (Player) sender;
            SalvagerGui inv = new SalvagerGui(plugin);
            inv.openInventory(player);
        } else {
            sender.sendMessage("Must specify player when running as console");
        }
    }

    @Override
    public boolean permissibleFor(CommandSender sender) {
        return sender.hasPermission("acuteloot.salvage"); // TODO is this right??
    }
}
