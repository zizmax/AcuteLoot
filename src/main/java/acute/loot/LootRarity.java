package acute.loot;

import java.util.*;

public class LootRarity {

    protected final String rarityColor;
    private final int id;
    private final String name;
    private final double effectChance;

    private static final Map<Integer, LootRarity> rarities = new HashMap<>();

    public LootRarity(int id, String name, double effectChance, String rarityColor) {
        this.id = id;
        this.name = name;
        this.rarityColor = rarityColor;
        if (effectChance < 0 || effectChance > 1) {
            throw new IllegalArgumentException("Effect chance must be in [0, 1]");
        }
        this.effectChance = effectChance;
    }

    public static void registerRarity(final LootRarity rarity) {
        if (rarities.containsKey(rarity.getId())) {
            throw new IllegalArgumentException("Rarity with id '" + rarity.getId() + "' already registered.");
        }
        rarities.put(rarity.getId(), rarity);
    }

    public static LootRarity get(int id) {
        return rarities.get(id);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getEffectChance() {
        return effectChance;
    }

    public String getRarityColor() {
        return rarityColor;
    }

    public static Map<Integer, LootRarity> getRarities() {
        return rarities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LootRarity that = (LootRarity) o;
        return id == that.id &&
                Double.compare(that.effectChance, effectChance) == 0 &&
                Objects.equals(rarityColor, that.rarityColor) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rarityColor, id, name, effectChance);
    }

    @Override
    public String toString() {
        return "LootRarity: " + name;
    }
}
