package acute.loot;


import acute.loot.commands.*;
import base.commands.TabCompletedMultiCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Commands implements CommandExecutor, TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return multiCommand.onTabComplete(sender, command, label, args);
    }

    private final AcuteLoot plugin;
    private final TabCompletedMultiCommand multiCommand = new TabCompletedMultiCommand();
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

        final TabCompleter addAndNewCompletion = (s, c, l, args) -> {
            switch (args.length) {
                case 2: return StringUtil.copyPartialMatches(args[1], plugin.rarityNames.keySet(), new ArrayList<>());
                case 3: return StringUtil.copyPartialMatches(args[2], plugin.effectNames.keySet(), new ArrayList<>());
                default: return null;
            }
        };

        final TabCompleter giveCompletion = (s, c, l, args) -> {
            // Same logic as above for 'add' and 'new' but shifted by one to account for player name
            // Note: this means args[0] is the player name, not "give"...
            return addAndNewCompletion.onTabComplete(s, c, l, Arrays.copyOfRange(args, 1, args.length));
        };

        final TabCompleter nameCompletion = (s, c, l, args) -> args.length == 2 ? StringUtil.copyPartialMatches(args[1], plugin.nameGeneratorNames.keySet(), new ArrayList<>()) : null;

        multiCommand.registerSubcompletion("add", addAndNewCompletion);
        multiCommand.registerSubcompletion("new", addAndNewCompletion);
        multiCommand.registerSubcompletion("give", giveCompletion);
        multiCommand.registerSubcompletion("name", nameCompletion);
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
