package acute.loot;


import acute.loot.namegen.NameGenerator;
import acute.loot.namegen.PermutationCounts;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;

import java.util.*;

public class Commands implements CommandExecutor, TabCompleter {
    private static final List<String> DEFAULT_COMPLETIONS = Arrays.asList("help", "reload", "add", "remove", "stats", "new", "rename", "name", "chest", "salvage");
    //public static final TabCompleter TAB_COMPLETER = (s, c, l, args) -> (args.length == 1) ? StringUtil.copyPartialMatches(args[0], DEFAULT_COMPLETIONS, new ArrayList<>()) : null;

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> possibleArguments = new ArrayList<>();
        if(args.length == 1){
            StringUtil.copyPartialMatches(args[0], DEFAULT_COMPLETIONS, possibleArguments);
            return possibleArguments;
        }
        if(args.length >= 2 && args.length < 4){
            if(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("new")){
                StringUtil.copyPartialMatches(args[1], AcuteLoot.rarityNames.keySet(), possibleArguments);
                if (args.length == 3) {
                    possibleArguments.clear();
                    StringUtil.copyPartialMatches(args[2], AcuteLoot.effectNames.keySet(), possibleArguments);
                    return possibleArguments;
                }
                return possibleArguments;
            }
            if(args.length == 2 && args[0].equalsIgnoreCase("name")){
                StringUtil.copyPartialMatches(args[1], AcuteLoot.nameGeneratorNames.keySet(), possibleArguments);
                return possibleArguments;
            }
        }
        return null;
    }

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
        final long birthdayCount = PermutationCounts.birthdayProblem(PermutationCounts.totalPermutations(), 0.5, 0.0001);
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "General stats:");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + String.format("Total number of possible names: ~%,d", PermutationCounts.totalPermutations()));
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + String.format("Names for 50%% chance of duplicate: ~%,d", birthdayCount));
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Number of rarities: " + AcuteLoot.rarityChancePool.values().size());
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Number of effects: " + AcuteLoot.effectChancePool.values().size());
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Number of loot materials: " + Events.materials.size());
    }

    private void helpCommand(CommandSender sender) {
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.YELLOW + "==========| " + ChatColor.GRAY + "AcuteLoot Help" + ChatColor.YELLOW + " |==========");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al reload" + ChatColor.GRAY + " Reload AL config and names");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al add <rarity> [effect]" + ChatColor.GRAY + " Add AcuteLoot to item");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al remove" + ChatColor.GRAY + " Remove AcuteLoot from an item");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al new" + ChatColor.GRAY + " Create new random AcuteLoot");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al rename [name]" + ChatColor.GRAY + " Supports '&' codes!");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al name [generator]" + ChatColor.GRAY + " Name item using generator");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al stats" + ChatColor.GRAY + " Stats about an item or general stats");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al chest [minutes]" + ChatColor.GRAY + " Set AL chests");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "/al salvage [player]" + ChatColor.GRAY + " Open the salvaging GUI");
    }

    private void reloadCommand(CommandSender sender) {
        // Reload names/rarities, copy and save config.yml
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
                if(args.length == 1) {
                    final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(item.getType());
                    if(lootMaterial == LootMaterial.UNKNOWN) {
                        player.sendMessage(AcuteLoot.CHAT_PREFIX + item.getType() + " isn't valid AcuteLoot material");
                        return;
                    }
                    new Events(plugin).createLootItem(item, AcuteLoot.random.nextDouble());
                    player.sendMessage(AcuteLoot.CHAT_PREFIX + "AcuteLoot added with random rarity");
                }
                else {
                    if (AcuteLoot.rarityNames.containsKey(args[1])) {
                        final int rarity = AcuteLoot.rarityNames.get(args[1]);
                        if (args.length > 2) {
                            if (AcuteLoot.effectNames.containsKey(args[2])) {
                                final EffectId effectId = new EffectId(AcuteLoot.effectNames.get(args[2]));
                                final LootItem lootItem = new LootItem(rarity, Collections.singletonList(effectId));
                                new Events(plugin).createLootItem(item, lootItem);
                                player.sendMessage(AcuteLoot.CHAT_PREFIX + "AcuteLoot added with " + args[1] + " and " + args[2]);
                                sendIncompatibleEffectsWarning(player, lootItem, item);
                            } else {
                                player.sendMessage(AcuteLoot.CHAT_PREFIX + "Effect " + args[2] + " doesn't exist");
                            }
                        } else {
                            new Events(plugin).createLootItem(item, LootRarity.get(rarity));
                            player.sendMessage(AcuteLoot.CHAT_PREFIX + "AcuteLoot added with " + args[1]);
                        }

                    } else {
                        player.sendMessage(AcuteLoot.CHAT_PREFIX + "Rarity " + args[1] + " doesn't exist");
                    }
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

    private void newCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ItemStack item = Events.chooseLootMaterial();
        LootItem lootItem = null;
        if(args.length == 1) new Events(plugin).createLootItem(item, AcuteLoot.random.nextDouble());
        else{
            if (AcuteLoot.rarityNames.containsKey(args[1])) {
                final int rarityID = AcuteLoot.rarityNames.get(args[1]);
                if(args.length == 2){
                    new Events(plugin).createLootItem(item, LootRarity.get(rarityID));
                }
                final List<EffectId> effects = new ArrayList<>();
                if (args.length > 2) {
                    if (AcuteLoot.effectNames.containsKey(args[2])){
                        effects.add(new EffectId(AcuteLoot.effectNames.get(args[2])));
                        lootItem = new LootItem(rarityID, effects);
                        new Events(plugin).createLootItem(item, lootItem);
                    }
                    else {
                        player.sendMessage(AcuteLoot.CHAT_PREFIX + "Effect " + args[2] + " doesn't exist");
                        return; // Do not apply the rarity if the effect is invalid
                    }
                }

            }
            else {
                player.sendMessage(AcuteLoot.CHAT_PREFIX + "Rarity " + args[1] + " doesn't exist");
                return;
            }
        }
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
            player.sendMessage(AcuteLoot.CHAT_PREFIX + "Created " + ChatColor.GOLD + item.getType());
            player.sendMessage(AcuteLoot.CHAT_PREFIX + "Name: " + item.getItemMeta().getDisplayName());
            player.sendMessage(AcuteLoot.CHAT_PREFIX + "Rarity: " + item.getItemMeta().getLore().get(0));
            sendIncompatibleEffectsWarning(player, lootItem, item);
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

    private void nameCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR) {
            final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(item.getType());
            if(lootMaterial == LootMaterial.UNKNOWN) {
                player.sendMessage(AcuteLoot.CHAT_PREFIX + item.getType() + " isn't valid AcuteLoot material");
                return;
            }
            ItemMeta meta = item.getItemMeta();
            String name = null;
            LootItemGenerator generator = new LootItemGenerator(AcuteLoot.rarityChancePool, AcuteLoot.effectChancePool);
            if (args.length >= 2) {
                if (AcuteLoot.nameGeneratorNames.containsKey(args[1])) {
                    try {
                        name = AcuteLoot.nameGeneratorNames.get(args[1]).generate(lootMaterial, generator.generate(AcuteLoot.random.nextDouble(), lootMaterial).rarity());
                    } catch (NoSuchElementException e) {
                        player.sendMessage(AcuteLoot.CHAT_PREFIX + args[1] + " doesn't have any valid names for this item!");
                        return;
                    }
                    player.sendMessage(AcuteLoot.CHAT_PREFIX + "Name added with " + args[1]);
                }
                else {
                    player.sendMessage(AcuteLoot.CHAT_PREFIX + "Name generator " + args[1] + " doesn't exist");
                    return;
                }

            } else {
                // Choose random generator when no other arguments are present
                int attempts = 100;
                NameGenerator nameGenerator = null;
                do {
                    try {
                        nameGenerator = AcuteLoot.nameGenChancePool.draw();
                        name = nameGenerator.generate(lootMaterial, generator.generate(AcuteLoot.random.nextDouble(), lootMaterial).rarity());
                        player.sendMessage(AcuteLoot.CHAT_PREFIX + "Name added with random generator");
                    } catch (NoSuchElementException e) {
                        // Couldn't draw a name for some reason, try again
                        attempts--;
                    }
                } while (name == null && attempts > 0);
                if (attempts == 0) {
                    plugin.getLogger().severe("Could not generate a name in 100 attempts! Are name files empty or corrupted?");
                    plugin.getLogger().severe("Name Generator: " + nameGenerator.toString());
                }
            }
            player.sendMessage(name);
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("loot-name-color")) + name);
            item.setItemMeta(meta);


        } else {
            player.sendMessage(AcuteLoot.CHAT_PREFIX + "You must be holding something");
        }

    }

    private void chestCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        int numFoundChests = 0;
        NamespacedKey key = new NamespacedKey(plugin, "chestMetadataKey");
        // chestMetadataCode Version 1.0 = "1.0:currentTimeMillis():refillCooldown (minutes)"
        // i.e. "1.0:1606604412:90"
        String version = "1.0";
        String currentTime = String.valueOf(System.currentTimeMillis());
        String refillCooldown = "-1";
        for (BlockState tileEntity : player.getLocation().getChunk().getTileEntities()){
            if (tileEntity instanceof Chest){
                if (((Chest) tileEntity).getPersistentDataContainer().has(key, PersistentDataType.STRING)){
                    String chestMetadata = ((Chest) tileEntity).getPersistentDataContainer().get(key, PersistentDataType.STRING);
                }
                else {
                    if (args.length >= 2){
                        try {
                            Integer.parseInt(args[1]);
                            refillCooldown = args[1];
                        }
                        catch (NumberFormatException e){
                            player.sendMessage(AcuteLoot.CHAT_PREFIX + "Refill cooldown (minutes) must be an integer");
                            return;
                        }
                    }
                    String chestMetadataCode = String.format("%s:%s:%s", version, currentTime, refillCooldown);
                    if (AcuteLoot.debug) player.sendMessage("Code: " + chestMetadataCode);
                    ((Chest) tileEntity).getPersistentDataContainer().set(key, PersistentDataType.STRING, chestMetadataCode);
                    tileEntity.update();
                    BlockState state = tileEntity.getBlock().getState();

                    if (state instanceof Chest) {
                        Chest chest = (Chest) state;
                        Inventory inventory = chest.getInventory();
                        if (inventory instanceof DoubleChestInventory) {
                            //TODO: Check if it's a double chest
                            //TODO: Adjust numFoundChests accordingly
                            //DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                        }

                    }
                    tileEntity.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, tileEntity.getBlock().getLocation().add(.5, 1, .5), 100);
                    numFoundChests++;
                }
            }
        }
        //TODO: Play sound
        player.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.YELLOW + "============= " + ChatColor.GRAY + "Chest Creator"
                + ChatColor.YELLOW + " =============");
        if(numFoundChests == 0){
            player.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "0" + ChatColor.GRAY
                    + " non-AcuteLoot chests found in current chunk!");
        }
        else {
            player.sendMessage(AcuteLoot.CHAT_PREFIX + "Chests created: " + ChatColor.AQUA + numFoundChests);
            if (refillCooldown == "-1"){
                player.sendMessage(AcuteLoot.CHAT_PREFIX + "Refill cooldown: " + ChatColor.AQUA + "none"
                        + ChatColor.GRAY + "*");
                player.sendMessage(AcuteLoot.CHAT_PREFIX + "*Will only generate loot once, when first opened");
            }
            else{

                int seconds = (Integer.parseInt(refillCooldown) * 60) % 60;
                int minutes = Integer.parseInt(refillCooldown) % 60;
                int hours   = Integer.parseInt(refillCooldown) / 60;
                player.sendMessage(String.format(AcuteLoot.CHAT_PREFIX + "Refill cooldown: " + ChatColor.AQUA
                        + "%dh:%dm:%ds", hours, minutes, seconds));
            }
        }
    }

    private void salvageCommand(CommandSender sender, String[] args) {
        if (args.length >= 2){
            if (sender.getServer().getPlayerExact(args[1]) != null){
                SalvagerGUI inv = new SalvagerGUI(plugin);
                inv.openInventory(sender.getServer().getPlayerExact(args[1]));
            }
            else {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Player is not online!");
            }
        }
        else {
            Player player = (Player) sender;
            SalvagerGUI inv = new SalvagerGUI(plugin);
            inv.openInventory(player);
        }
    }

    public void sendIncompatibleEffectsWarning(Player player, LootItem lootItem, ItemStack item){
        if(lootItem == null) return;
        for(LootSpecialEffect effect : lootItem.getEffects()){
            if(!effect.getValidMaterials().contains(LootMaterial.lootMaterialForMaterial(item.getType()))){
                player.sendMessage(ChatColor.GOLD + "[" + ChatColor.RED + "WARNING" + ChatColor.GOLD + "] " + ChatColor.GRAY + effect.getName() + " not strictly compatible with this item!");
                player.sendMessage(ChatColor.GOLD + "[" + ChatColor.RED + "WARNING" + ChatColor.GOLD + "] " + ChatColor.GRAY + "Effect may not work as expected/won't do anything");
            }
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
                            addCommand(sender, args);
                        } else {
                            sender.sendMessage(PERM_DENIED_MSG);
                        }
                    } else {
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "You have to be a player!");
                    }
                    return true;
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
                    } else {
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "You have to be a player!");
                    }
                    return true;

                }

                // Stats
                else if (args[0].equalsIgnoreCase("stats")) {
                    if (sender instanceof Player) {
                        if (hasPermission(sender, "acuteloot.stats") || sender instanceof ConsoleCommandSender) {
                            statsCommand(sender);
                        } else {
                            sender.sendMessage(PERM_DENIED_MSG);
                        }
                    } else {
                        printGeneralStats(sender);
                    }
                    return true;

                }

                // Name
                else if (args[0].equalsIgnoreCase("name")) {
                    if (sender instanceof Player) {
                        if (hasPermission(sender, "acuteloot.name")) {
                            nameCommand(sender, args);
                            return true;
                        } else {
                            sender.sendMessage(PERM_DENIED_MSG);
                        }
                    } else {
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "You have to be a player!");
                    }
                    return true;

                }

                // New
                else if (args[0].equalsIgnoreCase("new")) {
                    if (sender instanceof Player) {
                        if (hasPermission(sender, "acuteloot.new")) {
                            newCommand(sender, args);
                        } else {
                            sender.sendMessage(PERM_DENIED_MSG);
                        }
                    } else {
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "You have to be a player!");
                    }
                    return true;
                }

                // Chest
                else if (args[0].equalsIgnoreCase("chest")) {
                    if (sender instanceof Player) {
                        if (hasPermission(sender, "acuteloot.chest")) {
                            chestCommand(sender, args);
                            return true;
                        } else {
                            sender.sendMessage(PERM_DENIED_MSG);
                        }
                    } else {
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "You have to be a player!");
                    }
                    return true;
                }

                // Salvage
                else if (args[0].equalsIgnoreCase("salvage")) {
                    if(plugin.getConfig().getBoolean("salvager.enabled")){
                        if(args.length >= 2) {
                            if (hasPermission(sender, "acuteloot.salvage-force-open")) {
                                salvageCommand(sender, args);
                                return true;
                            }
                            else {
                                sender.sendMessage(PERM_DENIED_MSG);
                            }
                        }
                        else if(sender instanceof Player) {
                            if (hasPermission(sender, "acuteloot.salvage")) {
                                salvageCommand(sender, args);
                                return true;
                            } else {
                                sender.sendMessage(PERM_DENIED_MSG);
                            }
                        } else {
                            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "You have to be a player!");
                        }
                        return true;
                    }
                    else{
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Salvaging is not enabled");
                        return true;
                    }
                }

                // Invalid subcommand
                else {
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Subcommand not found!");
                    return true;
                }

            }

            // Root permission node/command (/al)
            else {
                if (hasPermission(sender, "acuteloot") || sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + "AcuteLoot version: " + ChatColor.YELLOW + plugin.getDescription()
                                                                                                                .getVersion());
                    //TODO: Print lootCode version as well
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
