package acute.loot;


import acute.loot.commands.*;
import base.commands.MultiCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Commands implements CommandExecutor, TabCompleter {
    private static final List<String> DEFAULT_COMPLETIONS = Arrays.asList("help", "reload", "add", "remove", "stats", "new", "rename", "name", "chest", "salvage", "give");
    //public static final TabCompleter TAB_COMPLETER = (s, c, l, args) -> (args.length == 1) ? StringUtil.copyPartialMatches(args[0], DEFAULT_COMPLETIONS, new ArrayList<>()) : null;

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> possibleArguments = new ArrayList<>();
        if(args.length == 1){
            StringUtil.copyPartialMatches(args[0], DEFAULT_COMPLETIONS, possibleArguments);
            return possibleArguments;
        }
        if(args.length >= 2 && args.length <= 4){
            if(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("new")){
                if (args.length == 2){
                    StringUtil.copyPartialMatches(args[1], plugin.rarityNames.keySet(), possibleArguments);
                    return possibleArguments;
                }
                if (args.length == 3) {
                    possibleArguments.clear();
                    StringUtil.copyPartialMatches(args[2], plugin.effectNames.keySet(), possibleArguments);
                    return possibleArguments;
                }
            }
            else if(args[0].equalsIgnoreCase("give")) {
                // Same logic as above for 'add' and 'new' but shifted by one to account for player name
                if (args.length == 2){
                    return null;
                }
                if (args.length == 3) {
                    StringUtil.copyPartialMatches(args[2], plugin.rarityNames.keySet(), possibleArguments);
                }
                if (args.length == 4) {
                    possibleArguments.clear();
                    StringUtil.copyPartialMatches(args[3], plugin.effectNames.keySet(), possibleArguments);
                    return possibleArguments;
                }
                return possibleArguments;
            }
            else if(args.length == 2 && args[0].equalsIgnoreCase("name")){
                StringUtil.copyPartialMatches(args[1], plugin.nameGeneratorNames.keySet(), possibleArguments);
                return possibleArguments;
            }
        }
        return null;
    }

    private final AcuteLoot plugin;
    private final MultiCommand multiCommand = new MultiCommand();
    public Commands(AcuteLoot plugin) {
        this.plugin = plugin;
        multiCommand.setCannotBeUsedByConsole(AcuteLoot.CHAT_PREFIX + "You have to be a player!");
        multiCommand.setCannotBeUsedByPlayer(AcuteLoot.CHAT_PREFIX + "You have to be a console!");
        multiCommand.setUnknownCommand(AcuteLoot.CHAT_PREFIX + "Subcommand not found!");
        multiCommand.setNoArgsCommand(new NoArgsCommand("acuteloot", plugin));

        multiCommand.registerPlayerSubcommand("add", new AddCommand("acuteloot.add", plugin));

        multiCommand.registerPlayerSubcommand("chest", new ChestCommand("acuteloot.chest", plugin));

        multiCommand.registerGenericSubcommand("give", new GiveCommand("acuteloot.give", plugin));

        multiCommand.registerGenericSubcommand("help", new HelpCommand("acuteloot.help", plugin));

        multiCommand.registerPlayerSubcommand("name", new NameCommand("acuteloot.name", plugin));

        multiCommand.registerPlayerSubcommand("new", new NewLootCommand("acuteloot.new", plugin));

        multiCommand.registerPlayerSubcommand("reload", new ReloadCommand.PlayerReloadCommand("acuteloot.reload", plugin));
        multiCommand.registerConsoleSubcommand("reload", new ReloadCommand.ConsoleReloadCommand("acuteloot.reload", plugin));

        multiCommand.registerPlayerSubcommand("remove", new RemoveCommand("acuteloot.remove", plugin));

        multiCommand.registerPlayerSubcommand("rename", new RenameCommand("acuteloot.rename", plugin));

        multiCommand.registerGenericSubcommand("salvage", new SalvageCommand(plugin));

        multiCommand.registerPlayerSubcommand("stats", new StatsCommand.PlayerStatsCommand("acuteloot.stats", plugin));
        multiCommand.registerConsoleSubcommand("stats", new StatsCommand.ConsoleStatsCommand("acuteloot.stats", plugin));
    }

    public static void sendIncompatibleEffectsWarning(CommandSender sender, LootItem lootItem, ItemStack item){
        if(lootItem == null) return;
        for(LootSpecialEffect effect : lootItem.getEffects()){
            if(!effect.getValidMaterials().contains(LootMaterial.lootMaterialForMaterial(item.getType()))){
                sender.sendMessage(ChatColor.GOLD + "[" + ChatColor.RED + "WARNING" + ChatColor.GOLD + "] " + ChatColor.GRAY + effect.getName() + " not strictly compatible with this item!");
                sender.sendMessage(ChatColor.GOLD + "[" + ChatColor.RED + "WARNING" + ChatColor.GOLD + "] " + ChatColor.GRAY + "Effect may not work as expected/won't do anything");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return multiCommand.onCommand(sender, command, label, args);
    }

}
