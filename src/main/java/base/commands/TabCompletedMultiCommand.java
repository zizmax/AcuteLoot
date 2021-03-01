package base.commands;

import base.util.Checks;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.*;

/**
 * A MultiCommand that also implements TabCompleter. By default this object will
 * provide tab-completions for each subcommand valid for the sender requesting the
 * completion. These completions will be returned if the arguments list is either
 * empty or has a single entry (in which case the partial matches will be returned
 * from the list of valid subcommands).
 *
 * If the arguments list has two or more entries, this object will return null unless
 * a delegate TabCompleter has been registered for the given subcommand. Note that
 * delegates CANNOT currently vary based on the type of the sender requesting the
 * completion.
 */
public class TabCompletedMultiCommand extends MultiCommand implements TabCompleter {

    // Constant completion, used when a subcompletion has not been registered
    // for the subcommand.
    private static final TabCompleter NULL_COMPLETION = (commandSender, command, s, strings) -> null;

    private final Map<String, TabCompleter> subcompletions = new HashMap<>();

    /**
     * Register a subcompletion for the given subcommand.
     * @param subcommand the subcommand, must be non-empty and not already registered
     * @param completion the completion, must be non-null
     */
    public void registerSubcompletion(final String subcommand, final TabCompleter completion) {
        Objects.requireNonNull(completion);
        Checks.requireDoesNotHaveKey(subcommand, subcompletions);
        subcompletions.put(subcommand, completion);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        final List<String> subcommandsForSender = getSubcommandsForSender(sender);
        if (args.length == 0) {
            return subcommandsForSender;
        } else if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], subcommandsForSender, (List<String>) new ArrayList<String>());
        } else {
            return subcompletions.getOrDefault(args[0], NULL_COMPLETION).onTabComplete(sender, command, label, args);
        }
    }
}
