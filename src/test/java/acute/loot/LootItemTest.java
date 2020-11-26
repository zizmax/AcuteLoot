package acute.loot;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LootItemTest {

    private static TestHelper testHelper;
    private static LootItemWithCode liCommonNoEffects;
    private static LootItemWithCode liRareNoEffects;
    private static LootItemWithCode liUncommonOneEffect;
    private static LootItemWithCode liRareTwoEffects;

    @BeforeAll
    public static void setup() {
        testHelper = new TestHelper();
        testHelper.addTestResources();

        liCommonNoEffects = new LootItemWithCode(new LootItem(testHelper.common.getId(), Collections.emptyList()),
                                                 String.format("#AL:1.0:%d::#", testHelper.common.getId()));
        liRareNoEffects = new LootItemWithCode(new LootItem(testHelper.rare.getId(), Collections.emptyList()),
                                               String.format("#AL:1.0:%d::#", testHelper.rare.getId()));
        liUncommonOneEffect = new LootItemWithCode(new LootItem(testHelper.uncommon.getId(), Collections.singletonList(testHelper.effect1.getId())),
                                                   String.format("#AL:1.0:%d:%d:#", testHelper.uncommon.getId(), testHelper.effect1.getId()));
        liRareTwoEffects = new LootItemWithCode(new LootItem(testHelper.rare.getId(), Arrays.asList(testHelper.effect2.getId(), testHelper.effect3.getId())),
                                                String.format("#AL:1.0:%d:%d_%d:#", testHelper.rare.getId(), testHelper.effect2.getId(), testHelper.effect3.getId()));
    }

    @Test
    @DisplayName("V1 Loot Code can be constructed and reconstructed")
    public void testLootCodeV1() {
        assertThat(liCommonNoEffects.lootItem.lootCodeV1(), is(liCommonNoEffects.lootCode));
        assertThat(liUncommonOneEffect.lootItem.lootCodeV1(), is(liUncommonOneEffect.lootCode));
        assertThat(liRareNoEffects.lootItem.lootCodeV1(), is(liRareNoEffects.lootCode));
        assertThat(liRareTwoEffects.lootItem.lootCodeV1(), is(liRareTwoEffects.lootCode));
        assertThat(new LootItem(liCommonNoEffects.lootCode), is(liCommonNoEffects.lootItem));
        assertThat(new LootItem(liUncommonOneEffect.lootCode), is(liUncommonOneEffect.lootItem));
        assertThat(new LootItem(liRareNoEffects.lootCode), is(liRareNoEffects.lootItem));
        assertThat(new LootItem(liRareTwoEffects.lootCode), is(liRareTwoEffects.lootItem));
    }

    //public void testLootCodeV2();

    @Test
    @DisplayName("Invalid loot code throws IllegalArgumentException")
    public void testInvalidLootCode() {
        assertThrows(IllegalArgumentException.class, () -> new LootItem(""));
        assertThrows(IllegalArgumentException.class, () -> new LootItem("   "));
        assertThrows(IllegalArgumentException.class, () -> new LootItem("hello"));
        assertThrows(IllegalArgumentException.class, () -> new LootItem("hello, world!"));
        assertThrows(IllegalArgumentException.class, () -> new LootItem(null));
        assertThrows(IllegalArgumentException.class, () -> new LootItem("#AL:v0.0:1::#"));
    }

    @Test
    @DisplayName("LootItem can convert rarity and effect id's into objects")
    public void testIdConversion() {
        assertThat(liCommonNoEffects.lootItem.rarity(), is(testHelper.common));
        assertThat(liUncommonOneEffect.lootItem.rarity(), is(testHelper.uncommon));
        assertThat(liUncommonOneEffect.lootItem.getEffects(), containsInAnyOrder(testHelper.effect1));
        assertThat(liRareNoEffects.lootItem.getEffects(), empty());
        assertThat(liRareTwoEffects.lootItem.getEffects(), containsInAnyOrder(testHelper.effect2, testHelper.effect3));
    }

    /**
     * This is a randomized version of the other LootCode tests
     */
    @Test
    @DisplayName("LootItem can be reconstructed from LootCode")
    public void testLootCodeReconstruction() {
        final Random random = new Random(0);
        for (int i = 0; i < 100; i++) {
            final int rarity = testHelper.rarities.get(random.nextInt(testHelper.rarities.size())).getId();
            final List<Integer> effects = new ArrayList<>();
            for (int j = 0; j < random.nextInt(5); i++) {
                effects.add(testHelper.effects.get(random.nextInt(testHelper.rarities.size())).getId());
            }
            LootItem lootItem = new LootItem(rarity, effects);

            assertThat(new LootItem(lootItem.lootCode()), is(lootItem));
        }
    }

    private static class LootItemWithCode {
        final LootItem lootItem;
        final String lootCode;

        public LootItemWithCode(LootItem lootItem, String lootCode) {
            this.lootItem = lootItem;
            this.lootCode = lootCode;
        }
    }
}
