package base.commands;

import org.bukkit.command.CommandSender;

/**
 * A CommandHandler handles a command in a type-safe way.
 *
 * @param <T> the type of the command sender
 */
public interface CommandHandler<T extends CommandSender> {

    /**
     * Handle a command sent by the given sender with the given
     * arguments.
     *
     * @param sender the sender of the command
     * @param args   the arguments of the command
     */
    void handle(T sender, String[] args);

}
