package acute.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public class LootItemGenerator {

    private static final Random random = AcuteLoot.random;

    private final IntegerChancePool<LootRarity> rarityPool;
    private final IntegerChancePool<LootSpecialEffect> effectPool;

    public LootItemGenerator(IntegerChancePool<LootRarity> rarityPool, IntegerChancePool<LootSpecialEffect> effectPool) {
        this.rarityPool = rarityPool;
        this.effectPool = effectPool;
    }

    public LootItem generate(LootMaterial material) {
        return generate(random.nextDouble(), material);
    }

    /**
     * Generate a loot item using the given rarity and material.
     *
     * @param rarity Rarity of the item generated. Higher is rarer. Must be in [0, 1]
     * @param material Material of the item generated.
     * @return A randomly generated LootItem
     */
    public LootItem generate(double rarity, LootMaterial material) {
        if (rarity < 0 || rarity > 1) throw new IllegalArgumentException("Rarity must be in [0, 1]");
        final LootRarity lootRarity = rarityPool.draw(rarity);
        return generateWithRarity(lootRarity, material);
    }

    /**
     * Generate a loot item with the provided rarity and material.
     * @param lootRarity Rarity of the item generated. Higher is rarer. Must be in [0, 1]
     * @param material Material of the item generated.
     * @return A randomly generated LootItem with the provided rarity
     */
    public LootItem generateWithRarity(LootRarity lootRarity, LootMaterial material) {
        int itemRarity = lootRarity.getId();

        List<EffectId> effects = new ArrayList<>();
        try {
            if (random.nextDouble() <= lootRarity.getEffectChance()) {
                final LootSpecialEffect effect = effectPool.drawWithPredicate(l -> l.getValidMaterials().contains(material));
                effects.add(effect.effectId());
            }
        } catch (NoSuchElementException e) {
            // No effects for this material, ignore.
        }

        return new LootItem(itemRarity, effects);
    }
}
