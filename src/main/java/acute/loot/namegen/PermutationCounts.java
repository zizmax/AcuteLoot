package acute.loot.namegen;

import acute.loot.AcuteLoot;
import acute.loot.LootMaterial;
import acute.loot.LootRarity;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class PermutationCounts {

    private PermutationCounts() {
    }

    public static long totalPermutations(boolean kanaEnabled) {
        long count = Arrays.stream(LootMaterial.values())
                           .mapToLong(PermutationCounts::prefixSuffixPermutations)
                           .sum();
        count += fixedPermutations();
        if (kanaEnabled) count += jpKanaPermutation();
        return count;
    }

    public static long jpKanaPermutation() {
        return JPKanaNameGenerator.jpKanaNameGenerator.numberOfNames();
    }

    public static long fixedPermutations() {
        return FixedNameGenerator.defaultGenerator().numberOfNames();
    }

    public static long prefixSuffixPermutations(LootMaterial material) {
        return prefixSuffixPermutations(material, AcuteLoot.rarityChancePool.values());
    }

    public static long prefixSuffixPermutations(LootMaterial material, List<LootRarity> rarities) {
        if (material == LootMaterial.UNKNOWN) return 0;

        final long prefixes = rarities.stream()
                                      .flatMap(r -> r.getPrefixNames().stream())
                                      .collect(Collectors.toSet())
                                      .size();

        final long suffixes = rarities.stream()
                                      .flatMap(r -> r.getSuffixNames().stream())
                                      .collect(Collectors.toSet())
                                      .size();

        final long names = rarities.stream()
                                   .flatMap(r -> r.namesForMaterial(material).stream())
                                   .collect(Collectors.toSet())
                                   .size();

        final long prefixCount = names * prefixes;
        final long suffixCount = names * suffixes;
        final long prefixSuffixCount = names * prefixes * suffixes;
        if(AcuteLoot.debug) {
            Bukkit.getServer().getLogger().info("Prefixes: " + prefixes);
            Bukkit.getServer().getLogger().info("Suffixes: " + suffixes);
            Bukkit.getServer().getLogger().info("Names: " + names);
        }
        return prefixCount + suffixCount + prefixSuffixCount;
    }

    // Try to find, with tolerance epsilon, the number of draws needed to have a `targetChance`
    // chance of having a duplicate when drawing from a uniform distribution on `total` objects
    public static long birthdayProblem(long total, double targetChance, double epsilon) {
        long x = total / 2;
        long lower = 0;
        long upper = total;
        long interations = 0;
        while (x > 0 && interations < 64) {
            final double chance = birthdayPercent(x, total);
            if (Math.abs(chance - targetChance) < epsilon) {
                return x;
            } else if (chance < targetChance) {
                lower = x;
                x = (upper + x) / 2;
            } else if (chance > targetChance) {
                upper = x;
                x = (x + lower) / 2;
            }
            interations++;
        }

        return -1;
    }

    // This will estimate the chance that `count` draws of a uniform distribution
    // on `total` objects will contain a duplicate
    public static double birthdayPercent(long count, long total) {
        return 1.0 - Math.exp(((double) -count) * (count - 1) / total / 2);
    }

}
