package base.commands;

import base.util.Mock;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class MultiCommandTest {

    @Test
    @DisplayName("MultiCommand register correct")
    public void multiCommandRegisterCorrect() {
        final MultiCommand multiCommand = new MultiCommand();

        final CommandHandler<Player> playerCommandHandler = (sender, args) -> {};
        final CommandHandler<ConsoleCommandSender> consoleCommandHandler = (sender, args) -> {};
        final CommandHandler<CommandSender> genericCommandHandler = (sender, args) -> {};

        for (String s : Arrays.asList("", null, "   ")) {
            final Class<? extends Throwable> expectedException = s == null ? NullPointerException.class : IllegalArgumentException.class;
            assertThrows(expectedException, () -> multiCommand.registerPlayerSubcommand(s, playerCommandHandler));
            assertThrows(expectedException, () -> multiCommand.registerConsoleSubcommand(s, consoleCommandHandler));
            assertThrows(expectedException, () -> multiCommand.registerGenericSubcommand(s, genericCommandHandler));
        }

        assertThrows(NullPointerException.class, () -> multiCommand.registerPlayerSubcommand("test", null));
        assertThrows(NullPointerException.class, () -> multiCommand.registerConsoleSubcommand("foo", null));
        assertThrows(NullPointerException.class, () -> multiCommand.registerGenericSubcommand("bar", null));

        multiCommand.registerPlayerSubcommand("player", playerCommandHandler);
        multiCommand.registerConsoleSubcommand("console", consoleCommandHandler);
        multiCommand.registerGenericSubcommand("generic", genericCommandHandler);

        // Trying to register an already registered command should ALWAYS throw an exception,
        // even if it's the same handler as is already registered
        assertThrows(IllegalStateException.class, () -> multiCommand.registerPlayerSubcommand("player", playerCommandHandler));
        assertThrows(IllegalStateException.class, () -> multiCommand.registerConsoleSubcommand("console", consoleCommandHandler));
        assertThrows(IllegalStateException.class, () -> multiCommand.registerGenericSubcommand("generic", genericCommandHandler));

        // A subcommand can either have a player handler, a console handler,
        // a player handler and a console handler, or a generic handler.
        // All other combinations are invalid.
        assertThrows(IllegalStateException.class, () -> multiCommand.registerGenericSubcommand("player", genericCommandHandler));
        assertDoesNotThrow(() -> multiCommand.registerConsoleSubcommand("player", consoleCommandHandler));
        assertThrows(IllegalStateException.class, () -> multiCommand.registerGenericSubcommand("console", genericCommandHandler));
        assertDoesNotThrow(() -> multiCommand.registerPlayerSubcommand("console", playerCommandHandler));
        assertThrows(IllegalStateException.class, () -> multiCommand.registerPlayerSubcommand("generic", playerCommandHandler));
        assertThrows(IllegalStateException.class, () -> multiCommand.registerConsoleSubcommand("generic", consoleCommandHandler));
    }

    @Test
    @DisplayName("MultiCommand dispatch correct")
    public void multiCommandDispatchCorrect() {
        final MultiCommand multiCommand = new MultiCommand();

        final RecordingCommandHandler<Player> playerCommandHandler = new RecordingCommandHandler<>();
        final RecordingCommandHandler<ConsoleCommandSender> consoleCommandHandler = new RecordingCommandHandler<>();
        final RecordingCommandHandler<CommandSender> genericCommandHandler = new RecordingCommandHandler<>();

        final Player player = Mock.mockPlayer();
        final ConsoleCommandSender console = Mock.mockConsoleCommandSender();

        multiCommand.registerPlayerSubcommand("player", playerCommandHandler);
        multiCommand.registerConsoleSubcommand("console", consoleCommandHandler);
        multiCommand.registerPlayerSubcommand("player-and-console", playerCommandHandler);
        multiCommand.registerConsoleSubcommand("player-and-console", consoleCommandHandler);
        multiCommand.registerGenericSubcommand("generic", genericCommandHandler);

        assertThat(mockCommandSend(multiCommand, player), is(false));
        assertThat(mockCommandSend(multiCommand, console), is(false));

        assertThat(mockCommandSend(multiCommand, player, "player", "bar", "hello, world!"), is(true));
        assertThat(playerCommandHandler.hits, is(1));
        assertThat(consoleCommandHandler.hits, is(0));
        assertThat(genericCommandHandler.hits, is(0));

        assertThat(playerCommandHandler.lastArgs, contains("bar", "hello, world!"));

        assertThat(mockCommandSend(multiCommand, console, "console"), is(true));
        assertThat(playerCommandHandler.hits, is(1));
        assertThat(consoleCommandHandler.hits, is(1));
        assertThat(genericCommandHandler.hits, is(0));

        assertThat(consoleCommandHandler.lastArgs, emptyIterable());

        assertThat(mockCommandSend(multiCommand, player, "generic"), is(true));
        assertThat(playerCommandHandler.hits, is(1));
        assertThat(consoleCommandHandler.hits, is(1));
        assertThat(genericCommandHandler.hits, is(1));

        assertThat(mockCommandSend(multiCommand, console, "generic", "test"), is(true));
        assertThat(playerCommandHandler.hits, is(1));
        assertThat(consoleCommandHandler.hits, is(1));
        assertThat(genericCommandHandler.hits, is(2));

        assertThat(genericCommandHandler.lastArgs, contains("test"));

        assertThat(mockCommandSend(multiCommand, console, "player-and-console", "test", "testing"), is(true));
        assertThat(playerCommandHandler.hits, is(1));
        assertThat(consoleCommandHandler.hits, is(2));
        assertThat(genericCommandHandler.hits, is(2));

        assertThat(consoleCommandHandler.lastArgs, contains("test", "testing"));

        assertThat(mockCommandSend(multiCommand, player, "player-and-console", "test", "testing"), is(true));
        assertThat(playerCommandHandler.hits, is(2));
        assertThat(consoleCommandHandler.hits, is(2));
        assertThat(genericCommandHandler.hits, is(2));

        assertThat(playerCommandHandler.lastArgs, contains("test", "testing"));

        assertThat(mockCommandSend(multiCommand, null, "generic"), is(true));
        assertThat(playerCommandHandler.hits, is(2));
        assertThat(consoleCommandHandler.hits, is(2));
        assertThat(genericCommandHandler.hits, is(3));

        assertThat(mockCommandSend(multiCommand, null, "fake_command"), is(false));
        assertThat(playerCommandHandler.hits, is(2));
        assertThat(consoleCommandHandler.hits, is(2));
        assertThat(genericCommandHandler.hits, is(3));

        multiCommand.setNoArgsCommand(genericCommandHandler);
        assertThat(mockCommandSend(multiCommand, player), is(true));
        assertThat(mockCommandSend(multiCommand, console), is(true));
        assertThat(playerCommandHandler.hits, is(2));
        assertThat(consoleCommandHandler.hits, is(2));
        assertThat(genericCommandHandler.hits, is(5));
    }

    private boolean mockCommandSend(final CommandExecutor executor, final CommandSender commandSender, final String... args) {
        return executor.onCommand(commandSender, null, null, args);
    }

    private static class RecordingCommandHandler<T extends CommandSender> implements CommandHandler<T> {

        private int hits = 0;
        private final List<String> lastArgs = new ArrayList<>();

        @Override
        public void handle(T sender, String[] args) {
            hits++;
            lastArgs.clear();
            lastArgs.addAll(Arrays.asList(args));
        }
    }

}
