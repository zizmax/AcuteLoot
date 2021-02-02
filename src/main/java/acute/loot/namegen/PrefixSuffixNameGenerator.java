package acute.loot.namegen;

import java.util.Arrays;

public class PrefixSuffixNameGenerator {

    public static NameGenerator getPrefixGenerator() {
        return new CompoundNameGenerator(Arrays.asList(
                new NamePoolNameGenerator(FixedListNamePool.defaultPrefixPool()),
                new NamePoolNameGenerator(new MaterialNamePool.FileBuilder().defaultNameFiles().build())
        ));
    }

    public static NameGenerator getSuffixGenerator(String conjunction) {
        return new CompoundNameGenerator(Arrays.asList(
                new NamePoolNameGenerator(new MaterialNamePool.FileBuilder().defaultNameFiles().build()),
                new ConstantNameGenerator(conjunction),
                new NamePoolNameGenerator(FixedListNamePool.defaultSuffixPool())
        ));
    }

    public static NameGenerator getPrefixSuffixGenerator(String conjunction) {
        return new CompoundNameGenerator(Arrays.asList(
                new NamePoolNameGenerator(FixedListNamePool.defaultPrefixPool()),
                new NamePoolNameGenerator(new MaterialNamePool.FileBuilder().defaultNameFiles().build()),
                new ConstantNameGenerator(conjunction),
                new NamePoolNameGenerator(FixedListNamePool.defaultSuffixPool())
        ));
    }
}
