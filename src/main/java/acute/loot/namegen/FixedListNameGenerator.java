package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;
import acute.loot.Util;

import java.util.Collections;
import java.util.List;

public class FixedListNameGenerator implements NameGenerator {

    private final List<String> names;

    public FixedListNameGenerator(String constantName) {
        this(Collections.singletonList(constantName));
    }

    public FixedListNameGenerator(List<String> names) {
        this.names = names;
    }

    @Override
    public String generate(LootMaterial lootMaterial, LootRarity rarity) {
        return Util.drawRandom(names);
    }

    @Override
    public long countNumberOfNames() {
        return names.size();
    }

    public static FixedListNameGenerator fromNamesFile(final String namesFile) {
        return new FixedListNameGenerator(NameGenerator.readNames(namesFile));
    }

    public static FixedListNameGenerator defaultPrefixPool() {
        return fromNamesFile(DEFAULT_PREFIX_NAME_FILE);
    }

    public static FixedListNameGenerator defaultSuffixPool() {
        return fromNamesFile(DEFAULT_SUFFIX_NAME_FILE);
    }

    public static FixedListNameGenerator kanaPool() {
        return fromNamesFile(KANA_NAME_FILE);
    }

    public static final String DEFAULT_PREFIX_NAME_FILE = "plugins/AcuteLoot/names/prefixes.txt";
    public static final String DEFAULT_SUFFIX_NAME_FILE = "plugins/AcuteLoot/names/suffixes.txt";
    public static final String KANA_NAME_FILE = "plugins/AcuteLoot/names/kana.txt";
}
