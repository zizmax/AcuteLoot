package acute.loot.namegen;

import java.util.HashMap;
import java.util.Map;

public class DefaultNameGenerators {

    private static final Map<String, NameGenerator> variableMap;

    static {
        variableMap = new HashMap<>();
        variableMap.put("prefix", FixedListNameGenerator.defaultPrefixPool());
        variableMap.put("suffix", FixedListNameGenerator.defaultSuffixPool());
        variableMap.put("kana", FixedListNameGenerator.kanaPool());
        variableMap.put("item_name", new MaterialNameGenerator.FileBuilder().defaultNameFiles()
                                                                            .defaultPrefix()
                                                                            .build());
        variableMap.put("fixed", new MaterialNameGenerator.FileBuilder().defaultNameFiles()
                                                                        .prefix("plugins/AcuteLoot/names/fixed/")
                                                                        .build());
    }

    public static final NameGenerator jpKanaNameGenerator = NameGenerator.compile("[kana](2-5)", variableMap);

    public static NameGenerator getPrefixGenerator() {
        return NameGenerator.compile("[prefix] [item_name]", variableMap);
    }

    public static NameGenerator getSuffixGenerator(String conjunction) {
        return NameGenerator.compile(String.format("[item_name] %s [suffix]", conjunction), variableMap);
    }

    public static NameGenerator getPrefixSuffixGenerator(String conjunction) {
        return NameGenerator.compile(String.format("[prefix] [item_name] %s [suffix]", conjunction), variableMap);
    }

    public static NameGenerator fixedNameGenerator() {
        return NameGenerator.compile("[fixed]", variableMap);
    }
}
