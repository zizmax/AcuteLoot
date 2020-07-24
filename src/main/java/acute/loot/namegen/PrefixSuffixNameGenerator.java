package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;
import acute.loot.Util;

public class PrefixSuffixNameGenerator implements NameGenerator {

    private final TriFunction<String, String, String, String> combiner;

    public PrefixSuffixNameGenerator(TriFunction<String, String, String, String> combiner) {
        this.combiner = combiner;
    }

    @Override
    public String generate(LootMaterial lootMaterial, LootRarity rarity) {
        final String name = Util.drawRandom(rarity.namesForMaterial(lootMaterial));
        final String prefix = Util.drawRandom(rarity.getPrefixNames());
        final String suffix = Util.drawRandom(rarity.getSuffixNames());
        return combiner.apply(prefix, name, suffix);
    }

    public static NameGenerator getPrefixGenerator() {
        return new PrefixSuffixNameGenerator((p, n, s) -> p + " " + n);
    }

    public static NameGenerator getSuffixGenerator(String conjunction) {
        return new PrefixSuffixNameGenerator((p, n, s) -> n + " " + conjunction + " " + s);
    }

    public static NameGenerator getPrefixSuffixGenerator(String conjunction) {
        return new PrefixSuffixNameGenerator((p, n, s) -> p + " " + n + " " + conjunction + " " + s);
    }
}
