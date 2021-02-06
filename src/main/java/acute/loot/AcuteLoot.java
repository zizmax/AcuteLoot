package acute.loot;

import acute.loot.namegen.*;
import org.bstats.bukkit.Metrics;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import java.util.Random;

public final class AcuteLoot extends JavaPlugin {

    public static final Random random = new Random();

    public static final int spigotID = 81899;

    public static boolean debug = false;

    public static final String CHAT_PREFIX = ChatColor.GOLD + "[" + ChatColor.GRAY + "AcuteLoot" + ChatColor.GOLD + "] " + ChatColor.GRAY;
    public static final String SPIGOT_URL = "https://www.spigotmc.org/resources/acuteloot.81899";
    public static final String UPDATE_AVAILABLE = "Update available! Download v%s ";
    public static final String UP_TO_DATE = "AcuteLoot is up to date: (%s)";
    public static final String UNRELEASED_VERSION = "Version (%s) is more recent than the one publicly available. Dev build?";
    public static final String UPDATE_CHECK_FAILED = "Could not check for updates. Reason: ";

    // Maybe these shouldn't be static?
    // But, will there ever be multiple AcuteLoot instances?
    // Maybe access them from getters just in case...
    public static final IntegerChancePool<LootRarity> rarityChancePool = new IntegerChancePool<>(random);
    public static final IntegerChancePool<LootSpecialEffect> effectChancePool = new IntegerChancePool<>(random);
    public static final IntegerChancePool<NameGenerator> nameGenChancePool = new IntegerChancePool<>(random);

    public static final HashMap<String, Integer> rarityNames = new HashMap<>();
    public static final HashMap<String, String> effectNames = new HashMap<>();
    public static final HashMap<String, NameGenerator> nameGeneratorNames = new HashMap<>();

    // Minecraft version: Used for materials compatibility
    // Defaults to -1 before the plugin has loaded, useful for tests
    public static int serverVersion = -1;

    int configVersion = 1;

    @Override
    public void onEnable() {
        getLogger().info("+----------------------------------------------------------------+");
        getLogger().info("|                      AcuteLoot Community                       |");
        getLogger().info("+================================================================+");
        getLogger().info("| * Please report bugs at: https://git.io/JkJLD                  |");
        getLogger().info("| * Join the AcuteLoot Discord at: https://discord.gg/BXhUUQEymg |");
        getLogger().info("| * Enjoying the plugin? Leave a review and share with a friend! |");
        getLogger().info("+----------------------------------------------------------------+");

        // Register events and commands
        getServer().getPluginManager().registerEvents(new Events(this), this);
        getCommand("acuteloot").setExecutor(new Commands(this));

        // Save/read config.yml
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        // Connect to bStats
        int bStatsID = 7348;
        Metrics metrics = new Metrics(this, bStatsID);

        // Set server version
        serverVersion = Integer.parseInt(Bukkit.getBukkitVersion().substring(2,4));

        // Configure name generators, rarities, and effects
        reloadConfiguration();
        if (!this.isEnabled()) return;

        // Set the AcuteLoot instance for the API
        API.setAcuteLoot(this);

        // Check for updates
        UpdateChecker.init(this, spigotID).requestUpdateCheck().whenComplete((result, exception) -> {
            if (result.requiresUpdate()) {
                this.getLogger().warning((String.format(ChatColor.RED +
                        AcuteLoot.UPDATE_AVAILABLE, result.getNewestVersion()) + "at "
                        + ChatColor.UNDERLINE + AcuteLoot.SPIGOT_URL));
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

        if(debug) {
            getLogger().info(rarityChancePool.toString());
            getLogger().info(effectChancePool.toString());
        }

        //birthdayProblem();
        final long birthdayCount = PermutationCounts.birthdayProblem(PermutationCounts.totalPermutations(), 0.5, 0.0001);
        getLogger().info(String.format("Total number of possible names: ~%,d", PermutationCounts.totalPermutations()));
        getLogger().info(String.format("Approximately %,d names before ~50%% chance of a duplicate", birthdayCount));

        getLogger().info("Enabled");
    }

    public void checkConfigVersion() {
        int installedVersion = getIntIfDefined("config-version", getConfig());
        if(installedVersion < configVersion){
            try {
                Files.copy(Paths.get("plugins/AcuteLoot/config.yml"), Paths.get("plugins/AcuteLoot/config.bak"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                getLogger().warning("Failed to create backup config!");
            }
            getLogger().warning(String.format("[CONFIG OUT OF DATE]: Your version (v%d) is behind the current version (v%d)", installedVersion, configVersion));
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

    private int getIntIfDefined(String key, ConfigurationSection config){
        // Modified version of code from:
        // https://www.spigotmc.org/threads/prevent-defaults-coming-from-default-config-yml.439927/
        // This is due to odd and annoying behavior of getConfig() to pull from config defaults if they don't exist on disk
        if(config.getKeys(false).contains(key)){
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
        if(debug) this.getLogger().warning("Debug mode enabled!");

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

        String[] namesFiles = {"axes", "boots", "bows", "chest_plates", "crossbows", "fishing_rods", "generic",
                "helmets", "hoes", "kana", "leggings", "picks", "prefixes", "shovels", "suffixes",
                "swords", "tridents"};

        String[] fixedNamesFiles = {"axes", "boots", "bows", "chest_plates", "crossbows", "fishing_rods", "generic",
                "helmets", "hoes", "leggings", "picks", "shovels", "swords", "tridents"};

        for (String fileName : namesFiles) {
            File fileToCheck = new File("plugins/AcuteLoot/names/" + fileName + ".txt");
            if (!fileToCheck.exists()) {
                try {
                    Files.copy(this.getClass().getResourceAsStream("/names/" + fileName + ".txt"), Paths.get("plugins/AcuteLoot/names/" + fileName + ".txt"), StandardCopyOption.REPLACE_EXISTING);
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
                    Files.copy(this.getClass().getResourceAsStream("/names/fixed/" + fileName + ".txt"), Paths.get("plugins/AcuteLoot/names/fixed/" + fileName + ".txt"), StandardCopyOption.REPLACE_EXISTING);
                    getLogger().info("Wrote fixed/" + fileName + ".txt file");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Materials file
        String fileName = "materials";
        if(debug) getLogger().info("Detected Major MC version: 1." + serverVersion);
        String version;
        // MC version 1.16 or above
        if (serverVersion > 15) {
            version = "1.16";
        }
        // MC version 1.15 or below
        else {
            version = "1.15";
        }
        File fileToCheck = new File("plugins/AcuteLoot/" + fileName + ".txt");
        if (!fileToCheck.exists()) {
            try {
                Files.copy(this.getClass().getResourceAsStream("/" + fileName + version + ".txt"), Paths.get("plugins/AcuteLoot/" + fileName + ".txt"), StandardCopyOption.REPLACE_EXISTING);
                getLogger().info("Wrote " + version + " " + fileName + ".txt file");
            } catch (IOException e) {
                this.getLogger().severe("IO Exception");
                e.printStackTrace();
            }
        }
        // Initialize materials array
        Events.createMaterials(this, "plugins/AcuteLoot/" + fileName + ".txt");

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
        for(String key : getConfig().getConfigurationSection("rarities").getKeys(false)){
            int id;
            try {
                id = Integer.parseInt(key);
            }
            catch (Exception e) {
                getLogger().severe("Fatal config error on rarity ID: " + key);
                getLogger().severe("Are you sure the ID is an integer?");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            String name = getConfig().getString("rarities." + id + ".name");

            // Add "tab completer-safe" name to HashMap of rarities
            rarityNames.put(name.replace(' ', '_'), id);
            String color = ChatColor.translateAlternateColorCodes('&', getConfig().getString("rarities." + id + ".color"));
            int chance =  getConfig().getInt("rarities." + id + ".chance");
            double effectChance = getConfig().getInt("rarities." + id + ".effect-chance") / 100.0;
            if(debug) {
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

        // Tool Particle
        LootSpecialEffect.registerEffect(new ToolParticleEffect("weapons_laser", LootSpecialEffect.AL_NS, 1, Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE), Particle.REDSTONE, true, this));
        LootSpecialEffect.registerEffect(new ToolParticleEffect("weapons_note", LootSpecialEffect.AL_NS, 2, Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE), Particle.NOTE, false, this));
        LootSpecialEffect.registerEffect(new ToolParticleEffect("weapons_lava", LootSpecialEffect.AL_NS, 3, Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE), Particle.LAVA, false, this));
        LootSpecialEffect.registerEffect(new ToolParticleEffect("weapons_enchanting-table", LootSpecialEffect.AL_NS, 4, Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE), Particle.ENCHANTMENT_TABLE, false, this));
        LootSpecialEffect.registerEffect(new ToolParticleEffect("weapons_potion-effect", LootSpecialEffect.AL_NS, 5, Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE), Particle.SPELL_MOB, false, this));
        LootSpecialEffect.registerEffect(new ToolParticleEffect("weapons_nautilus", LootSpecialEffect.AL_NS, 6, Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE), Particle.NAUTILUS, false, this));
        LootSpecialEffect.registerEffect(new ToolParticleEffect("weapons_slime", LootSpecialEffect.AL_NS, 7, Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE), Particle.SLIME, false, this));
        LootSpecialEffect.registerEffect(new ToolParticleEffect("weapons_water-splash", LootSpecialEffect.AL_NS, 8, Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE), Particle.WATER_SPLASH, false, this));

        // Bow Teleport
        LootSpecialEffect.registerEffect(new BowTeleportEffect("enderbow", LootSpecialEffect.AL_NS, 9,  Arrays.asList(LootMaterial.BOW, LootMaterial.CROSSBOW), this));

        // Bow Particle
        LootSpecialEffect.registerEffect(new BowParticleEffect("bows_heart", LootSpecialEffect.AL_NS, 10, Arrays.asList(LootMaterial.BOW, LootMaterial.CROSSBOW), Particle.HEART, this ));
        LootSpecialEffect.registerEffect(new BowParticleEffect("bows_purple-spark", LootSpecialEffect.AL_NS, 11, Arrays.asList(LootMaterial.BOW, LootMaterial.CROSSBOW), Particle.SPELL_WITCH, this ));
        LootSpecialEffect.registerEffect(new BowParticleEffect("bows_lava", LootSpecialEffect.AL_NS, 12, Arrays.asList(LootMaterial.BOW, LootMaterial.CROSSBOW), Particle.LAVA, this ));
        LootSpecialEffect.registerEffect(new BowParticleEffect("bows_drip", LootSpecialEffect.AL_NS, 13, Arrays.asList(LootMaterial.BOW, LootMaterial.CROSSBOW), Particle.DRIP_LAVA, this ));
        LootSpecialEffect.registerEffect(new BowParticleEffect("bows_sparkle", LootSpecialEffect.AL_NS, 14, Arrays.asList(LootMaterial.BOW, LootMaterial.CROSSBOW), Particle.TOTEM, this ));

        // Block Trail
        LootSpecialEffect.registerEffect(new BlockTrailEffect("gardener", LootSpecialEffect.AL_NS, 15, Collections.singletonList(LootMaterial.BOOTS), this));

        // XP Boost
        LootSpecialEffect.registerEffect(new XPBoostEffect("xp-boost", LootSpecialEffect.AL_NS, 16, Collections.singletonList(LootMaterial.HELMET), this));

        // Time Walker
        LootSpecialEffect.registerEffect(new TimewalkEffect("timewalker", LootSpecialEffect.AL_NS, 17, Collections.singletonList(LootMaterial.BOOTS), this));

        // Dead Eye
        LootSpecialEffect.registerEffect(new DeadEyeEffect("dead-eye", LootSpecialEffect.AL_NS, 18, Collections.singletonList(LootMaterial.BOW), this));

        //Medusa (Gorgon)
        LootSpecialEffect.registerEffect(new MedusaEffect("medusa", LootSpecialEffect.AL_NS, 19, Collections.singletonList(LootMaterial.BOW), this));

        // Rebuild the effect chance pool
        effectChancePool.clear();
        effectNames.clear();
        for (LootSpecialEffect effect : LootSpecialEffect.getEffects(LootSpecialEffect.AL_NS).values()) {
            int chance = getConfig().getInt("effects." + effect.getName().replace("_", ".") + ".chance");
            if(debug) getLogger().info(effect.getName() + ": " + chance);
            effectChancePool.addDiscardingInvalid(effect, chance);
            // Add "tab completer-safe" name to HashMap of effects
            //FIXME: Append namespace for duplicate effect names across different namespaces
            effectNames.put(effect.getName(), effect.effectId().toString());
        }

        // Dev Effects (currently being tested)
        if(debug) {

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
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled");
    }
}
