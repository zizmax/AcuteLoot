package acute.loot;

import acute.loot.namegen.*;
import org.bstats.bukkit.Metrics;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
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
    public static final String UPDATE_AVAILABLE = "An update is available! AcuteLoot %s may be downloaded on SpigotMC";
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
    public static final HashMap<String, Integer> effectNames = new HashMap<>();
    public static final HashMap<String, NameGenerator> nameGeneratorNames = new HashMap<>();

    // Minecraft version: Used for materials compatibility
    public static final int serverVersion = Integer.parseInt(Bukkit.getBukkitVersion().substring(2,4));

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
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        // Connect to bStats
        int bStatsID = 7348;
        Metrics metrics = new Metrics(this, bStatsID);

        // Configure name generators, rarities, and effects
        reloadConfiguration();
        if (!this.isEnabled()) return;

        // Set the AcuteLoot instance for the API
        API.setAcuteLoot(this);

        // Check for updates
        UpdateChecker.init(this, spigotID).requestUpdateCheck().whenComplete((result, exception) -> {
            if (result.requiresUpdate()) {
                this.getLogger().warning(String.format(UPDATE_AVAILABLE, result.getNewestVersion()));
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
        final long birthdayCount = PermutationCounts.birthdayProblem(PermutationCounts.totalPermutations(getConfig().getBoolean("kana-namegen")), 0.5, 0.0001);
        getLogger().info(String.format("Total number of possible names: ~%,d", PermutationCounts.totalPermutations(getConfig().getBoolean("kana-namegen"))));
        getLogger().info(String.format("Approximately %,d names before ~50%% chance of a duplicate", birthdayCount));

        getLogger().info("Enabled");
    }

    public void reloadConfiguration() {
        // Reload config
        reloadConfig();
        saveDefaultConfig();

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
                "helmets", "hoes", "leggings", "picks", "prefixes", "shovels", "suffixes",
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

        // prefix, prefixSuffixOf, suffixOf -> 30% each
        // jp, fixed -> 5% each
        nameGenChancePool.clear();
        final String conjunction = getConfig().getString("conjunction");
        nameGenChancePool.add(PrefixSuffixNameGenerator.getPrefixGenerator(), 6);
        nameGeneratorNames.put("prefixGenerator", PrefixSuffixNameGenerator.getPrefixGenerator());
        nameGenChancePool.add(PrefixSuffixNameGenerator.getSuffixGenerator(conjunction), 6);
        nameGeneratorNames.put("suffixGenerator", PrefixSuffixNameGenerator.getSuffixGenerator(conjunction));
        nameGenChancePool.add(PrefixSuffixNameGenerator.getPrefixSuffixGenerator(conjunction), 6);
        nameGeneratorNames.put("prefixSuffixGenerator", PrefixSuffixNameGenerator.getPrefixSuffixGenerator(conjunction));
        if (getConfig().getBoolean("kana-namegen")) {
            nameGenChancePool.add(JPKanaNameGenerator.jpKanaNameGenerator, 1);
            nameGeneratorNames.put("kanaGenerator", JPKanaNameGenerator.jpKanaNameGenerator);
        }
        nameGenChancePool.add(FixedNameGenerator.defaultGenerator(), 1);
        nameGeneratorNames.put("fixedGenerator", FixedNameGenerator.defaultGenerator());


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
        LootSpecialEffect.getEffects().clear();

        // Tool Particle
        LootSpecialEffect.registerEffect(new ToolParticleEffect("weapons_laser", 1, Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE), Particle.REDSTONE, true, this));
        LootSpecialEffect.registerEffect(new ToolParticleEffect("weapons_note", 2, Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE), Particle.NOTE, false, this));
        LootSpecialEffect.registerEffect(new ToolParticleEffect("weapons_lava", 3, Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE), Particle.LAVA, false, this));
        LootSpecialEffect.registerEffect(new ToolParticleEffect("weapons_enchanting-table", 4, Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE), Particle.ENCHANTMENT_TABLE, false, this));
        LootSpecialEffect.registerEffect(new ToolParticleEffect("weapons_potion-effect", 5, Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE), Particle.SPELL_MOB, false, this));
        LootSpecialEffect.registerEffect(new ToolParticleEffect("weapons_nautilus", 6, Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE), Particle.NAUTILUS, false, this));
        LootSpecialEffect.registerEffect(new ToolParticleEffect("weapons_slime", 7, Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE), Particle.SLIME, false, this));
        LootSpecialEffect.registerEffect(new ToolParticleEffect("weapons_water-splash", 8, Arrays.asList(LootMaterial.SWORD, LootMaterial.AXE), Particle.WATER_SPLASH, false, this));

        // Bow Teleport
        LootSpecialEffect.registerEffect(new BowTeleportEffect("enderbow", 9,  Arrays.asList(LootMaterial.BOW, LootMaterial.CROSSBOW), this));

        // Bow Particle
        LootSpecialEffect.registerEffect(new BowParticleEffect("bows_heart", 10, Arrays.asList(LootMaterial.BOW, LootMaterial.CROSSBOW), Particle.HEART, this ));
        LootSpecialEffect.registerEffect(new BowParticleEffect("bows_purple-spark", 11, Arrays.asList(LootMaterial.BOW, LootMaterial.CROSSBOW), Particle.SPELL_WITCH, this ));
        LootSpecialEffect.registerEffect(new BowParticleEffect("bows_lava", 12, Arrays.asList(LootMaterial.BOW, LootMaterial.CROSSBOW), Particle.LAVA, this ));
        LootSpecialEffect.registerEffect(new BowParticleEffect("bows_drip", 13, Arrays.asList(LootMaterial.BOW, LootMaterial.CROSSBOW), Particle.DRIP_LAVA, this ));
        LootSpecialEffect.registerEffect(new BowParticleEffect("bows_sparkle", 14, Arrays.asList(LootMaterial.BOW, LootMaterial.CROSSBOW), Particle.TOTEM, this ));

        // Block Trail
        LootSpecialEffect.registerEffect(new BlockTrailEffect("gardener", 15, Collections.singletonList(LootMaterial.BOOTS), this));

        // XP Boost
        LootSpecialEffect.registerEffect(new XPBoostEffect("xp-boost", 16, Collections.singletonList(LootMaterial.HELMET), this));

        // Time Walker
        LootSpecialEffect.registerEffect(new TimewalkEffect("timewalker", 17, Collections.singletonList(LootMaterial.BOOTS), this));

        // Dead Eye
        LootSpecialEffect.registerEffect(new DeadEyeEffect("dead-eye", 18, Collections.singletonList(LootMaterial.BOW), this));

        //Medusa (Gorgon)
        LootSpecialEffect.registerEffect(new MedusaEffect("medusa", 19, Collections.singletonList(LootMaterial.BOW), this));

        // Rebuild the effect chance pool
        effectChancePool.clear();
        effectNames.clear();
        for (LootSpecialEffect effect : LootSpecialEffect.getEffects().values()) {
            int chance = getConfig().getInt("effects." + effect.getName().replace("_", ".") + ".chance");
            if(debug) getLogger().info(effect.getName() + ": " + chance);
            effectChancePool.add(effect, chance);
            // Add "tab completer-safe" name to HashMap of effects
            effectNames.put(effect.getName(), effect.getId());
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
            effectChancePool.add(LootSpecialEffect.get(20), 1);
            // Add "tab completer-safe" name to HashMap of effects
            effectNames.put("Moonboots", 20);
            */

        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled");
    }
}
