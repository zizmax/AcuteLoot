package acute.loot;

import static acute.loot.namegen.NameGenerator.readNames;

import acute.loot.namegen.FixedListNameGenerator;
import acute.loot.namegen.MappedNameGenerator;
import acute.loot.namegen.NameGenerator;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builder for a MaterialNameGenerator based on a set of files, one for
 * each material. Typical usage of this class is:
 *
 * <p>new FileBuilder().defaultNameFiles().prefix("your prefix/").build()
 */
@Accessors(fluent = true)
@Setter
public class MaterialNameGenBuilder {
    private String swordFile;
    private String pickFile;
    private String bowFile;
    private String helmetFile;
    private String bootsFile;
    private String crossbowFile;
    private String shovelFile;
    private String hoeFile;
    private String axeFile;
    private String pantsFile;
    private String chestPlateFile;
    private String fishingRodFile;
    private String tridentFile;
    private String genericFile;
    private String shieldFile;
    private String elytraFile;

    private String prefix = "";

    /**
     * Build the NameGenerator.
     *
     * @return the NameGenerator
     */
    public NameGenerator build() {
        final Map<LootMaterial, String> mapping = new HashMap<>();

        mapping.put(LootMaterial.SWORD, swordFile);
        mapping.put(LootMaterial.PICK, pickFile);
        mapping.put(LootMaterial.BOW, bowFile);
        mapping.put(LootMaterial.HELMET, helmetFile);
        mapping.put(LootMaterial.BOOTS, bootsFile);
        mapping.put(LootMaterial.CROSSBOW, crossbowFile);
        mapping.put(LootMaterial.SHOVEL, shovelFile);
        mapping.put(LootMaterial.HOE, hoeFile);
        mapping.put(LootMaterial.AXE, axeFile);
        mapping.put(LootMaterial.PANTS, pantsFile);
        mapping.put(LootMaterial.CHEST_PLATE, chestPlateFile);
        mapping.put(LootMaterial.FISHING_ROD, fishingRodFile);
        mapping.put(LootMaterial.TRIDENT, tridentFile);
        mapping.put(LootMaterial.GENERIC, genericFile);
        mapping.put(LootMaterial.SHIELD, shieldFile);
        mapping.put(LootMaterial.ELYTRA, elytraFile);

        final Map<String, NameGenerator> map = mapping.entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey().name(),
                e -> new FixedListNameGenerator(readNames(prefix + e.getValue()))
        ));

        return new MappedNameGenerator("lootMaterial", map);
    }

    /**
     * Initialize with the default name files.
     *
     * @return this builder
     */
    public MaterialNameGenBuilder defaultNameFiles() {
        return swordFile(DEFAULT_SWORD_NAME_FILE)
                .bowFile(DEFAULT_BOW_NAME_FILE)
                .pickFile(DEFAULT_PICK_NAME_FILE)
                .axeFile(DEFAULT_AXE_NAME_FILE)
                .shovelFile(DEFAULT_SHOVEL_NAME_FILE)
                .hoeFile(DEFAULT_HOE_NAME_FILE)
                .crossbowFile(DEFAULT_CROSSBOW_NAME_FILE)
                .fishingRodFile(DEFAULT_FISHING_ROD_NAME_FILE)
                .tridentFile(DEFAULT_TRIDENT_NAME_FILE)
                .bootsFile(DEFAULT_BOOTS_NAME_FILE)
                .pantsFile(DEFAULT_LEGGINGS_NAME_FILE)
                .chestPlateFile(DEFAULT_CHEST_PLATE_NAME_FILE)
                .helmetFile(DEFAULT_HELMET_NAME_FILE)
                .genericFile(DEFAULT_GENERIC_NAME_FILE)
                .shieldFile(DEFAULT_SHIELD_NAME_FILE)
                .elytraFile(DEFAULT_ELYTRA_NAME_FILE);

    }

    public static final String DEFAULT_SWORD_NAME_FILE = "swords.txt";
    public static final String DEFAULT_BOW_NAME_FILE = "bows.txt";
    public static final String DEFAULT_PICK_NAME_FILE = "picks.txt";
    public static final String DEFAULT_AXE_NAME_FILE = "axes.txt";
    public static final String DEFAULT_SHOVEL_NAME_FILE = "shovels.txt";
    public static final String DEFAULT_HOE_NAME_FILE = "hoes.txt";
    public static final String DEFAULT_CROSSBOW_NAME_FILE = "crossbows.txt";
    public static final String DEFAULT_FISHING_ROD_NAME_FILE = "fishing_rods.txt";
    public static final String DEFAULT_TRIDENT_NAME_FILE = "tridents.txt";
    public static final String DEFAULT_BOOTS_NAME_FILE = "boots.txt";
    public static final String DEFAULT_LEGGINGS_NAME_FILE = "leggings.txt";
    public static final String DEFAULT_CHEST_PLATE_NAME_FILE = "chest_plates.txt";
    public static final String DEFAULT_HELMET_NAME_FILE = "helmets.txt";
    public static final String DEFAULT_GENERIC_NAME_FILE = "generic.txt";
    public static final String DEFAULT_SHIELD_NAME_FILE = "shields.txt";
    public static final String DEFAULT_ELYTRA_NAME_FILE = "elytras.txt";
}
