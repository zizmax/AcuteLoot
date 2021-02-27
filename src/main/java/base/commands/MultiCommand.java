package base.commands;

import base.util.Checks;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MultiCommand implements CommandExecutor {

    private final Map<String, CommandHandler<Player>> playerSubcommands = new HashMap<>();
    private final Map<String, CommandHandler<ConsoleCommandSender>> consoleSubcommands = new HashMap<>();
    private final Map<String, CommandHandler<CommandSender>> genericSubcommands = new HashMap<>();

    private CommandHandler<CommandSender> noArgsCommand = null;

    private String cannotBeUsedByPlayer = "This command cannot be used by a player.";
    private String cannotBeUsedByConsole = "This command cannot be used by the console.";
    private String unknownCommand = "Unknown command.";

    public void registerPlayerSubcommand(final String subcommand, final CommandHandler<Player> commandHandler) {
        precheck(subcommand, commandHandler, playerSubcommands);
        playerSubcommands.put(subcommand, commandHandler);
    }

    public void registerConsoleSubcommand(final String subcommand, final CommandHandler<ConsoleCommandSender> commandHandler) {
        precheck(subcommand, commandHandler, consoleSubcommands);
        consoleSubcommands.put(subcommand, commandHandler);
    }

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

    public void setNoArgsCommand(final CommandHandler<CommandSender> noArgsCommand) {
        this.noArgsCommand = noArgsCommand;
    }

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

    public void setCannotBeUsedByPlayer(String cannotBeUsedByPlayer) {
        this.cannotBeUsedByPlayer = Objects.requireNonNull(cannotBeUsedByPlayer);
    }

    public void setCannotBeUsedByConsole(String cannotBeUsedByConsole) {
        this.cannotBeUsedByConsole = Objects.requireNonNull(cannotBeUsedByConsole);
    }

    public void setUnknownCommand(String unknownCommand) {
        this.unknownCommand = Objects.requireNonNull(unknownCommand);
    }

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
