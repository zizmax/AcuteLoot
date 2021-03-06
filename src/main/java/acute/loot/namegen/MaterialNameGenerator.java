package acute.loot.namegen;

import static acute.loot.namegen.NameGenerator.readNames;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;
import base.util.Util;

import java.util.List;
import java.util.Objects;

/**
 * NameGenerator the selects names from a fixed list based on the
 * material and ignoring the rarity, which may be null. Names for a given
 * material may or may not exist.
 */
public class MaterialNameGenerator implements NameGenerator {

    private final List<String> swordNames;
    private final List<String> pickNames;
    private final List<String> bowNames;
    private final List<String> helmetNames;
    private final List<String> bootsNames;
    private final List<String> crossbowNames;
    private final List<String> shovelNames;
    private final List<String> hoeNames;
    private final List<String> axeNames;
    private final List<String> pantsNames;
    private final List<String> chestPlateNames;
    private final List<String> fishingRodNames;
    private final List<String> tridentNames;
    private final List<String> genericNames;
    private final List<String> shieldNames;
    private final List<String> elytraNames;

    /**
     * Construct a new MaterialNameGenerator using the given FileBuilder.
     *
     * @param builder the FileBuilder pointing to the names files for this generator
     */
    public MaterialNameGenerator(FileBuilder builder) {
        Objects.requireNonNull(builder);
        this.swordNames = readNames(builder.prefix + builder.swordFile);
        this.pickNames = readNames(builder.prefix + builder.pickFile);
        this.bowNames = readNames(builder.prefix + builder.bowFile);
        this.helmetNames = readNames(builder.prefix + builder.helmetFile);
        this.bootsNames = readNames(builder.prefix + builder.bootsFile);
        this.crossbowNames = readNames(builder.prefix + builder.crossbowFile);
        this.shovelNames = readNames(builder.prefix + builder.shovelFile);
        this.hoeNames = readNames(builder.prefix + builder.hoeFile);
        this.axeNames = readNames(builder.prefix + builder.axeFile);
        this.pantsNames = readNames(builder.prefix + builder.pantsFile);
        this.chestPlateNames = readNames(builder.prefix + builder.chestPlateFile);
        this.fishingRodNames = readNames(builder.prefix + builder.fishingRodFile);
        this.tridentNames = readNames(builder.prefix + builder.tridentFile);
        this.genericNames = readNames(builder.prefix + builder.genericFile);
        this.shieldNames = readNames(builder.prefix + builder.shieldFile);
        this.elytraNames = readNames(builder.prefix + builder.elytraFile);
    }

    /**
     * Get the individual names list for the given material.
     *
     * @param lootMaterial the material
     * @return the names list for the material
     */
    public List<String> getNamesForMaterial(LootMaterial lootMaterial) {
        switch (lootMaterial) {
            case SWORD:
                return swordNames;
            case PICK:
                return pickNames;
            case BOW:
                return bowNames;
            case HELMET:
                return helmetNames;
            case BOOTS:
                return bootsNames;
            case CROSSBOW:
                return crossbowNames;
            case SHOVEL:
                return shovelNames;
            case HOE:
                return hoeNames;
            case AXE:
                return axeNames;
            case PANTS:
                return pantsNames;
            case CHEST_PLATE:
                return chestPlateNames;
            case FISHING_ROD:
                return fishingRodNames;
            case TRIDENT:
                return tridentNames;
            case GENERIC:
                return genericNames;
            case SHIELD:
                return shieldNames;
            case ELYTRA:
                return elytraNames;

            default:
                throw new IllegalArgumentException("Loot material " + lootMaterial + " not implemented.");
        }
    }

    @Override
    public String generate(LootMaterial lootMaterial, LootRarity rarity) {
        return Util.drawRandom(getNamesForMaterial(lootMaterial));
    }

    @Override
    public long countNumberOfNames() {
        long sum = 0;
        for (LootMaterial material : LootMaterial.values()) {
            try {
                sum += getNamesForMaterial(material).size();
            } catch (IllegalArgumentException ignored) { /* Material not supported, ignore */ }
        }
        return sum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaterialNameGenerator namePool = (MaterialNameGenerator) o;
        return swordNames.equals(namePool.swordNames) &&
                pickNames.equals(namePool.pickNames) &&
                bowNames.equals(namePool.bowNames) &&
                helmetNames.equals(namePool.helmetNames) &&
                bootsNames.equals(namePool.bootsNames) &&
                crossbowNames.equals(namePool.crossbowNames) &&
                shovelNames.equals(namePool.shovelNames) &&
                hoeNames.equals(namePool.hoeNames) &&
                axeNames.equals(namePool.axeNames) &&
                pantsNames.equals(namePool.pantsNames) &&
                chestPlateNames.equals(namePool.chestPlateNames) &&
                fishingRodNames.equals(namePool.fishingRodNames) &&
                tridentNames.equals(namePool.tridentNames) &&
                genericNames.equals(namePool.genericNames) &&
                shieldNames.equals(namePool.shieldNames) &&
                elytraNames.equals(namePool.elytraNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(swordNames, pickNames, bowNames, helmetNames, bootsNames, crossbowNames, shovelNames,
                            hoeNames, axeNames, pantsNames, chestPlateNames, fishingRodNames, tridentNames,
                            genericNames, shieldNames, elytraNames);
    }

    /**
     * Builder for a MaterialNameGenerator based on a set of files, one for
     * each material. Typical usage of this class is:
     *
     * <p>new FileBuilder().defaultNameFiles().prefix("your prefix/").build()
     */
    public static class FileBuilder {
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

        public FileBuilder() {
        }

        public MaterialNameGenerator build() {
            return new MaterialNameGenerator(this);
        }

        public FileBuilder swordFile(String swordFile) {
            this.swordFile = swordFile;
            return this;
        }

        public FileBuilder pickFile(String pickFile) {
            this.pickFile = pickFile;
            return this;
        }

        public FileBuilder bowFile(String bowFile) {
            this.bowFile = bowFile;
            return this;
        }

        public FileBuilder helmetFile(String helmetFile) {
            this.helmetFile = helmetFile;
            return this;
        }

        public FileBuilder bootsFile(String bootsFile) {
            this.bootsFile = bootsFile;
            return this;
        }

        public FileBuilder crossbowFile(String crossbowFile) {
            this.crossbowFile = crossbowFile;
            return this;
        }

        public FileBuilder shovelFile(String shovelFile) {
            this.shovelFile = shovelFile;
            return this;
        }

        public FileBuilder hoeFile(String hoeFile) {
            this.hoeFile = hoeFile;
            return this;
        }

        public FileBuilder axeFile(String axeFile) {
            this.axeFile = axeFile;
            return this;
        }

        public FileBuilder pantsFile(String pantsFile) {
            this.pantsFile = pantsFile;
            return this;
        }

        public FileBuilder chestPlateFile(String chestPlateFile) {
            this.chestPlateFile = chestPlateFile;
            return this;
        }

        public FileBuilder fishingRodFile(String fishingRodFile) {
            this.fishingRodFile = fishingRodFile;
            return this;
        }

        public FileBuilder tridentFile(String tridentFile) {
            this.tridentFile = tridentFile;
            return this;
        }

        public FileBuilder genericFile(String genericFile) {
            this.genericFile = genericFile;
            return this;
        }

        public FileBuilder shieldFile(String shieldFile) {
            this.shieldFile = shieldFile;
            return this;
        }

        public FileBuilder elytraFile(String elytraFile) {
            this.elytraFile = elytraFile;
            return this;
        }

        public FileBuilder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * Initialize with the default name files.
         *
         * @return this builder
         */
        public FileBuilder defaultNameFiles() {
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

}
