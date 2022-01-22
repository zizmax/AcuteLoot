package acute.loot;

import acute.loot.namegen.*;
import com.github.phillip.h.acutelib.collections.IntegerChancePool;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        assertThat(constantGenerator.generate(params(LootMaterial.AXE, testHelper.uncommon)), is("Test"));
        assertThat(constantGenerator.generate(params(LootMaterial.PICK, null)), is("Test"));
        assertThat(constantGenerator.generate(params(null, testHelper.rare)), is("Test"));
        assertThat(constantGenerator.generate(params(null, null)), is("Test"));

        assertThat(listGenerator.generate(params(LootMaterial.AXE, testHelper.uncommon)), oneOf("Foo", "Bar"));
        assertThat(listGenerator.generate(params(LootMaterial.PICK, null)), oneOf("Foo", "Bar"));
        assertThat(listGenerator.generate(params(null, testHelper.rare)), oneOf("Foo", "Bar"));
        assertThat(listGenerator.generate(params(null, null)), oneOf("Foo", "Bar"));

        final RepeatedNameGenerator repeatedGenerator = new RepeatedNameGenerator(constantGenerator, 1, 3);
        assertThat(repeatedGenerator.generate(params(LootMaterial.AXE, testHelper.uncommon)), oneOf("Test", "TestTest", "TestTestTest"));
        assertThat(repeatedGenerator.generate(params(LootMaterial.PICK, null)), oneOf("Test", "TestTest", "TestTestTest"));
        assertThat(repeatedGenerator.generate(params(null, testHelper.rare)), oneOf("Test", "TestTest", "TestTestTest"));
        assertThat(repeatedGenerator.generate(params(null, null)), oneOf("Test", "TestTest", "TestTestTest"));

        final CompoundNameGenerator compoundGenerator = new CompoundNameGenerator(listGenerator, repeatedGenerator);
        final String[] possibleNames = { "Foo Test", "Foo TestTest", "Foo TestTestTest",
                                         "Bar Test", "Bar TestTest", "Bar TestTestTest" };
        assertThat(compoundGenerator.generate(params(LootMaterial.AXE, testHelper.uncommon)), oneOf(possibleNames));
        assertThat(compoundGenerator.generate(params(LootMaterial.PICK, null)), oneOf(possibleNames));
        assertThat(compoundGenerator.generate(params(null, testHelper.rare)), oneOf(possibleNames));
        assertThat(compoundGenerator.generate(params(null, null)), oneOf(possibleNames));

        final TransformationNameGenerator uppercaser = TransformationNameGenerator.uppercaser(new FixedListNameGenerator("hello", "world"));
        assertThat(uppercaser.generate(params(LootMaterial.AXE, testHelper.uncommon)), oneOf("Hello", "World"));
        assertThat(uppercaser.generate(params(LootMaterial.PICK, null)), oneOf("Hello", "World"));
        assertThat(uppercaser.generate(params(null, testHelper.rare)), oneOf("Hello", "World"));
        assertThat(uppercaser.generate(params(null, null)), oneOf("Hello", "World"));
    }

    @Test
    @DisplayName("Name generator compile correct")
    public void compileCorrect() {

        final MaterialNameGenBuilder materialBuilder = new MaterialNameGenBuilder().defaultNameFiles();

        final Map<String, NameGenerator> variableMap = new HashMap<>();
        final FixedListNameGenerator prefixPool = FixedListNameGenerator.fromNamesFile("src/main/resources/names/prefixes.txt");
        final FixedListNameGenerator suffixPool = FixedListNameGenerator.fromNamesFile("src/main/resources/names/suffixes.txt");
        final FixedListNameGenerator kanaPool = FixedListNameGenerator.fromNamesFile("src/main/resources/names/kana.txt");
        variableMap.put("prefix", prefixPool);
        variableMap.put("suffix", suffixPool);
        variableMap.put("kana", kanaPool);
        variableMap.put("ka-na-", kanaPool);
        variableMap.put("ka[na])](", kanaPool);
        variableMap.put("item_name", materialBuilder.prefix("src/main/resources/names/").build());
        variableMap.put("fixed", materialBuilder.prefix("src/main/resources/names/fixed/").build());

        assertThrows(NullPointerException.class, () -> NameGenerator.compile(null, variableMap));
        assertThrows(NullPointerException.class, () -> NameGenerator.compile("foo", null));
        assertThrows(NullPointerException.class, () -> NameGenerator.compile("foo", variableMap, null));
        assertThrows(IllegalArgumentException.class, () -> NameGenerator.compile("", variableMap));
        assertThrows(IllegalArgumentException.class, () -> NameGenerator.compile("      ", variableMap));

        assertThrows(IllegalArgumentException.class, () -> NameGenerator.compile("[foo]", variableMap));
        assertThrows(IllegalArgumentException.class, () -> NameGenerator.compile("[bar](1-2)", variableMap));
        assertThrows(IllegalArgumentException.class, () -> NameGenerator.compile("[kana](0-1)", variableMap));
        assertThrows(IllegalArgumentException.class, () -> NameGenerator.compile("[kana](-1-2)", variableMap));
        assertDoesNotThrow(() -> NameGenerator.compile("[ka-na-](1-2)", variableMap));
        assertDoesNotThrow(() -> NameGenerator.compile("[ka[na])](]", variableMap));
        assertDoesNotThrow(() -> NameGenerator.compile("[ka[na])](](1-2)", variableMap));
        assertThrows(IllegalArgumentException.class, () -> NameGenerator.compile("[kana](4-2)", variableMap));
        assertThrows(NumberFormatException.class, () -> NameGenerator.compile("[kana](a-2)", variableMap));
        assertThrows(NumberFormatException.class, () -> NameGenerator.compile("[kana](1-)", variableMap));
        assertDoesNotThrow(() -> NameGenerator.compile("[kana]()", variableMap));
        assertThrows(NumberFormatException.class, () -> NameGenerator.compile("[kana](-)", variableMap));

        @SuppressWarnings("unchecked") final Consumer<String> warning = Mockito.mock(Consumer.class);

        NameGenerator.compile("[foo", variableMap, warning);
        Mockito.verify(warning, Mockito.times(1)).accept(Mockito.anyString());

        NameGenerator.compile("foo]", variableMap, warning);
        Mockito.verify(warning, Mockito.times(2)).accept(Mockito.anyString());

        NameGenerator.compile("[foo](1-2", variableMap, warning);
        Mockito.verify(warning, Mockito.times(3)).accept(Mockito.anyString());

        NameGenerator.compile("[foo(1-2)", variableMap, warning);
        Mockito.verify(warning, Mockito.times(4)).accept(Mockito.anyString());

        NameGenerator.compile("[kana](1-2)", variableMap, warning);
        Mockito.verify(warning, Mockito.times(4)).accept(Mockito.anyString());

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

        assertThat(constantGenerator.countNumberOfNames(), is(Optional.of(1L)));
        assertThat(listGenerator.countNumberOfNames(), is(Optional.of(2L)));
        assertThat(repeatedGenerator.countNumberOfNames(), is(Optional.of(2 + 4 + 8L)));
        assertThat(compoundGenerator.countNumberOfNames(), is(Optional.of(28L)));
        assertThat(uppercaser.countNumberOfNames(), is(Optional.of(2L)));
        assertThat(compoundUppercaser.countNumberOfNames(), is(compoundGenerator.countNumberOfNames()));

        final IntegerChancePool<NameGenerator> namePool = new IntegerChancePool<>();
        namePool.add(constantGenerator, 1);
        namePool.add(listGenerator, 1);
        namePool.add(repeatedGenerator, 1);
        namePool.add(compoundGenerator, 1);
        assertThat(PermutationCounts.totalPermutations(namePool), is(1 + 2 + 14 + 28L));
    }

    private static Map<String, String> params(final LootMaterial lootMaterial, final LootRarity lootRarity) {
        final Map<String, String> mapping = new HashMap<>();
        mapping.put("lootMaterial", lootMaterial == null ? null : lootMaterial.name());
        mapping.put("lootRarity", lootRarity == null ? null : lootRarity.getName());
        return mapping;
    }

}
