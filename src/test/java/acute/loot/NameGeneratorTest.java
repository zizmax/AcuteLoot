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

    // todo test permutation counts

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
        variableMap.put("prefix", FixedListNameGenerator.defaultPrefixPool());
        variableMap.put("suffix", FixedListNameGenerator.defaultSuffixPool());
        variableMap.put("kana", FixedListNameGenerator.kanaPool());
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
                FixedListNameGenerator.defaultPrefixPool(),
                materialBuilder.prefix("src/main/resources/names/").build()
        );
        assertThat(NameGenerator.compile("[prefix] [item_name]", variableMap), is(prefixGenerator));

        final NameGenerator suffixGenerator = new CompoundNameGenerator(
                materialBuilder.prefix("src/main/resources/names/").build(),
                new FixedListNameGenerator("of"),
                FixedListNameGenerator.defaultSuffixPool()
        );
        assertThat(NameGenerator.compile("[item_name] of [suffix]", variableMap), is(suffixGenerator));

        final NameGenerator prefixSuffixGenerator = new CompoundNameGenerator(
                FixedListNameGenerator.defaultPrefixPool(),
                materialBuilder.prefix("src/main/resources/names/").build(),
                new FixedListNameGenerator("de"),
                FixedListNameGenerator.defaultSuffixPool()
        );
        assertThat(NameGenerator.compile("[prefix] [item_name] de [suffix]", variableMap), is(prefixSuffixGenerator));

        final NameGenerator fixedNameGenerator = new CompoundNameGenerator(
                materialBuilder.prefix("src/main/resources/names/fixed/").build());
        assertThat(NameGenerator.compile("[fixed]", variableMap), is(fixedNameGenerator));

        final NameGenerator jpKanaNameGenerator = new CompoundNameGenerator(
                new RepeatedNameGenerator(FixedListNameGenerator.kanaPool(), 2, 5));
        assertThat(NameGenerator.compile("[kana](2-5)", variableMap), is(jpKanaNameGenerator));
    }

}
