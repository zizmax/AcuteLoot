package acute.loot.commands;

import acute.loot.AcuteLoot;
import base.commands.CommandHandler;
import org.bukkit.command.CommandSender;

import java.util.Objects;

/**
 * CommandHandler with an AcuteLoot instance and permission.
 *
 * @param <T> the type of CommandHandler
 */
public abstract class AcuteLootCommand<T extends CommandSender> implements CommandHandler<T> {

    private final String permission;
    private final AcuteLoot plugin;

    public AcuteLootCommand(String permission, AcuteLoot plugin) {
        this.permission = Objects.requireNonNull(permission);
        this.plugin = Objects.requireNonNull(plugin);
    }

    @Override
    public void handle(T sender, String[] args) {
        if (permissibleFor(sender)) {
            doHandle(sender, args);
        } else {
            sender.sendMessage(AcuteLoot.PERM_DENIED_MSG);
        }
    }

    @Override
    public boolean permissibleFor(T sender) {
        return plugin.hasPermission(sender, permission);
    }

    protected abstract void doHandle(T sender, String[] args);

    protected AcuteLoot plugin() {
        return plugin;
    }

}
