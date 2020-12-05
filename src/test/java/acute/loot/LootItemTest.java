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
    private static LootItemWithCode liCommonNsEffect;
    private static LootItemWithCode liRareDifferentNsEffects;

    @BeforeAll
    public static void setup() {
        testHelper = new TestHelper();
        testHelper.addTestResources();

        liCommonNoEffects = new LootItemWithCode(new LootItem(testHelper.common.getId(), Collections.emptyList()),
                                                 String.format("#AL:1.0:%d::#", testHelper.common.getId()));
        liRareNoEffects = new LootItemWithCode(new LootItem(testHelper.rare.getId(), Collections.emptyList()),
                                               String.format("#AL:1.0:%d::#", testHelper.rare.getId()));
        liUncommonOneEffect = new LootItemWithCode(new LootItem(testHelper.uncommon.getId(), Collections.singletonList(testHelper.effect1.effectId())),
                                                   String.format("#AL:1.0:%d:%d:#", testHelper.uncommon.getId(), testHelper.effect1.id()));
        liRareTwoEffects = new LootItemWithCode(new LootItem(testHelper.rare.getId(), Arrays.asList(testHelper.effect2.effectId(), testHelper.effect3.effectId())),
                                                String.format("#AL:1.0:%d:%d_%d:#", testHelper.rare.getId(), testHelper.effect2.id(), testHelper.effect3.id()));

        liCommonNsEffect = new LootItemWithCode(new LootItem(testHelper.common.getId(), Collections.singletonList(testHelper.ns_effect1.effectId())),
                                                String.format("#AL:2.0:%d:%s;%d:#", testHelper.common.getId(), testHelper.ns_effect1.ns(), testHelper.ns_effect1.id()));

        liRareDifferentNsEffects = new LootItemWithCode(new LootItem(testHelper.rare.getId(), Arrays.asList(testHelper.ns_effect2.effectId(),
                                                                                   testHelper.ns_effect3.effectId(),
                                                                                   testHelper.effect1.effectId())
        ),
                                                        String.format("#AL:2.0:%d:%s;%d_%s;%d_%s;%d:#",
                                                                      testHelper.rare.getId(),
                                                                      testHelper.ns_effect2.ns(),
                                                                      testHelper.ns_effect2.id(),
                                                                      testHelper.ns_effect3.ns(),
                                                                      testHelper.ns_effect3.id(),
                                                                      testHelper.effect1.ns(),
                                                                      testHelper.effect1.id()));
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

        assertThrows(IllegalStateException.class, () -> liCommonNsEffect.lootItem.lootCodeV1());
        assertThrows(IllegalStateException.class, () -> liRareDifferentNsEffects.lootItem.lootCodeV1());
    }

    @Test
    @DisplayName("V2 Loot Code can be constructed and reconstructed")
    public void testLootCodeV2() {
        // Cannot do this test with items with a V1 loot code (because the V2 loot code will clearly be different)
        assertThat(liCommonNsEffect.lootItem.lootCodeV2(), is(liCommonNsEffect.lootCode));
        assertThat(liRareDifferentNsEffects.lootItem.lootCodeV2(), is(liRareDifferentNsEffects.lootCode));

        assertThat(new LootItem(liCommonNoEffects.lootItem.lootCodeV2()), is(liCommonNoEffects.lootItem));
        assertThat(new LootItem(liUncommonOneEffect.lootItem.lootCodeV2()), is(liUncommonOneEffect.lootItem));
        assertThat(new LootItem(liRareNoEffects.lootItem.lootCodeV2()), is(liRareNoEffects.lootItem));
        assertThat(new LootItem(liRareTwoEffects.lootItem.lootCodeV2()), is(liRareTwoEffects.lootItem));
        assertThat(new LootItem(liCommonNsEffect.lootCode), is(liCommonNsEffect.lootItem));
        assertThat(new LootItem(liRareDifferentNsEffects.lootCode), is(liRareDifferentNsEffects.lootItem));
    }

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
            final List<EffectId> effects = new ArrayList<>();
            for (int j = 0; j < random.nextInt(5); i++) {
                effects.add(testHelper.effects.get(random.nextInt(testHelper.rarities.size())).effectId());
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
