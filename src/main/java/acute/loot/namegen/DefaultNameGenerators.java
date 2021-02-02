package acute.loot.namegen;

import java.util.Arrays;

public class DefaultNameGenerators {
    public static final NameGenerator jpKanaNameGenerator = new RepeatedNameGenerator(FixedListNameGenerator.kanaPool(), 2, 5);

    public static NameGenerator getPrefixGenerator() {
        return new CompoundNameGenerator(Arrays.asList(
                FixedListNameGenerator.defaultPrefixPool(),
                new MaterialNameGenerator.FileBuilder().defaultNameFiles().build()
        ));
    }

    public static NameGenerator getSuffixGenerator(String conjunction) {
        return new CompoundNameGenerator(Arrays.asList(
                new MaterialNameGenerator.FileBuilder().defaultNameFiles().build(),
                new ConstantNameGenerator(conjunction),
                FixedListNameGenerator.defaultSuffixPool()
        ));
    }

    public static NameGenerator getPrefixSuffixGenerator(String conjunction) {
        return new CompoundNameGenerator(Arrays.asList(
                FixedListNameGenerator.defaultPrefixPool(),
                new MaterialNameGenerator.FileBuilder().defaultNameFiles().build(),
                new ConstantNameGenerator(conjunction),
                FixedListNameGenerator.defaultSuffixPool()
        ));
    }

    public static NameGenerator fixedNameGenerator() {
        return new MaterialNameGenerator.FileBuilder()
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
    }
}
