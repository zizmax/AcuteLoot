package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;

public class FixedNameGenerator implements NameGenerator {

    private final NamePool namePool;

    public FixedNameGenerator(NamePool namePool) {
        this.namePool = namePool;
    }

    @Override
    public String generate(LootMaterial lootMaterial, LootRarity rarity) {
        return namePool.drawName(lootMaterial);
    }

    public static FixedNameGenerator defaultGenerator() {
        final NamePool namePool = new NamePool.FileBuilder()
                .swordFile("plugins/AcuteLoot/names/fixed/swords.txt")
                .bowFile("plugins/AcuteLoot/names/fixed/bows.txt")
                .pickFile("plugins/AcuteLoot/names/fixed/picks.txt")
                .axeFile("plugins/AcuteLoot/names/fixed/axes.txt")
                .shovelFile("plugins/AcuteLoot/names/fixed/shovels.txt")
                .hoeFile("plugins/AcuteLoot/names/fixed/hoes.txt")
                .crossbowFile("plugins/AcuteLoot/names/fixed/crossbows.txt")
                .fishingRodFile("plugins/AcuteLoot/names/fixed/fishing_rods.txt")
                .tridentFile("plugins/AcuteLoot/names/fixed/tridents.txt")
                .bootsFile("plugins/AcuteLoot/names/fixed/boots.txt")
                .pantsFile("plugins/AcuteLoot/names/fixed/leggings.txt")
                .chestPlateFile("plugins/AcuteLoot/names/fixed/chest_plates.txt")
                .helmetFile("plugins/AcuteLoot/names/fixed/helmets.txt")
                .genericFile("plugins/AcuteLoot/names/fixed/generic.txt")
                .build();
        return new FixedNameGenerator(namePool);
    }

    public long numberOfNames() {
        long count = 0;
        for (LootMaterial mat : LootMaterial.values()) {
            try {
                count += namePool.getNames(mat).size();
            } catch (IllegalArgumentException e) {
                // Material not supported, so no names
            }
        }

        return count;
    }
}
