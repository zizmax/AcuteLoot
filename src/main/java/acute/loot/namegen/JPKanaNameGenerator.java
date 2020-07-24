package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;

import java.util.Random;
import java.util.stream.LongStream;

public class JPKanaNameGenerator implements NameGenerator {

    private static final Random random = new Random();

    public int maxLength;
    public int minLength;

    public JPKanaNameGenerator(int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    public static final JPKanaNameGenerator jpKanaNameGenerator = new JPKanaNameGenerator(2, 5);

    @Override
    public String generate(LootMaterial lootMaterial, LootRarity rarity) {
        int length = minLength + random.nextInt((maxLength + 1) - minLength);
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < length; i++) {
            name.append(japaneseKana[random.nextInt(japaneseKana.length)]);
        }
        name = new StringBuilder(name.substring(0, 1).toUpperCase() + name.substring(1));
        return name.toString();
    }

    public long numberOfNames() {
        return LongStream.range(minLength, maxLength + 1).map(i -> (int) Math.pow(japaneseKana.length, i)).sum();
    }

    private static final String[] japaneseKana = new String[]{
            "a", "i", "u", "e", "o",
            "ka", "ki", "ku", "ke", "ko",
            "sa", "shi", "su", "se", "so",
            "ta", "chi", "tsu", "te", "to",
            "na", "ni", "nu", "ne", "no",
            "ha", "hi", "fu", "he", "ho",
            "ma", "mi", "mu", "me", "mo",
            "ya", "yu", "yo",
            "ra", "ri", "ru", "re", "ro",
            "wa", "n",
            "ga", "gi", "gu", "ge", "go",
            "za", "ji", "zu", "ze", "zo",
            "da", "de", "do",
            "ba", "bi", "bu", "be", "bo",
            "pa", "pi", "pu", "pe", "po",
            "kya", "kyu", "kyo", "gya", "gyu", "gyo",
            "nya", "nyu", "nyo", "hya", "hyu", "hyo",
            "bya", "byu", "byo", "pya", "pyu", "pyo",
            "mya", "myu", "myo", "rya", "ryu", "ryo",
            "ja", "ju", "jo", "cha", "chu", "cho",
            "sha", "shu", "sho"
    };

}
