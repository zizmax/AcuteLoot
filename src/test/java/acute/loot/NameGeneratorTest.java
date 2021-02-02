package acute.loot;

import acute.loot.namegen.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class NameGeneratorTest {

    private static TestHelper testHelper;

    @BeforeAll
    public static void setup() {
        testHelper = new TestHelper();
        testHelper.addTestResources();
    }

    @AfterAll
    public static void reset() {
        testHelper.reset();
    }

    @Test
    @DisplayName("Name generators correct")
    public void nameGeneratorsCorrect() {
        final FixedListNameGenerator constantGenerator = new FixedListNameGenerator("Test");
        final FixedListNameGenerator listGenerator = new FixedListNameGenerator("Foo", "Bar");

        assertThat(constantGenerator.generate(LootMaterial.AXE, testHelper.uncommon), is("Test"));
        assertThat(constantGenerator.generate(LootMaterial.PICK, null), is("Test"));
        assertThat(constantGenerator.generate(null, testHelper.rare), is("Test"));
        assertThat(constantGenerator.generate(null, null), is("Test"));

        assertThat(listGenerator.generate(LootMaterial.AXE, testHelper.uncommon), oneOf("Foo", "Bar"));
        assertThat(listGenerator.generate(LootMaterial.PICK, null), oneOf("Foo", "Bar"));
        assertThat(listGenerator.generate(null, testHelper.rare), oneOf("Foo", "Bar"));
        assertThat(listGenerator.generate(null, null), oneOf("Foo", "Bar"));

        final RepeatedNameGenerator repeatedGenerator = new RepeatedNameGenerator(constantGenerator, 1, 3);
        assertThat(repeatedGenerator.generate(LootMaterial.AXE, testHelper.uncommon), oneOf("Test", "TestTest", "TestTestTest"));
        assertThat(repeatedGenerator.generate(LootMaterial.PICK, null), oneOf("Test", "TestTest", "TestTestTest"));
        assertThat(repeatedGenerator.generate(null, testHelper.rare), oneOf("Test", "TestTest", "TestTestTest"));
        assertThat(repeatedGenerator.generate(null, null), oneOf("Test", "TestTest", "TestTestTest"));

        final CompoundNameGenerator compoundGenerator = new CompoundNameGenerator(listGenerator, repeatedGenerator);
        final String[] possibleNames = { "Foo Test", "Foo TestTest", "Foo TestTestTest",
                                         "Bar Test", "Bar TestTest", "Bar TestTestTest" };
        assertThat(compoundGenerator.generate(LootMaterial.AXE, testHelper.uncommon), oneOf(possibleNames));
        assertThat(compoundGenerator.generate(LootMaterial.PICK, null), oneOf(possibleNames));
        assertThat(compoundGenerator.generate(null, testHelper.rare), oneOf(possibleNames));
        assertThat(compoundGenerator.generate(null, null), oneOf(possibleNames));

        final TransformationNameGenerator uppercaser = TransformationNameGenerator.uppercaser(new FixedListNameGenerator("hello", "world"));
        assertThat(uppercaser.generate(LootMaterial.AXE, testHelper.uncommon), oneOf("Hello", "World"));
        assertThat(uppercaser.generate(LootMaterial.PICK, null), oneOf("Hello", "World"));
        assertThat(uppercaser.generate(null, testHelper.rare), oneOf("Hello", "World"));
        assertThat(uppercaser.generate(null, null), oneOf("Hello", "World"));

        // Todo test parameters
    }

    @Test
    @DisplayName("Name generator compile correct")
    public void compileCorrect() {

        // todo test empty, null

        // todo test invalid

        // todo test repeats + material (and add to defaults??)

        final MaterialNameGenerator.FileBuilder materialBuilder = new MaterialNameGenerator.FileBuilder().defaultNameFiles();

        final Map<String, NameGenerator> variableMap = new HashMap<>();
        // todo fix for test environment
        final FixedListNameGenerator prefixPool = FixedListNameGenerator.fromNamesFile("src/main/resources/names/prefixes.txt");
        final FixedListNameGenerator suffixPool = FixedListNameGenerator.fromNamesFile("src/main/resources/names/suffixes.txt");
        final FixedListNameGenerator kanaPool = FixedListNameGenerator.fromNamesFile("src/main/resources/names/kana.txt");
        variableMap.put("prefix", prefixPool);
        variableMap.put("suffix", suffixPool);
        variableMap.put("kana", kanaPool);
        variableMap.put("item_name", materialBuilder.prefix("src/main/resources/names/").build());
        variableMap.put("fixed", materialBuilder.prefix("src/main/resources/names/fixed/").build());

        assertThat(NameGenerator.compile("foo", variableMap),
                is(new CompoundNameGenerator(
                        new FixedListNameGenerator("foo"))));

        assertThat(NameGenerator.compile("foo bar baz", variableMap),
                is(new CompoundNameGenerator(
                        new FixedListNameGenerator("foo"),
                        new FixedListNameGenerator("bar"),
                        new FixedListNameGenerator("baz"))));

        assertThat(NameGenerator.compile("[item_name]", variableMap),
                is(new CompoundNameGenerator(materialBuilder.prefix("src/main/resources/names/").build())));

        final NameGenerator prefixGenerator = new CompoundNameGenerator(
                prefixPool,
                materialBuilder.prefix("src/main/resources/names/").build()
        );
        assertThat(NameGenerator.compile("[prefix] [item_name]", variableMap), is(prefixGenerator));

        final NameGenerator suffixGenerator = new CompoundNameGenerator(
                materialBuilder.prefix("src/main/resources/names/").build(),
                new FixedListNameGenerator("of"),
                suffixPool
        );
        assertThat(NameGenerator.compile("[item_name] of [suffix]", variableMap), is(suffixGenerator));

        final NameGenerator prefixSuffixGenerator = new CompoundNameGenerator(
                prefixPool,
                materialBuilder.prefix("src/main/resources/names/").build(),
                new FixedListNameGenerator("de"),
                suffixPool
        );
        assertThat(NameGenerator.compile("[prefix] [item_name] de [suffix]", variableMap), is(prefixSuffixGenerator));

        final NameGenerator fixedNameGenerator = new CompoundNameGenerator(
                materialBuilder.prefix("src/main/resources/names/fixed/").build());
        assertThat(NameGenerator.compile("[fixed]", variableMap), is(fixedNameGenerator));

        final NameGenerator jpKanaNameGenerator = new CompoundNameGenerator(
                new RepeatedNameGenerator(kanaPool, 2, 5));
        assertThat(NameGenerator.compile("[kana](2-5)", variableMap), is(jpKanaNameGenerator));
    }

    @Test
    @DisplayName("Count number of names correct")
    public void permutationCountsCorrect() {
        final FixedListNameGenerator constantGenerator = new FixedListNameGenerator("Test");
        final FixedListNameGenerator listGenerator = new FixedListNameGenerator("Foo", "Bar");
        final RepeatedNameGenerator repeatedGenerator = new RepeatedNameGenerator(listGenerator, 1, 3);
        final CompoundNameGenerator compoundGenerator = new CompoundNameGenerator(listGenerator, constantGenerator, repeatedGenerator);

        final TransformationNameGenerator uppercaser = TransformationNameGenerator.uppercaser(new FixedListNameGenerator("hello", "world"));
        final TransformationNameGenerator compoundUppercaser = TransformationNameGenerator.uppercaser(compoundGenerator);

        assertThat(constantGenerator.countNumberOfNames(), is(1L));
        assertThat(listGenerator.countNumberOfNames(), is(2L));
        assertThat(repeatedGenerator.countNumberOfNames(), is(2 + 4 + 8L));
        assertThat(compoundGenerator.countNumberOfNames(), is(28L));
        assertThat(uppercaser.countNumberOfNames(), is(2L));
        assertThat(compoundUppercaser.countNumberOfNames(), is(compoundGenerator.countNumberOfNames()));

        AcuteLoot.nameGenChancePool.add(constantGenerator, 1);
        AcuteLoot.nameGenChancePool.add(listGenerator, 1);
        AcuteLoot.nameGenChancePool.add(repeatedGenerator, 1);
        AcuteLoot.nameGenChancePool.add(compoundGenerator, 1);
        assertThat(PermutationCounts.totalPermutations(), is(1 + 2 + 14 + 28L));
        AcuteLoot.nameGenChancePool.clear();
    }

}
