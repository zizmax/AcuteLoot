package acute.loot;


import acute.loot.namegen.PermutationCounts;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Commands implements CommandExecutor, TabCompleter {
    private static final List<String> DEFAULT_COMPLETIONS = Arrays.asList("help", "reload", "add", "remove", "stats", "new", "rename");
    //public static final TabCompleter TAB_COMPLETER = (s, c, l, args) -> (args.length == 1) ? StringUtil.copyPartialMatches(args[0], DEFAULT_COMPLETIONS, new ArrayList<>()) : null;

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> possibleArguments = new ArrayList<>();
        if(args.length == 1){
            StringUtil.copyPartialMatches(args[0], DEFAULT_COMPLETIONS, possibleArguments);
            return possibleArguments;
        }
        if(args.length >= 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("new"))){
            StringUtil.copyPartialMatches(args[1], AcuteLoot.rarityNames.keySet(), possibleArguments);
            if(args.length == 3){
                possibleArguments.clear();
                StringUtil.copyPartialMatches(args[2], AcuteLoot.effectNames.keySet(), possibleArguments);
                return possibleArguments;
            }
            return possibleArguments;
        }
        return null;
    };

    private final AcuteLoot plugin;

    public Commands(AcuteLoot plugin) {
        this.plugin = plugin;
    }

    final String PERM_DENIED_MSG = AcuteLoot.CHAT_PREFIX + "You do not have permission to do this";

    private boolean hasPermission(CommandSender sender, String node) {
        return (!plugin.getConfig().getBoolean("use-permissions") && sender.isOp())
                || (plugin.getConfig().getBoolean("use-permissions") && sender.hasPermission(node));
    }

    private void printGeneralStats(CommandSender sender) {
        final long birthdayCount = PermutationCounts.birthdayProblem(PermutationCounts.totalPermutations(plugin.getConfig()
                                                                                                               .getBoolean("kana-namegen")), 0.5, 0.0001);
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "General stats:");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + String.format("Total number of possible names: ~%,d", PermutationCounts
                .totalPermutations(plugin.getConfig().getBoolean("kana-namegen"))));
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + String.format("Names for 50%% chance of duplicate: ~%,d", birthdayCount));
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Number of rarities: " + AcuteLoot.rarityChancePool.values().size());
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Number of effects: " + AcuteLoot.effectChancePool.values().size());
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Number of loot materials: " + Events.materials.size());
    }

    private void helpCommand(CommandSender sender) {
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al reload" + ChatColor.GRAY + " Reload AL config and names");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al add" + ChatColor.GRAY + " Feature coming soon!");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al remove" + ChatColor.GRAY + " Remove AcuteLoot from an item");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al new" + ChatColor.GRAY + " Create new random AcuteLoot");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al rename [name]" + ChatColor.GRAY + " Rename an item (Color codes supported!)");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al stats" + ChatColor.GRAY + " Stats about an item or general stats");
    }

    private void reloadCommand(CommandSender sender) {
        // Reload config
        plugin.reloadConfig();
        plugin.saveDefaultConfig();

        // Reload names and rarities
        plugin.reloadConfiguration();

        // Check for updates
        UpdateChecker.init(plugin, AcuteLoot.spigotID).requestUpdateCheck().whenComplete((result, exception) -> {
            if (result.requiresUpdate()) {
                sender.sendMessage(String.format(AcuteLoot.CHAT_PREFIX + ChatColor.RED + AcuteLoot.UPDATE_AVAILABLE, result
                        .getNewestVersion()));
                return;
            }

            UpdateChecker.UpdateReason reason = result.getReason();
            if (reason == UpdateChecker.UpdateReason.UP_TO_DATE) {
                sender.sendMessage(String.format(AcuteLoot.CHAT_PREFIX + AcuteLoot.UP_TO_DATE, result.getNewestVersion()));
            } else if (reason == UpdateChecker.UpdateReason.UNRELEASED_VERSION) {
                sender.sendMessage(String.format(AcuteLoot.CHAT_PREFIX + AcuteLoot.UNRELEASED_VERSION, result.getNewestVersion()));
            } else {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.RED + AcuteLoot.UPDATE_CHECK_FAILED + ChatColor.WHITE + reason);
            }
        });
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Config reloaded successfully");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Checking for updates...");

    }

    private void addCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (Events.getLootCode(plugin, item) == null) {
                if (AcuteLoot.rarityNames.containsKey(args[1])) {
                    final int rarity = AcuteLoot.rarityNames.get(args[1]);
                    final List<Integer> effects = new ArrayList<>();
                    if (args.length > 2) {
                        if (AcuteLoot.effectNames.containsKey(args[2])){
                            effects.add(AcuteLoot.effectNames.get(args[2]));
                        }
                        else {
                            player.sendMessage(AcuteLoot.CHAT_PREFIX + "Effect " + args[2] + " doesn't exist");
                            return; // Do not apply the rarity if the effect is invalid
                        }
                    }

                    new Events(plugin).createLootItem(item, rarity, effects);
                }
                else {
                    player.sendMessage(AcuteLoot.CHAT_PREFIX + "Rarity " + args[1] + " doesn't exist");
                }
            }
            else{
                player.sendMessage(AcuteLoot.CHAT_PREFIX + "Item is already AcuteLoot");
            }
        } else {
            player.sendMessage(AcuteLoot.CHAT_PREFIX + "You must be holding something");
        }
    }

    private void removeCommand(CommandSender sender) {
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR) {
            if (Events.getLootCode(plugin, item) != null) {
                ItemMeta meta = item.getItemMeta();
                meta.setLore(new ArrayList<>());
                meta.setDisplayName(null);
                NamespacedKey key = new NamespacedKey(plugin, "lootCodeKey");
                meta.getPersistentDataContainer().remove(key);
                item.setItemMeta(meta);
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "AcuteLoot removed");
            } else {
                player.sendMessage(AcuteLoot.CHAT_PREFIX + "Item is not AcuteLoot");
            }
        } else {
            player.sendMessage(AcuteLoot.CHAT_PREFIX + "You must be holding something");
        }
    }

    private void statsCommand(CommandSender sender) {
        Player player = (Player) sender;
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
            String lootCode = Events.getLootCode(plugin, player.getInventory().getItemInMainHand());
            if (lootCode != null) {
                player.sendMessage(AcuteLoot.CHAT_PREFIX + "Loot code: " + ChatColor.AQUA + lootCode);
            } else {
                player.sendMessage(AcuteLoot.CHAT_PREFIX + "Item is not AcuteLoot");
            }
        } else {
            printGeneralStats(player);
        }

    }

    private void newCommand(CommandSender sender) {
        Player player = (Player) sender;

        ItemStack item = new Events(plugin).createLootItem();
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
            player.sendMessage(AcuteLoot.CHAT_PREFIX + "Created " + ChatColor.GOLD + item.getType());
            player.sendMessage(AcuteLoot.CHAT_PREFIX + "Name: " + item.getItemMeta().getDisplayName());
            player.sendMessage(AcuteLoot.CHAT_PREFIX + "Rarity: " + item.getItemMeta().getLore().get(0));
        } else {
            player.sendMessage(AcuteLoot.CHAT_PREFIX + "Inventory cannot be full");
        }

    }

    private void renameCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR) {
            ItemMeta meta = item.getItemMeta();
            if (args.length > 1) {
                String name = "";
                for (int i = 1; i < args.length - 1; i++) {
                    name = name + args[i] + " ";
                }
                name = name + args[args.length - 1];
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            } else {
                // Setting to null will force the default material name
                meta.setDisplayName(null);
            }
            item.setItemMeta(meta);

        } else {
            player.sendMessage(AcuteLoot.CHAT_PREFIX + "You must be holding something");
        }

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("acuteloot") || command.getName().equalsIgnoreCase("al")) {
            if (args.length > 0) {

                // Help
                if (args[0].equalsIgnoreCase("help")) {
                    if (hasPermission(sender, "acuteloot.help") || sender instanceof ConsoleCommandSender) {
                        helpCommand(sender);
                    } else {
                        sender.sendMessage(PERM_DENIED_MSG);
                    }
                    return true;
                }

                // Reload
                else if (args[0].equalsIgnoreCase("reload")) {
                    if (hasPermission(sender, "acuteloot.reload") || sender instanceof ConsoleCommandSender) {
                        reloadCommand(sender);
                    } else {
                        sender.sendMessage(PERM_DENIED_MSG);
                    }
                    return true;
                }

                // Add
                else if (args[0].equalsIgnoreCase("add")) {
                    if (sender instanceof Player) {
                        if (hasPermission(sender, "acuteloot.add")) {
                            if (args.length > 1) {
                                addCommand(sender, args);
                            } else {
                                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "/acuteloot add [rarity] <effect>");
                            }
                            return true;
                        } else {
                            sender.sendMessage(PERM_DENIED_MSG);
                            return true;
                        }
                    } else {
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "You have to be a player!");
                        return true;
                    }
                }

                // Remove
                else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("clear")) {
                    if (sender instanceof Player) {
                        if (hasPermission(sender, "acuteloot.remove")) {
                            removeCommand(sender);
                            return true;
                        } else {
                            sender.sendMessage(PERM_DENIED_MSG);
                        }
                    } else {
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "You have to be a player!");
                    }
                    return true;

                }

                // Rename
                else if (args[0].equalsIgnoreCase("rename")) {
                    if (sender instanceof Player) {
                        if (hasPermission(sender, "acuteloot.rename")) {
                            renameCommand(sender, args);
                        } else {
                            sender.sendMessage(PERM_DENIED_MSG);
                        }
                        return true;
                    } else {
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "You have to be a player!");
                        return true;
                    }

                }

                // Stats
                else if (args[0].equalsIgnoreCase("stats")) {
                    if (sender instanceof Player) {
                        if (hasPermission(sender, "acuteloot.stats") || sender instanceof ConsoleCommandSender) {
                            statsCommand(sender);
                        } else {
                            sender.sendMessage(PERM_DENIED_MSG);
                        }
                        return true;
                    } else {
                        printGeneralStats(sender);
                        return true;
                    }

                }

                // New
                else if (args[0].equalsIgnoreCase("new")) {
                    if (sender instanceof Player) {
                        if (hasPermission(sender, "acuteloot.new")) {
                            newCommand(sender);
                        } else {
                            sender.sendMessage(PERM_DENIED_MSG);
                        }
                        return true;
                    } else {
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "You have to be a player!");
                        return true;
                    }
                } else {
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Subcommand not found!");
                    return true;
                }
            } else {
                // Root permission node/command (/al)
                if (hasPermission(sender, "acuteloot") || sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + "AcuteLoot version: " + ChatColor.YELLOW + plugin.getDescription()
                                                                                                                .getVersion());
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Use " + ChatColor.AQUA + "/al help" + ChatColor.GRAY + " to learn more.");
                } else {
                    sender.sendMessage(PERM_DENIED_MSG);
                }
                return true;

            }
        }
        return false;
    }
}
