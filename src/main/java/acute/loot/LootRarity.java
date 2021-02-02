package acute.loot;

import acute.loot.namegen.MaterialNamePool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LootRarity {

    protected final String rarityColor;
    private final int id;
    private final String name;
    private final double effectChance;

    private final List<String> prefixNames;
    private final List<String> suffixNames;
    private final MaterialNamePool namePool;

    private static final Map<Integer, LootRarity> rarities = new HashMap<>();

    public LootRarity(int id, String name, double effectChance, String rarityColor) {
        this(id, name, effectChance, new MaterialNamePool.FileBuilder().defaultNameFiles().build(), rarityColor,
                MaterialNamePool.FileBuilder.DEFAULT_PREFIX_NAME_FILE, MaterialNamePool.FileBuilder.DEFAULT_SUFFIX_NAME_FILE);
    }

    public LootRarity(int id, String name, double effectChance, MaterialNamePool namePool, String rarityColor,
                      String prefixNameFile, String suffixNameFile) {
        this.id = id;
        this.name = name;
        this.rarityColor = rarityColor;
        if (effectChance < 0 || effectChance > 1) {
            throw new IllegalArgumentException("Effect chance must be in [0, 1]");
        }
        this.effectChance = effectChance;
        this.namePool = namePool;
        this.prefixNames = readNames(prefixNameFile);
        this.suffixNames = readNames(suffixNameFile);
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

    public List<String> namesForMaterial(LootMaterial lootMaterial) {
        return namePool.getNames(lootMaterial, this);
    }

    public List<String> getPrefixNames() {
        return prefixNames;
    }

    public List<String> getSuffixNames() {
        return suffixNames;
    }

    public String getRarityColor() {
        return rarityColor;
    }

    public static Map<Integer, LootRarity> getRarities() {
        return rarities;
    }

    private static List<String> readNames(String file) {
        try (Stream<String> stream = Files.lines(Paths.get(file))) {
            return stream.collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LootRarity that = (LootRarity) o;
        return id == that.id &&
                name.equals(that.name) &&
                namePool.equals(that.namePool) &&
                prefixNames.equals(that.prefixNames) &&
                suffixNames.equals(that.suffixNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, namePool, prefixNames, suffixNames);
    }

    @Override
    public String toString() {
        return "LootRarity: " + name;
    }
}
