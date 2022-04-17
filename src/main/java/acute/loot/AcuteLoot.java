package acute.loot;

import static acute.loot.LootSpecialEffect.registerEffect;

import acute.loot.commands.*;
import acute.loot.generator.LootItemGenerator;
import acute.loot.listener.EnchantingLootListener;
import acute.loot.rules.LootRulesModule;
import acute.loot.namegen.*;
import acute.loot.tables.LootTableParser;
import com.github.phillip.h.acutelib.collections.IntegerChancePool;
import com.github.phillip.h.acutelib.commands.TabCompletedMultiCommand;
import com.github.phillip.h.acutelib.util.Util;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main plugin class.
 */
public class AcuteLoot extends JavaPlugin {

    public static final Random random = new Random();

    static {
        Util.setRandom(random);
    }

    public static final String CHAT_PREFIX = ChatColor.GOLD + "[" + ChatColor.GRAY + "AcuteLoot" + ChatColor.GOLD + "] " + ChatColor.GRAY;
    public static String PERM_DENIED_MSG = CHAT_PREFIX + "You do not have permission to do this";
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

    private @Getter AlConfig globalConfig;
    private final @Getter Map<String, AlConfig> worldConfigs = new HashMap<>();

    private @Getter LootSource enchantingLootSource;

    private final @Getter ModuleManager moduleManager;

    private final @Getter AlApi alApi;

    public AcuteLoot() {
        alApi = new AlApi(this);
        moduleManager = new ModuleManager(this, getLogger());
        moduleManager.add("debugMode", new DebugModule(this), "debug");
        moduleManager.add("lootRules", new LootRulesModule(alApi), "lootRules");
    }

    public static final int configVersion = 13;

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
        addListener(EffectEventListener::new);
        addListener(LootCreationEventListener::new);
        addListener(UiEventListener::new);
        addListener(EnchantingLootListener::new);

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

        final long totalNames = PermutationCounts.totalPermutations(nameGenChancePool.values());
        final long birthdayCount = PermutationCounts.birthdayProblem(totalNames, 0.5, 0.0001);
        getLogger().info(String.format("Total number of possible names: ~%,d", totalNames));
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

    /**
     * Separate class for reloading the config and registering names/effects to do so on startup and /al reload.
     */
    public void reloadConfiguration() {
        moduleManager.stop();
        moduleManager.reload();
        moduleManager.preStart();

        // Reload config
        saveDefaultConfig();
        reloadConfig();
        checkConfigVersion();

        PERM_DENIED_MSG = CHAT_PREFIX + getConfig().getString("msg.generic.no-permission");

        // Set debug mode
        if (debug) {
            this.getLogger().warning("Debug mode enabled!");
        }

        // Create loot well(s)
        if (debug) {
            lootWell = new LootWell(this);
        }

        // Write default files to disk if they don't exist
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/defaults.txt")))) {
            br.lines().forEach(this::writeIfNotPresent);
        } catch (IOException e) {
            getLogger().severe("Failed to read default files list: " + e.getMessage());
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
            if (serverVersion > 16) {
                version = "1.17";

            }
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

        // Set up global and per-world configs
        worldConfigs.clear();
        globalConfig = AlConfig.buildConfig(getConfig().getValues(true));
        for (Map<?, ?> worldConfig : getConfig().getMapList("world-settings")) {
            worldConfigs.put((String) worldConfig.get("world-name"), AlConfig.buildConfig(worldConfig));
        }
        getLogger().fine("GLOBAL CONFIG:");
        getLogger().fine(globalConfig.toString());
        getLogger().fine("WORLD CONFIGS:");
        worldConfigs.forEach((key1, value) -> getLogger().fine(key1 + ": " + value));

        // Set up name generators

        final Map<String, NameGenerator> namePools = new HashMap<>();
        for (Map<?, ?> namePool : getConfig().getMapList("name-pools")) {
            final String name = (String) namePool.get("name");
            final String type = (String) namePool.get("type");
            if (type.equals("common")) {
                namePools.put(name, FixedListNameGenerator.fromNamesFile((String) namePool.get("file")));
            } else if (type.equals("material")) {
                namePools.put(name, new MaterialNameGenBuilder().defaultNameFiles()
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

        if (serverVersion >= 17) {
            //Light Walker
            registerEffect(new BlockTrailEffect("light-walker", 20, Collections.singletonList(LootMaterial.BOOTS), this));
        }


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

            if (serverVersion >= 17) {
            registerEffect(new ToolParticleEffect("weapons_dripstone", 20, axeSwordMat, Particle.DRIPPING_DRIPSTONE_LAVA, false, this));
            registerEffect(new ToolParticleEffect("weapons_spark", 21, axeSwordMat, Particle.ELECTRIC_SPARK, false, this));
            registerEffect(new ToolParticleEffect("weapons_glow", 22, axeSwordMat, Particle.GLOW, false, this));
            registerEffect(new ToolParticleEffect("weapons_ink", 23, axeSwordMat, Particle.GLOW_SQUID_INK, false, this));
            registerEffect(new ToolParticleEffect("weapons_scrape", 24, axeSwordMat, Particle.SCRAPE, false, this));
            registerEffect(new ToolParticleEffect("weapons_spore", 25, axeSwordMat, Particle.SPORE_BLOSSOM_AIR, false, this));
            registerEffect(new ToolParticleEffect("weapons_vibration", 26, axeSwordMat, Particle.VIBRATION, false, this));
            registerEffect(new ToolParticleEffect("weapons_waxon", 27, axeSwordMat, Particle.WAX_OFF, false, this));
            registerEffect(new ToolParticleEffect("weapons_waxoff", 28, axeSwordMat, Particle.WAX_ON, false, this));

        }

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


        lootGenerator = LootItemGenerator.builder(this)
                                         .namePool(nameGenChancePool, true, true)
                                         .build();

        // Rebuild loot tables
        alApi.clearLootTables();
        new LootTableParser(alApi).parseAndAddLootTables(getConfig().getConfigurationSection("loot-tables"));

        final boolean usePermissions = getConfig().getBoolean("use-permissions");
        final boolean overwriteNames = getConfig().getBoolean("loot-sources.enchanting.overwrite-existing-name");
        final boolean overwriteColors = getConfig().getBoolean("loot-sources.enchanting.overwrite-existing-colors");
        final LootItemGenerator enchantingGenerator = LootItemGenerator.builder(this)
                                                                       .namePool(nameGenChancePool, overwriteNames, overwriteColors)
                                                                       .build();

        final Map<String, Boolean> enchantingConfigs = Util.mapMap(worldConfigs, AlConfig::isEnchantingEnabled);
        enchantingLootSource = new LootSource(globalConfig.isEnchantingEnabled(), enchantingConfigs,
                                              usePermissions, "acuteloot.enchant",
                                              enchantingGenerator);

        moduleManager.start();
    }

    private void writeIfNotPresent(final String path) {
        try {
            final File fileToCheck = new File(getDataFolder(), path);
            Files.createDirectories(fileToCheck.getParentFile().toPath());
            if (!fileToCheck.exists()) {
                Files.copy(getClass().getResourceAsStream(path), fileToCheck.toPath());
                getLogger().info("Wrote " + fileToCheck.getName());
            }
        } catch (IOException e) {
            getLogger().severe("Failed to write file: " + e.getMessage());
        }
    }

    /**
     * Returns formatted UI message from config.
     *
     * @param messageName name of message name to construct YAML node
     * @return the message or an error string
     */
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

        alCommand.registerPlayerSubcommand("chest", new ChestCommand.CreateChestCommand("acuteloot.chest", this));

        alCommand.registerGenericSubcommand("give", new GiveCommand("acuteloot.give", this));

        alCommand.registerGenericSubcommand("help", new HelpCommand("acuteloot.help", this));

        alCommand.registerPlayerSubcommand("name", new NameCommand("acuteloot.name", this));

        alCommand.registerPlayerSubcommand("new", new NewLootCommand("acuteloot.new", this));

        alCommand.registerPlayerSubcommand("reload", new ReloadCommand.PlayerReloadCommand("acuteloot.reload", this));
        alCommand.registerConsoleSubcommand("reload", new ReloadCommand.ConsoleReloadCommand("acuteloot.reload", this));

        alCommand.registerPlayerSubcommand("remove", new RemoveCommand("acuteloot.remove", this));

        alCommand.registerPlayerSubcommand("rename", new RenameCommand("acuteloot.rename", this));

        alCommand.registerPlayerSubcommand("reroll", new RerollCommand("acuteloot.reroll", this));

        alCommand.registerGenericSubcommand("salvage", new SalvageCommand(this));

        alCommand.registerPlayerSubcommand("stats", new StatsCommand.PlayerStatsCommand("acuteloot.stats", this));
        alCommand.registerConsoleSubcommand("stats", new StatsCommand.ConsoleStatsCommand("acuteloot.stats", this));

        alCommand.registerPlayerSubcommand("rmchest", new ChestCommand.RemoveChestCommand("acuteloot.rmchest", this));
        alCommand.registerPlayerSubcommand("share", new ShareCommand("acuteloot.share", this));

        alCommand.registerGenericSubcommand("enable", new EnableCommand("acuteloot.enable", this));
        alCommand.registerGenericSubcommand("disable", new DisableCommand("acuteloot.disable", this));

        alCommand.registerPlayerSubcommand("repair", new RepairCommand("acuteloot.repair", this));

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

        final TabCompleter chestCompletion = (s, c, l, args) -> {
            switch (args.length) {
                case 2:
                    return StringUtil.copyPartialMatches(args[1],
                            Arrays.asList("-1", "1", "5", "10", "60", "240", "480", "720", "1440"),
                            new ArrayList<>());
                case 3:
                    return StringUtil.copyPartialMatches(args[2],
                            Arrays.asList("1", "2", "4", "8"),
                            new ArrayList<>());
                case 4:
                    return StringUtil.copyPartialMatches(args[3],
                            Collections.singletonList("true"),
                            new ArrayList<>());
                default:
                    return null;
            }
        };

        final TabCompleter rmChestCompletion = (s, c, l, args) -> {
            if (args.length == 2) {
                return StringUtil.copyPartialMatches(args[1],
                        Arrays.asList("1", "2", "4", "8"),
                        new ArrayList<>());
            } else {
                return null;
            }
        };

        final TabCompleter enableDisableCompletion = (s, c, l, args) -> {
            if (args.length == 2) {
                return StringUtil.copyPartialMatches(args[1], moduleManager.modules(), new ArrayList<>());
            }
            return null;
        };

        alCommand.registerSubcompletion("add", addAndNewCompletion);
        alCommand.registerSubcompletion("new", addAndNewCompletion);
        alCommand.registerSubcompletion("give", giveCompletion);
        alCommand.registerSubcompletion("name", nameCompletion);
        alCommand.registerSubcompletion("chest", chestCompletion);
        alCommand.registerSubcompletion("rmchest", rmChestCompletion);
        alCommand.registerSubcompletion("enable", enableDisableCompletion);
        alCommand.registerSubcompletion("disable", enableDisableCompletion);
    }

    /**
     * Sends player a warning that the item they just generated has an effect that is not guaranteed to work.
     *
     * @param sender the player or console that executed the command
     * @param lootItem the AcuteLoot item object
     * @param item the ItemStack of the AcuteLoot item
     */
    public static void sendIncompatibleEffectsWarning(CommandSender sender, LootItem lootItem, ItemStack item) {
        if (lootItem == null) {
            return;
        }
        for (LootSpecialEffect effect : lootItem.getEffects()) {
            if (!effect.getValidMaterials().contains(LootMaterial.lootMaterialForMaterial(item.getType()))) {
                sender.sendMessage(ChatColor.GOLD + "[" + ChatColor.RED + "WARNING" + ChatColor.GOLD + "] " + ChatColor.GRAY + effect
                        .getName() + " not strictly compatible with this item!");
                sender.sendMessage(ChatColor.GOLD + "[" + ChatColor.RED + "WARNING" + ChatColor.GOLD + "] " + ChatColor.GRAY +
                        "Effect may not work as expected/won't do anything");
            }
        }
    }

    public boolean hasPermission(CommandSender sender, String node) {
        return (!getConfig().getBoolean("use-permissions") && sender.isOp()) ||
               (getConfig().getBoolean("use-permissions") && sender.hasPermission(node));
    }

    protected IntegerChancePool<LootRarity> rarityChancePool() {
        return rarityChancePool;
    }

    public LootItemGenerator lootGenerator() {
        return lootGenerator;
    }

    public IntegerChancePool<NameGenerator> namePool() {
        return nameGenChancePool;
    }

    public boolean effectsEnabled(final World world) {
        return worldConfigs.getOrDefault(world.getName(), globalConfig).isEffectsEnabled();
    }

    public boolean chestLootEnabled(final World world) {
        return worldConfigs.getOrDefault(world.getName(), globalConfig).isChestsEnabled();
    }

    public boolean fishingLootEnabled(final World world) {
        return worldConfigs.getOrDefault(world.getName(), globalConfig).isFishingEnabled();
    }

    public boolean anvilLootEnabled(final World world) {
        return worldConfigs.getOrDefault(world.getName(), globalConfig).isAnvilsEnabled();
    }

    private void addListener(Function<AcuteLoot, Listener> constructor) {
        getServer().getPluginManager().registerEvents(constructor.apply(this), this);
    }

}
