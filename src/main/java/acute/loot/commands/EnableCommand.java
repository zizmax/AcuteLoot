package acute.loot.commands;

import acute.loot.AcuteLoot;
import org.bukkit.command.CommandSender;

import static acute.loot.AcuteLoot.CHAT_PREFIX;

public class EnableCommand extends AcuteLootCommand<CommandSender> {

    public EnableCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    @Override
    protected void doHandle(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(CHAT_PREFIX + "You must specify a module to enable.");
            return;
        }

        final String module = args[1];

        if (!plugin().getModuleManager().hasModule(module)) {
            sender.sendMessage(CHAT_PREFIX + "Unknown module.");
            return;
        }

        if (plugin().getModuleManager().isEnabled(module)) {
            sender.sendMessage(CHAT_PREFIX + "Module is already enabled.");
            return;
        }

        plugin().getModuleManager().enable(module);
        sender.sendMessage(CHAT_PREFIX + "Enabled module " + module);
    }
}
