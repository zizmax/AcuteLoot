package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;

import java.util.List;

public class FixedListNamePool implements NamePool {

    private final List<String> names;

    public FixedListNamePool(List<String> names) {
        this.names = names;
    }

    @Override
    public List<String> getNames(LootMaterial lootMaterial, LootRarity lootRarity) {
        return names;
    }

    public static FixedListNamePool fromNamesFile(final String namesFile) {
        return new FixedListNamePool(NamePool.readNames(namesFile));
    }

    public static FixedListNamePool defaultPrefixPool() {
        return fromNamesFile(DEFAULT_PREFIX_NAME_FILE);
    }

    public static FixedListNamePool defaultSuffixPool() {
        return fromNamesFile(DEFAULT_SUFFIX_NAME_FILE);
    }

    public static final String DEFAULT_PREFIX_NAME_FILE = "plugins/AcuteLoot/" + "names/prefixes.txt";
    public static final String DEFAULT_SUFFIX_NAME_FILE = "plugins/AcuteLoot/" + "names/suffixes.txt";
}
