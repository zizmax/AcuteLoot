package acute.loot;

import acute.loot.commands.*;
import acute.loot.namegen.*;
import base.collections.IntegerChancePool;
import base.commands.TabCompletedMultiCommand;
import base.util.Util;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static acute.loot.LootSpecialEffect.registerEffect;

/**
 * Main plugin class.
 */
public final class AcuteLoot extends JavaPlugin {

    public static final Random random = new Random();

    static {
        Util.setRandom(random);
    }

    public static final String CHAT_PREFIX = ChatColor.GOLD + "[" + ChatColor.GRAY + "AcuteLoot" + ChatColor.GOLD + "] " + ChatColor.GRAY;
    public static final String PERM_DENIED_MSG = CHAT_PREFIX + "You do not have permission to do this";
    public static final String SPIGOT_URL = "https://www.spigotmc.org/resources/acuteloot.81899";
    public static final String UPDATE_AVAILABLE = "Update available! Download v%s ";
    public static final String UP_TO_DATE = "AcuteLoot is up to date: (%s)";
    public static final String UNRELEASED_VERSION = "Version (%s) is more recent than the one publicly available. Dev build?";
    public static final String UPDATE_CHECK_FAILED = "Could not check for updates. Reason: ";
    public static final int spigotID = 81899;

    // Minecraft version: Used for materials compatibility
    // Defaults to -1 before the plugin has loaded, useful for tests
    public static int serverVersion = -1;

    public List<Material> lootMaterials = new ArrayList<>();

    public boolean debug = false;

    public TabCompletedMultiCommand acuteLootCommand;

    public LootWell lootWell;

    public final IntegerChancePool<LootRarity> rarityChancePool = new IntegerChancePool<>(random);
    public final IntegerChancePool<LootSpecialEffect> effectChancePool = new IntegerChancePool<>(random);
    public final IntegerChancePool<NameGenerator> nameGenChancePool = new IntegerChancePool<>(random);

    public final HashMap<String, Integer> rarityNames = new HashMap<>();
    public final HashMap<String, String> effectNames = new HashMap<>();
    public final HashMap<String, NameGenerator> nameGeneratorNames = new HashMap<>();
    public LootItemGenerator lootGenerator;

    public static final int configVersion = 1;

    @Override
    public void onEnable() {
        getLogger().info("+----------------------------------------------------------------+");
        getLogger().info("|                      AcuteLoot Community                       |");
        getLogger().info("+================================================================+");
        getLogger().info("| * Please report bugs at: https://git.io/JkJLD                  |");
        getLogger().info("| * Join the AcuteLoot Discord at: https://discord.gg/BXhUUQEymg |");
        getLogger().info("| * Enjoying the plugin? Leave a review and share with a friend! |");
        getLogger().info("+----------------------------------------------------------------+");

        // Register events
        getServer().getPluginManager().registerEvents(new EffectEventListener(this), this);
        getServer().getPluginManager().registerEvents(new LootCreationEventListener(this), this);

        // Save/read config.yml
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        // Connect to bStats
        int bStatsId = 7348;
        Metrics metrics = new Metrics(this, bStatsId);

        // Set server version
        serverVersion = Integer.parseInt(Bukkit.getBukkitVersion().substring(2, 4));

        // Configure name generators, rarities, and effects
        reloadConfiguration();
        if (!this.isEnabled()) {
            return;
        }

        // Set the AcuteLoot instance for the API
        API.setAcuteLoot(this);

        // Check for updates
        UpdateChecker.init(this, spigotID).requestUpdateCheck().whenComplete((result, exception) -> {
            if (result.requiresUpdate()) {
                this.getLogger().warning((String.format(ChatColor.RED +
                        AcuteLoot.UPDATE_AVAILABLE, result.getNewestVersion()) + "at " +
                        ChatColor.UNDERLINE + AcuteLoot.SPIGOT_URL));
                return;
            }

            UpdateChecker.UpdateReason reason = result.getReason();
            if (reason == UpdateChecker.UpdateReason.UP_TO_DATE) {
                this.getLogger().info(String.format(UP_TO_DATE, result.getNewestVersion()));
            } else if (reason == UpdateChecker.UpdateReason.UNRELEASED_VERSION) {
                this.getLogger().info(String.format(UNRELEASED_VERSION, result.getNewestVersion()));
            } else {
                this.getLogger().warning(UPDATE_CHECK_FAILED + reason);
            }
        });

        if (debug) {
            getLogger().info(rarityChancePool.toString());
            getLogger().info(effectChancePool.toString());
        }

        //birthdayProblem();
        final long birthdayCount = PermutationCounts.birthdayProblem(PermutationCounts.totalPermutations(nameGenChancePool), 0.5, 0.0001);
        getLogger().info(String.format("Total number of possible names: ~%,d", PermutationCounts.totalPermutations(nameGenChancePool)));
        getLogger().info(String.format("Approximately %,d names before ~50%% chance of a duplicate", birthdayCount));

        getLogger().info(String.format("v%s Enabled!", getDescription().getVersion()));
    }

    private void checkConfigVersion() {
        int installedVersion = getIntIfDefined("config-version", getConfig());
        if (installedVersion < configVersion) {
            try {
                Files.copy(Paths.get("plugins/AcuteLoot/config.yml"),
                           Paths.get("plugins/AcuteLoot/config.bak"),
                           StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                getLogger().warning("Failed to create backup config!");
            }
            getLogger().warning(String.format("[CONFIG OUT OF DATE]: Your version (v%d) is behind the current version (v%d)",
                                              installedVersion, configVersion));
            getLogger().warning("This means you are missing new/updated config options!");
            getLogger().warning("AcuteLoot will work correctly, but will use the defaults for missing options.");
            getLogger().warning("To view the latest version of the config: https://git.io/JtgCf");
            getLogger().warning("To use the new options:");
            getLogger().warning("   1) Delete the current config (a backup has been made for you called config.bak)");
            getLogger().warning("   2) Restart the server or use /al reload. It will generate a brand new config");
            getLogger().warning("   3) Paste your old options into the newly generated config");
            getLogger().warning("   4) Enjoy the new features! :)");
        }
    }

    private int getIntIfDefined(String key, ConfigurationSection config) {
        // Modified version of code from:
        // https://www.spigotmc.org/threads/prevent-defaults-coming-from-default-config-yml.439927/
        // This is due to odd and annoying behavior of getConfig() to pull from config defaults if they don't exist on disk
        if (config.getKeys(false).contains(key)) {
            return config.getInt(key);
        }
        return 0;
    }

    public void reloadConfiguration() {
        // Reload config
        saveDefaultConfig();
        reloadConfig();
        checkConfigVersion();

        // Set debug mode
        debug = getConfig().getBoolean("debug");
        if (debug) {
            this.getLogger().warning("Debug mode enabled!");
        }

        // Create loot well(s)
        if (debug) {
            lootWell = new LootWell(this);
        }

        // Writing names files to disk if they don't exist
        File namesFolder = new File("plugins/AcuteLoot/names");
        if (!namesFolder.exists()) {
            namesFolder.mkdir();
            getLogger().info("Created names folder");
        }

        File fixedFolder = new File("plugins/AcuteLoot/names/fixed");
        if (!fixedFolder.exists()) {
            fixedFolder.mkdir();
            getLogger().info("Created fixed names folder");
        }

        String[] namesFiles = { "axes", "boots", "bows", "chest_plates", "crossbows", "fishing_rods", "generic",
                                "helmets", "hoes", "kana", "leggings", "picks", "prefixes", "shovels", "suffixes",
                                "swords", "tridents"};

        String[] fixedNamesFiles = { "axes", "boots", "bows", "chest_plates", "crossbows", "fishing_rods", "generic",
                                     "helmets", "hoes", "leggings", "picks", "shovels", "swords", "tridents"};

        for (String fileName : namesFiles) {
            File fileToCheck = new File("plugins/AcuteLoot/names/" + fileName + ".txt");
            if (!fileToCheck.exists()) {
                try {
                    Files.copy(this.getClass().getResourceAsStream("/names/" + fileName + ".txt"),
                               Paths.get("plugins/AcuteLoot/names/" + fileName + ".txt"), StandardCopyOption.REPLACE_EXISTING);
                    getLogger().info("Wrote " + fileName + ".txt file");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Fixed names
        for (String fileName : fixedNamesFiles) {
            File fileToCheck = new File("plugins/AcuteLoot/names/fixed/" + fileName + ".txt");
            if (!fileToCheck.exists()) {
                try {
                    Files.copy(this.getClass().getResourceAsStream("/names/fixed/" + fileName + ".txt"),
                               Paths.get("plugins/AcuteLoot/names/fixed/" + fileName + ".txt"), StandardCopyOption.REPLACE_EXISTING);
                    getLogger().info("Wrote fixed/" + fileName + ".txt file");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Materials file
        String fileName = "materials";
        if (debug) {
            getLogger().info("Detected Major MC version: 1." + serverVersion);
        }
        String version;
        // MC version 1.16 or above
        if (serverVersion > 15) {
            version = "1.16";
        } else { // MC version 1.15 or below
            version = "1.15";
        }
        File fileToCheck = new File("plugins/AcuteLoot/" + fileName + ".txt");
        if (!fileToCheck.exists()) {
            try {
                Files.copy(this.getClass().getResourceAsStream("/" + fileName + version + ".txt"),
                           Paths.get("plugins/AcuteLoot/" + fileName + ".txt"), StandardCopyOption.REPLACE_EXISTING);
                getLogger().info("Wrote " + version + " " + fileName + ".txt file");
            } catch (IOException e) {
                this.getLogger().severe("IO Exception");
                e.printStackTrace();
            }
        }
        // Initialize materials array
        createMaterials(this, "plugins/AcuteLoot/" + fileName + ".txt");

        // Set up name generators

        final Map<String, NameGenerator> namePools = new HashMap<>();
        for (Map<?, ?> namePool : getConfig().getMapList("name-pools")) {
            final String name = (String) namePool.get("name");
            final String type = (String) namePool.get("type");
            if (type.equals("common")) {
                namePools.put(name, FixedListNameGenerator.fromNamesFile((String) namePool.get("file")));
            } else if (type.equals("material")) {
                namePools.put(name, new MaterialNameGenerator.FileBuilder().defaultNameFiles()
                                                                           .prefix((String) namePool.get("folder"))
                                                                           .build());
            } else {
                getLogger().warning(String.format("Unknown name pool type '%s'. Skipping.", type));
            }
        }

        // Commands
        acuteLootCommand = new TabCompletedMultiCommand();
        configureCommands(acuteLootCommand);
        getCommand("acuteloot").setExecutor(acuteLootCommand);

        nameGenChancePool.clear();
        nameGeneratorNames.clear();
        for (Map<?, ?> nameGenerator : getConfig().getMapList("name-generators")) {
            final String name = (String) nameGenerator.get("name");
            final int rarity = (Integer) nameGenerator.get("rarity");
            final String pattern = (String) nameGenerator.get("pattern");

            try {
                NameGenerator generator = NameGenerator.compile(pattern, namePools, s -> getLogger().warning(s));
                if (getConfig().getBoolean("capitalize-names")) {
                    generator = TransformationNameGenerator.uppercaser(generator);
                }
                nameGenChancePool.add(generator, rarity);
                nameGeneratorNames.put(name.replace(' ', '_'), generator); // Add "tab completer-safe" name
            } catch (IllegalArgumentException e) {
                getLogger().warning("Could not compile name generator pattern '" + pattern + "'");
                getLogger().warning("Exception was: ");
                getLogger().warning(e.getMessage());
                getLogger().warning(Arrays.toString(e.getStackTrace()));
            }
        }
        getLogger().info("Loaded " + nameGenChancePool.values().size() + " name generators");

        // Set up rarities (changes in id's must be updated further down as well)
        LootRarity.getRarities().clear();
        rarityChancePool.clear();
        rarityNames.clear();
        for (String key : getConfig().getConfigurationSection("rarities").getKeys(false)) {
            int id;
            try {
                id = Integer.parseInt(key);
            } catch (Exception e) {
                getLogger().severe("Fatal config error on rarity ID: " + key);
                getLogger().severe("Are you sure the ID is an integer?");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            String name = getConfig().getString("rarities." + id + ".name");

            // Add "tab completer-safe" name to HashMap of rarities
            rarityNames.put(name.replace(' ', '_'), id);
            String color = ChatColor.translateAlternateColorCodes('&', getConfig().getString("rarities." + id + ".color"));
            int chance = getConfig().getInt("rarities." + id + ".chance");
            double effectChance = getConfig().getInt("rarities." + id + ".effect-chance") / 100.0;
            if (debug) {
                getLogger().info("Chance: " + chance);
                getLogger().info("Effect Chance: " + effectChance);
            }
            LootRarity.registerRarity(new LootRarity(id, name, effectChance, color));
            rarityChancePool.add(LootRarity.get(id), chance);
        }
        getLogger().info("Registered " + rarityChancePool.values().size() + " rarities");

        // Set up effects

        // Clear any existing effects
        LootSpecialEffect.getEffects(LootSpecialEffect.AL_NS).clear();

        final List<LootMaterial> axeSwordMat = Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE);
        final List<LootMaterial> bowMat = Arrays.asList(LootMaterial.BOW, LootMaterial.CROSSBOW);

        // Tool Particle
        registerEffect(new ToolParticleEffect("weapons_laser", 1, axeSwordMat, Particle.REDSTONE, true, this));
        registerEffect(new ToolParticleEffect("weapons_note", 2, axeSwordMat, Particle.NOTE, false, this));
        registerEffect(new ToolParticleEffect("weapons_lava", 3, axeSwordMat, Particle.LAVA, false, this));
        registerEffect(new ToolParticleEffect("weapons_enchanting-table", 4, axeSwordMat, Particle.ENCHANTMENT_TABLE, false, this));
        registerEffect(new ToolParticleEffect("weapons_potion-effect", 5, axeSwordMat, Particle.SPELL_MOB, false, this));
        registerEffect(new ToolParticleEffect("weapons_nautilus", 6, axeSwordMat, Particle.NAUTILUS, false, this));
        registerEffect(new ToolParticleEffect("weapons_slime", 7, axeSwordMat, Particle.SLIME, false, this));
        registerEffect(new ToolParticleEffect("weapons_water-splash", 8, axeSwordMat, Particle.WATER_SPLASH, false, this));

        // Bow Teleport
        registerEffect(new BowTeleportEffect("enderbow", 9, bowMat, this));

        // Bow Particle
        registerEffect(new BowParticleEffect("bows_heart", 10, bowMat, Particle.HEART, this));
        registerEffect(new BowParticleEffect("bows_purple-spark", 11, bowMat, Particle.SPELL_WITCH, this));
        registerEffect(new BowParticleEffect("bows_lava", 12, bowMat, Particle.LAVA, this));
        registerEffect(new BowParticleEffect("bows_drip", 13, bowMat, Particle.DRIP_LAVA, this));
        registerEffect(new BowParticleEffect("bows_sparkle", 14, bowMat, Particle.TOTEM, this));

        // Block Trail
        registerEffect(new BlockTrailEffect("gardener", 15, Collections.singletonList(LootMaterial.BOOTS), this));

        // XP Boost
        registerEffect(new XpBoostEffect("xp-boost", 16, Collections.singletonList(LootMaterial.HELMET), this));

        // Time Walker
        registerEffect(new TimewalkEffect("timewalker", 17, Collections.singletonList(LootMaterial.BOOTS), this));

        // Dead Eye
        registerEffect(new DeadEyeEffect("dead-eye", 18, Collections.singletonList(LootMaterial.BOW), this));

        //Medusa (Gorgon)
        registerEffect(new MedusaEffect("medusa", 19, Collections.singletonList(LootMaterial.BOW), this));

        // Rebuild the effect chance pool
        effectChancePool.clear();
        effectNames.clear();
        for (LootSpecialEffect effect : LootSpecialEffect.getEffects(LootSpecialEffect.AL_NS).values()) {
            int chance = getConfig().getInt("effects." + effect.getName().replace("_", ".") + ".chance");
            if (debug) {
                getLogger().info(effect.getName() + ": " + chance);
            }
            effectChancePool.addDiscardingInvalid(effect, chance);
            // Add "tab completer-safe" name to HashMap of effects
            //FIXME: Append namespace for duplicate effect names across different namespaces
            effectNames.put(effect.getName(), effect.effectId().toString());
        }

        // Dev Effects (currently being tested)
        if (debug) {

            // Register effect, add effect to chancePool, add effect to effectNames

            /*
            // COMING SOON //

            // Diviner
            LootSpecialEffect.registerEffect(new DivinerEffect("diviner", 20, Collections.singletonList(LootMaterial.SHOVEL), this));
            effectChancePool.add(LootSpecialEffect.get(20), 1);
            // Add "tab completer-safe" name to HashMap of effects
            effectNames.put("Diviner", 20);

            // Midas Touch
            LootSpecialEffect.registerEffect(new MidasEffect("midas", 21, Collections.singletonList(LootMaterial.CHEST_PLATE), this));
            effectChancePool.add(LootSpecialEffect.get(21), 1);
            // Add "tab completer-safe" name to HashMap of effects
            effectNames.put("Midas", 21);

            // MoonBoots
            LootSpecialEffect.registerEffect(new MoonBootsEffect("moonboots", 20, Collections.singletonList(LootMaterial.BOOTS), this));
            effectChancePool.add(LootSpecialEffect.get(22), 1);
            // Add "tab completer-safe" name to HashMap of effects
            effectNames.put("Moonboots", 22);
            */

        }

        lootGenerator = new LootItemGenerator(rarityChancePool, effectChancePool, nameGenChancePool, this);
    }

    public String getUiString(String messageName) {
        if (getConfig().contains("msg." + messageName)) {
            return ChatColor.translateAlternateColorCodes('&', getConfig().getString("msg." + messageName));
        } else {
            getLogger().warning("Config message error at: msg." + messageName);
            return ChatColor.DARK_RED + "[" + ChatColor.BLACK + "Config Error" + ChatColor.DARK_RED + "]";
        }
    }

    /**
     * Get the loot code off the given item, or null if it cannot be read.
     *
     * @param item the item to get the loot code from
     * @return the loot code from the item
     */
    public String getLootCode(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        NamespacedKey key = new NamespacedKey(this, "lootCodeKey");
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(key, PersistentDataType.STRING)) {
            String foundValue = container.get(key, PersistentDataType.STRING);
            if (foundValue != null && foundValue.contains("#AL")) {
                return foundValue;
            }
        }
        return null;
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled");
    }

    private void createMaterials(AcuteLoot plugin, String path) {
        lootMaterials = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(path))) {
            List<String> lines = stream.collect(Collectors.toList());
            for (String line : lines) {
                if (!line.contains("#") && !line.trim().equals("")) {
                    String[] materialStrings = line.split(",");
                    for (String material : materialStrings) {
                        material = material.trim();
                        if (!material.equals("")) {
                            try {
                                Material mat = Material.matchMaterial(material);
                                if (mat != null) {
                                    lootMaterials.add(mat);
                                } else {
                                    throw new NullPointerException();
                                }
                            } catch (IllegalArgumentException | NullPointerException e) {
                                plugin.getLogger()
                                      .warning(material +
                                              " not valid material for server version: " +
                                              Bukkit.getBukkitVersion() + ". Skipping...");
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger()
                  .severe("Fatal IO exception while initializing materials.txt. Is file missing or corrupted?");
        }
        LootMaterial.setGenericMaterialsList(lootMaterials);
        plugin.getLogger().info("Initialized " + lootMaterials.size() + " materials");
    }

    private void configureCommands(final TabCompletedMultiCommand alCommand) {
        alCommand.setCannotBeUsedByConsole(CHAT_PREFIX + "You have to be a player!");
        alCommand.setCannotBeUsedByPlayer(CHAT_PREFIX + "You have to be a console!");
        alCommand.setUnknownCommand(CHAT_PREFIX + "Subcommand not found!");
        alCommand.setNoArgsCommand(new NoArgsCommand("acuteloot", this));

        alCommand.registerPlayerSubcommand("add", new AddCommand("acuteloot.add", this));

        alCommand.registerPlayerSubcommand("chest", new ChestCommand("acuteloot.chest", this));

        alCommand.registerGenericSubcommand("give", new GiveCommand("acuteloot.give", this));

        alCommand.registerGenericSubcommand("help", new HelpCommand("acuteloot.help", this));

        alCommand.registerPlayerSubcommand("name", new NameCommand("acuteloot.name", this));

        alCommand.registerPlayerSubcommand("new", new NewLootCommand("acuteloot.new", this));

        alCommand.registerPlayerSubcommand("reload", new ReloadCommand.PlayerReloadCommand("acuteloot.reload", this));
        alCommand.registerConsoleSubcommand("reload", new ReloadCommand.ConsoleReloadCommand("acuteloot.reload", this));

        alCommand.registerPlayerSubcommand("remove", new RemoveCommand("acuteloot.remove", this));

        alCommand.registerPlayerSubcommand("rename", new RenameCommand("acuteloot.rename", this));

        alCommand.registerGenericSubcommand("salvage", new SalvageCommand(this));

        alCommand.registerPlayerSubcommand("stats", new StatsCommand.PlayerStatsCommand("acuteloot.stats", this));
        alCommand.registerConsoleSubcommand("stats", new StatsCommand.ConsoleStatsCommand("acuteloot.stats", this));

        final TabCompleter addAndNewCompletion = (s, c, l, args) -> {
            switch (args.length) {
                case 2:
                    return StringUtil.copyPartialMatches(args[1], rarityNames.keySet(), new ArrayList<>());
                case 3:
                    return StringUtil.copyPartialMatches(args[2], effectNames.keySet(), new ArrayList<>());
                default:
                    return null;
            }
        };

        final TabCompleter giveCompletion = (s, c, l, args) -> {
            // Same logic as above for 'add' and 'new' but shifted by one to account for player name
            // Note: this means args[0] is the player name, not "give"...
            return addAndNewCompletion.onTabComplete(s, c, l, Arrays.copyOfRange(args, 1, args.length));
        };

        final TabCompleter nameCompletion = (s, c, l, args) -> args.length == 2 ? StringUtil.copyPartialMatches(args[1], nameGeneratorNames
                .keySet(), new ArrayList<>()) : null;

        alCommand.registerSubcompletion("add", addAndNewCompletion);
        alCommand.registerSubcompletion("new", addAndNewCompletion);
        alCommand.registerSubcompletion("give", giveCompletion);
        alCommand.registerSubcompletion("name", nameCompletion);
    }

    public static void sendIncompatibleEffectsWarning(CommandSender sender, LootItem lootItem, ItemStack item) {
        if (lootItem == null) {
            return;
        }
        for (LootSpecialEffect effect : lootItem.getEffects()) {
            if (!effect.getValidMaterials().contains(LootMaterial.lootMaterialForMaterial(item.getType()))) {
                sender.sendMessage(ChatColor.GOLD + "[" + ChatColor.RED + "WARNING" + ChatColor.GOLD + "] " + ChatColor.GRAY + effect
                        .getName() + " not strictly compatible with this item!");
                sender.sendMessage(ChatColor.GOLD + "[" + ChatColor.RED + "WARNING" + ChatColor.GOLD + "] " + ChatColor.GRAY + "Effect may not work as expected/won't do anything");
            }
        }
    }

    public boolean hasPermission(CommandSender sender, String node) {
        return (!getConfig().getBoolean("use-permissions") && sender.isOp()) ||
               (getConfig().getBoolean("use-permissions") && sender.hasPermission(node));
    }
}
