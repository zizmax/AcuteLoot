package acute.loot.namegen;

import base.collections.IntegerChancePool;

/**
 * Utility class for computing permutation statistics.
 */
public final class PermutationCounts {

    private PermutationCounts() {
    }

    public static long totalPermutations(final IntegerChancePool<NameGenerator> namePool) {
        return namePool.values().stream().mapToLong(NameGenerator::countNumberOfNames).sum();
    }

    /**
     * Try to find, with tolerance epsilon, the number of draws needed to have a `targetChance`
     * chance of having a duplicate when drawing from a uniform distribution on `total` objects
     */
    public static long birthdayProblem(long total, double targetChance, double epsilon) {
        long x = total / 2;
        long lower = 0;
        long upper = total;
        long iterations = 0;
        while (x > 0 && iterations < 64) {
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
            iterations++;
        }

        return -1;
    }

    // This will estimate the chance that `count` draws of a uniform distribution
    // on `total` objects will contain a duplicate
    public static double birthdayPercent(long count, long total) {
        return 1.0 - Math.exp(((double) -count) * (count - 1) / total / 2);
    }

}
