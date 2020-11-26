package acute.loot;

import acute.loot.namegen.FixedNameGenerator;
import acute.loot.namegen.JPKanaNameGenerator;
import acute.loot.namegen.NameGenerator;
import acute.loot.namegen.PrefixSuffixNameGenerator;

import java.util.stream.IntStream;

public class TestNameGen {

    private static void printNExamples(int n, NameGenerator generator, LootMaterial material, LootRarity rarity) {
        IntStream.rangeClosed(1, n)
                 .forEach(i -> System.out.println(generator.generate(material, rarity)));
    }

    public static void main(String[] args) {
        new TestHelper().addTestResources();

        final LootMaterial mat = LootMaterial.SWORD;
        final LootRarity rarity = LootRarity.get(1);

        System.out.println("Testing prefix generator:");
        printNExamples(10, PrefixSuffixNameGenerator.getPrefixGenerator(), mat, rarity);
        System.out.println();

        System.out.println("Testing suffixOf generator:");
        printNExamples(10, PrefixSuffixNameGenerator.getSuffixGenerator("of"), mat, rarity);
        System.out.println();

        System.out.println("Testing prefixSuffixOf generator:");
        printNExamples(10, PrefixSuffixNameGenerator.getPrefixSuffixGenerator("de"), mat, rarity);
        System.out.println();

        System.out.println("Testing jpKana generator:");
        printNExamples(10, JPKanaNameGenerator.jpKanaNameGenerator, mat, rarity);
        System.out.println();

        System.out.println("Testing fixedNamePool generator:");
        printNExamples(10, FixedNameGenerator.defaultGenerator(), mat, rarity);
        System.out.println();
    }

}
