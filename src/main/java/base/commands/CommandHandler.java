package base.commands;

import org.bukkit.command.CommandSender;

public interface CommandHandler<T extends CommandSender> {

    void handle(T sender, String[] args);

}
