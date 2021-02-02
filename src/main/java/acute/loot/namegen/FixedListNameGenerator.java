package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;
import acute.loot.Util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FixedListNameGenerator implements NameGenerator {

    private final List<String> names;

    public FixedListNameGenerator(String... names) {
        this(Arrays.asList(names));
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FixedListNameGenerator that = (FixedListNameGenerator) o;
        return Objects.equals(names, that.names);
    }

    @Override
    public int hashCode() {
        return Objects.hash(names);
    }
}
