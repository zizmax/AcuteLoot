package base.commands;

import base.util.Checks;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * A MultiCommand is a CommandExecutor that will delegate to a CommandHandler
 * based on the "subcommand", that is, the first command argument. Further, the
 * subcommand delegate can be selected based on the type of the command sender.
 * This allows, for example, subcommands that can only be run by the player or the console,
 * or ones that have different handlers for the player and the console.
 *
 * A subcommand can either be registered with just a player handler, just a console
 * handler, a player handler and a console handler, or a "generic" handler. All other
 * combinations are invalid and will throw an IllegalStateException if created. Please
 * note that generic handlers will be invoked for ANY CommandSender, not just ones implementing
 * Player or ConsoleCommandSender.
 *
 * Additionally, a generic CommandHandler may be registered in the event that the command
 * is executed without a subcommand (that is, with no arguments).
 *
 * Please note that the arguments list is passed to the delegate CommandHandler's as is,
 * so args[0] in the handler will always be the subcommand, with the subcommand's arguments
 * starting at args[1] if present.
 */
public class MultiCommand implements CommandExecutor {

    // Registered subcommands and handlers
    private final Map<String, CommandHandler<Player>> playerSubcommands = new HashMap<>();
    private final Map<String, CommandHandler<ConsoleCommandSender>> consoleSubcommands = new HashMap<>();
    private final Map<String, CommandHandler<CommandSender>> genericSubcommands = new HashMap<>();

    // Will be used if no subcommand is present (args.length = 0)
    private CommandHandler<CommandSender> noArgsCommand = null;

    // Messages for bad commands
    private String cannotBeUsedByPlayer = "This command cannot be used by a player.";
    private String cannotBeUsedByConsole = "This command cannot be used by the console.";
    private String unknownCommand = "Unknown command.";

    /**
     * Register a subcommand with a player CommandHandler. The subcommand must not already be
     * registered as a player subcommand or as a generic subcommand.
     * @param subcommand the subcommand, must be non-empty
     * @param commandHandler the handler for the subcommand, must be non-null
     */
    public void registerPlayerSubcommand(final String subcommand, final CommandHandler<Player> commandHandler) {
        precheck(subcommand, commandHandler, playerSubcommands);
        playerSubcommands.put(subcommand, commandHandler);
    }

    /**
     * Register a subcommand with a console CommandHandler. The subcommand must not already be
     * registered as a console subcommand or as a generic subcommand.
     * @param subcommand the subcommand, must be non-empty
     * @param commandHandler the handler for the subcommand, must be non-null
     */
    public void registerConsoleSubcommand(final String subcommand, final CommandHandler<ConsoleCommandSender> commandHandler) {
        precheck(subcommand, commandHandler, consoleSubcommands);
        consoleSubcommands.put(subcommand, commandHandler);
    }

    /**
     * Register a subcommand with a console CommandHandler. The subcommand must not already be
     * registered as any other kind of subcommand.
     * @param subcommand the subcommand, must be non-empty
     * @param commandHandler the handler for the subcommand, must be non-null
     */
    public void registerGenericSubcommand(final String subcommand, final CommandHandler<CommandSender> commandHandler) {
        precheck(subcommand, commandHandler, genericSubcommands);
        try {
            Checks.requireDoesNotHaveKey(subcommand, playerSubcommands, "Subcommand cannot already be registered as player");
            Checks.requireDoesNotHaveKey(subcommand, consoleSubcommands, "Subcommand cannot already be registered as console");
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(e);
        }
        genericSubcommands.put(subcommand, commandHandler);
    }

    /**
     * Set the handler for when no subcommand is specified. This may be null,
     * in which case if no subcommand is specified onCommand() will do nothing
     * and return false.
     * @param noArgsCommand the handler for when no subcommand is specified
     */
    public void setNoArgsCommand(final CommandHandler<CommandSender> noArgsCommand) {
        this.noArgsCommand = noArgsCommand;
    }

    // Check the subcommand and command handler are valid and not
    // already registered as generic or in the primary map.
    private void precheck(final String subcommand,
                          final CommandHandler<?> commandHandler,
                          final Map<String, ?> primaryMap) {
        Checks.requireNonEmpty(subcommand, "Subcommand cannot be empty");
        Objects.requireNonNull(commandHandler, "Command handler cannot be null");
        try {
            Checks.requireDoesNotHaveKey(subcommand, primaryMap, "Subcommand cannot already be registered");
            Checks.requireDoesNotHaveKey(subcommand, genericSubcommands, "Subcommand cannot already be registered as generic");
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (noArgsCommand != null) {
                noArgsCommand.handle(commandSender, args);
                return true;
            } else {
                return false;
            }
        }

        final String subcommand = args[0].toLowerCase();
        try {
            final Dispatcher visitor = new Dispatcher(playerSubcommands.get(subcommand),
                                                consoleSubcommands.get(subcommand),
                                                genericSubcommands.get(subcommand),
                                                args);
            if (commandSender instanceof Player) {
                visitor.dispatch((Player) commandSender);
            } else if (commandSender instanceof ConsoleCommandSender) {
                visitor.dispatch((ConsoleCommandSender) commandSender);
            } else {
                visitor.dispatch(commandSender);
            }

            return true;
        } catch (IllegalArgumentException e) {
            if (commandSender != null) {
                commandSender.sendMessage(unknownCommand);
            }
            return false;
        }
    }

    /**
     * Set the message for when a console-only subcommand is executed by a player.
     * @param cannotBeUsedByPlayer the messages, must be non-null
     */
    public void setCannotBeUsedByPlayer(String cannotBeUsedByPlayer) {
        this.cannotBeUsedByPlayer = Objects.requireNonNull(cannotBeUsedByPlayer);
    }

    /**
     * Set the message for when a player-only subcommand is executed by a console..
     * @param cannotBeUsedByConsole the messages, must be non-null
     */
    public void setCannotBeUsedByConsole(String cannotBeUsedByConsole) {
        this.cannotBeUsedByConsole = Objects.requireNonNull(cannotBeUsedByConsole);
    }

    /**
     * Set the message for when an unknown subcommand is executed.
     * @param unknownCommand the messages, must be non-null
     */
    public void setUnknownCommand(String unknownCommand) {
        this.unknownCommand = Objects.requireNonNull(unknownCommand);
    }

    /**
     * Return a list of all registered subcommands valid for the give sender.
     * @param sender the sender for whom subcommands will be returned
     * @return a list of all registered subcommands valid for the given sender
     */
    public List<String> getSubcommandsForSender(final CommandSender sender) {
        Objects.requireNonNull(sender);
        final List<String> subcommands = new ArrayList<>(genericSubcommands.keySet());
        if (sender instanceof Player) {
            subcommands.addAll(playerSubcommands.keySet());
        } else if (sender instanceof ConsoleCommandSender) {
            subcommands.addAll(consoleSubcommands.keySet());
        }
        return subcommands;
    }

    // Helper class for dispatching to the correct delegate
    private class Dispatcher {
        private final CommandHandler<Player> playerHandler;
        private final CommandHandler<ConsoleCommandSender> consoleHandler;
        private final CommandHandler<CommandSender> genericHandler;

        private final String[] args;

        public Dispatcher(CommandHandler<Player> playerHandler, CommandHandler<ConsoleCommandSender> consoleHandler, CommandHandler<CommandSender> genericHandler, String[] args) {
            this.playerHandler = playerHandler;
            this.consoleHandler = consoleHandler;
            this.genericHandler = genericHandler;
            this.args = Objects.requireNonNull(args);
            if (playerHandler == null && consoleHandler == null && genericHandler == null) {
                throw new IllegalArgumentException("Subcommand has no handlers");
            }
        }

        public void dispatch(Player player) {
            if (playerHandler != null) {
                playerHandler.handle(player, args);
            } else if (!dispatch((CommandSender) player)) {
                player.sendMessage(cannotBeUsedByPlayer);
            }
        }

        public void dispatch(ConsoleCommandSender consoleCommandSender) {
            if (consoleHandler != null) {
                consoleHandler.handle(consoleCommandSender, args);
            } else if (!dispatch((CommandSender) consoleCommandSender)) {
                consoleCommandSender.sendMessage(cannotBeUsedByConsole);
            }
        }

        public boolean dispatch(CommandSender commandSender) {
            if (genericHandler != null) {
                genericHandler.handle(commandSender, args);
                return true;
            }
            return false;
        }
    }
}
