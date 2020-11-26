package acute.loot;

import org.bukkit.ChatColor;
import org.bukkit.event.Event;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestLootItem {

    private static void registerAndAddRarityToPool(int id, String name, IntegerChancePool<LootRarity> pool, int chance) {
        final LootRarity rarity = new LootRarity(id, name, 0.5, ChatColor.DARK_GREEN.toString());
        LootRarity.registerRarity(rarity);
        pool.add(rarity, chance);
    }

    private static void registerAndAddEffectToPool(String name, int id, IntegerChancePool<LootSpecialEffect> pool, int chance) {
        final LootSpecialEffect effect = new MockSwordSpecialEffect(name, id);
        LootSpecialEffect.registerEffect(effect);
        pool.add(effect, chance);
    }

    public static void main(String[] args) {
        // Set up testing environment

        // Set up rarities (changes in id's must be updated further down as well)
        final IntegerChancePool<LootRarity> rarityChancePool = new IntegerChancePool<>();
        registerAndAddRarityToPool(-10, "Common", rarityChancePool, 64);
        registerAndAddRarityToPool(1, "Uncommon", rarityChancePool, 32);
        registerAndAddRarityToPool(25, "Rare", rarityChancePool, 16);
        registerAndAddRarityToPool(100, "Legendary", rarityChancePool, 8);
        registerAndAddRarityToPool(1000, "Mythical", rarityChancePool, 4);
        // Set up effects

        final IntegerChancePool<LootSpecialEffect> effectChancePool = new IntegerChancePool<>();
        registerAndAddEffectToPool("Heart gun", 0, effectChancePool, 1);
        registerAndAddEffectToPool("Particle trail", 1, effectChancePool, 2);
        registerAndAddEffectToPool("Water Splash", -3, effectChancePool, 2);
        registerAndAddEffectToPool("Flames", 18, effectChancePool, 3);

        // Set up generator

        LootItemGenerator generator = new LootItemGenerator(rarityChancePool, effectChancePool);

        // Run the tests

        // Edge cases

        generator.generate(0.0, LootMaterial.SWORD);
        generator.generate(1.0, LootMaterial.SWORD);
        generator.generate(0, LootMaterial.SWORD);
        generator.generate(1, LootMaterial.SWORD);

        System.out.print("\nTesting invalid LootCode version: ");
        try {
            new LootItem("#AL:foobar:1:-3:#");
            System.out.println("FAIL");
        } catch (IllegalArgumentException e) {
            System.out.println("OK");
        }

        // Test rarity distribution
        Map<Integer, Integer> counts = new HashMap<>();
        Map<Integer, Integer> effectCounts = new HashMap<>();

        final int trials = 1000000;
        for (int i = 0; i < trials; i++) {
            LootItem loot = generator.generate(LootMaterial.SWORD);
            int oldCount = counts.getOrDefault(loot.rarity().getId(), 0);
            int oldEffectCount = effectCounts.getOrDefault(loot.rarity().getId(), 0);
            counts.put(loot.rarityRaw(), oldCount + 1);
            if (!loot.getEffects().isEmpty()) {
                effectCounts.put(loot.rarityRaw(), oldEffectCount + 1);
            }
        }

        final Map<Integer, LootRarity> rarities = LootRarity.getRarities();
        final List<LootRarity> rarityList = rarities.values()
                                                    .stream()
                                                    .map(LootRarity::getId)
                                                    .sorted()
                                                    .map(rarities::get)
                                                    .collect(Collectors.toList());

        System.out.printf("\nRarity distribution (%d trials):\n", trials);
        for (int i = 0; i < rarities.size(); i++) {
            LootRarity rarity = rarityList.get(i);
            System.out.printf("%s: %d (expected: %f)\n", rarity.getName(), counts.get(rarity.getId()), trials * (Math.pow(2, 6 - i) / 124));
        }

        System.out.printf("\nEffect percentage (%d trials):\n", trials);
        for (LootRarity rarity : rarityList) {
            System.out.printf("%s: %f\n", rarity.getName(), (float) effectCounts.getOrDefault(rarity.getId(), 0) / counts
                    .get(rarity.getId()));
        }

        System.out.println("\nTesting lootcode:");
        final int tests = 1000000;
        final int dotInterval = 10000;
        for (int i = 0; i < tests; i++) {
            LootItem loot = generator.generate(LootMaterial.SWORD);
            LootItem rebuilt = new LootItem(loot.lootCode());
            if (!loot.equals(rebuilt)) {
                System.out.printf("ERROR: %s rebuilt incorrectly (new lootcode: %s)\n",
                        loot.lootCode(), rebuilt.lootCode());
                break;
            }

            if (i > 0 && i % dotInterval == 0) System.out.print('.');
            if (i == tests - 1) System.out.println("OK");
        }

        System.out.println("\nTests complete.");

    }

    private static class MockSwordSpecialEffect extends LootSpecialEffect {
        public MockSwordSpecialEffect(String name, int id) {
            super(name, "AL", id, Collections.singletonList(LootMaterial.SWORD), null);
        }

        @Override
        public void apply(Event event) {
        }
    }


}
