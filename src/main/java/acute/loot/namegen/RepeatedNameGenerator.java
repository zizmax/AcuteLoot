package acute.loot.namegen;

import acute.loot.AcuteLoot;
import acute.loot.LootMaterial;
import acute.loot.LootRarity;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RepeatedNameGenerator implements NameGenerator {

    private final NameGenerator baseGenerator;
    private final int minRepetitions;
    private final int maxRepetitions;

    public RepeatedNameGenerator(NameGenerator baseGenerator, int minRepetitions, int maxRepetitions) {
        this.baseGenerator = baseGenerator;
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
