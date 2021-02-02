package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;

import java.util.List;
import java.util.Objects;

import static acute.loot.namegen.NamePool.readNames;

public class MaterialNamePool implements NamePool {

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

    public MaterialNamePool(FileBuilder builder) {
        this.swordNames = readNames(builder.swordFile);
        this.pickNames = readNames(builder.pickFile);
        this.bowNames = readNames(builder.bowFile);
        this.helmetNames = readNames(builder.helmetFile);
        this.bootsNames = readNames(builder.bootsFile);
        this.crossbowNames = readNames(builder.crossbowFile);
        this.shovelNames = readNames(builder.shovelFile);
        this.hoeNames = readNames(builder.hoeFile);
        this.axeNames = readNames(builder.axeFile);
        this.pantsNames = readNames(builder.pantsFile);
        this.chestPlateNames = readNames(builder.chestPlateFile);
        this.fishingRodNames = readNames(builder.fishingRodFile);
        this.tridentNames = readNames(builder.tridentFile);
        this.genericNames = readNames(builder.genericFile);
    }

    public List<String> getNames(LootMaterial lootMaterial, LootRarity lootRarity) {
        switch (lootMaterial) {
            case SWORD:       return swordNames;
            case PICK:        return pickNames;
            case BOW:         return bowNames;
            case HELMET:      return helmetNames;
            case BOOTS:       return bootsNames;
            case CROSSBOW:    return crossbowNames;
            case SHOVEL:      return shovelNames;
            case HOE:         return hoeNames;
            case AXE:         return axeNames;
            case PANTS:       return pantsNames;
            case CHEST_PLATE: return chestPlateNames;
            case FISHING_ROD: return fishingRodNames;
            case TRIDENT:     return tridentNames;
            case GENERIC:     return genericNames;

            default:
                throw new IllegalArgumentException("Loot material " + lootMaterial + " not implemented.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaterialNamePool namePool = (MaterialNamePool) o;
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
                genericNames.equals(namePool.genericNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(swordNames, pickNames, bowNames, helmetNames, bootsNames, crossbowNames, shovelNames, hoeNames, axeNames, pantsNames, chestPlateNames, fishingRodNames, tridentNames, genericNames);
    }

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

        public FileBuilder() {}

        public MaterialNamePool build() {
            return new MaterialNamePool(this);
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
                    .genericFile(DEFAULT_GENERIC_NAME_FILE);

        }

        public static final String PREFIX = "plugins/AcuteLoot/";
        public static final String DEFAULT_SWORD_NAME_FILE = PREFIX + "names/swords.txt";
        public static final String DEFAULT_BOW_NAME_FILE = PREFIX + "names/bows.txt";
        public static final String DEFAULT_PICK_NAME_FILE = PREFIX + "names/picks.txt";
        public static final String DEFAULT_AXE_NAME_FILE = PREFIX + "names/axes.txt";
        public static final String DEFAULT_SHOVEL_NAME_FILE = PREFIX + "names/shovels.txt";
        public static final String DEFAULT_HOE_NAME_FILE = PREFIX + "names/hoes.txt";
        public static final String DEFAULT_CROSSBOW_NAME_FILE = PREFIX + "names/crossbows.txt";
        public static final String DEFAULT_FISHING_ROD_NAME_FILE = PREFIX + "names/fishing_rods.txt";
        public static final String DEFAULT_TRIDENT_NAME_FILE = PREFIX + "names/tridents.txt";
        public static final String DEFAULT_BOOTS_NAME_FILE = PREFIX + "names/boots.txt";
        public static final String DEFAULT_LEGGINGS_NAME_FILE = PREFIX + "names/leggings.txt";
        public static final String DEFAULT_CHEST_PLATE_NAME_FILE = PREFIX + "names/chest_plates.txt";
        public static final String DEFAULT_HELMET_NAME_FILE = PREFIX + "names/helmets.txt";
        public static final String DEFAULT_GENERIC_NAME_FILE = PREFIX + "names/generic.txt";
    }

}
