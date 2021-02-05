package acute.loot.namegen;

import acute.loot.AcuteLoot;
import acute.loot.LootMaterial;
import acute.loot.LootRarity;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * NameGenerator that invokes another NameGenerator a random number of times between
 * the specified minimum and maximum. The results are concatenated together and returned
 * as the generated name.
 */
public class RepeatedNameGenerator implements NameGenerator {

    private final NameGenerator baseGenerator;
    private final int minRepetitions;
    private final int maxRepetitions;

    /**
     * Construct a new RepeatedNameGenerator with the given base NameGenerator and repetition range.
     * @param baseGenerator the base NameGenerator, must not be null
     * @param minRepetitions the minimum number of repetitions, must be positive
     * @param maxRepetitions the maximum number of repetitions, must be positive and greater than minRepetitions
     */
    public RepeatedNameGenerator(NameGenerator baseGenerator, int minRepetitions, int maxRepetitions) {
        if (minRepetitions <= 0 || maxRepetitions <= 0) throw new IllegalArgumentException("min and max repetitions must be positive");
        if (minRepetitions >= maxRepetitions) throw new IllegalArgumentException("Max repetitions must be greater than min repetitions");
        this.baseGenerator = Objects.requireNonNull(baseGenerator);
        this.minRepetitions = minRepetitions;
        this.maxRepetitions = maxRepetitions;
    }

    @Override
    public String generate(LootMaterial lootMaterial, LootRarity rarity) {
        final int length = minRepetitions + AcuteLoot.random.nextInt((maxRepetitions + 1) - minRepetitions);
        return IntStream.range(0, length)
                        .mapToObj(i -> baseGenerator.generate(lootMaterial, rarity))
                        .collect(Collectors.joining());
    }

    @Override
    public long countNumberOfNames() {
        return IntStream.rangeClosed(minRepetitions, maxRepetitions)
                        .mapToLong(i -> (long) Math.pow(baseGenerator.countNumberOfNames(), i))
                        .sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepeatedNameGenerator that = (RepeatedNameGenerator) o;
        return minRepetitions == that.minRepetitions &&
                maxRepetitions == that.maxRepetitions &&
                Objects.equals(baseGenerator, that.baseGenerator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseGenerator, minRepetitions, maxRepetitions);
    }
}
