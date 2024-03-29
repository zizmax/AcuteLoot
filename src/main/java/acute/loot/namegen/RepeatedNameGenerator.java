package acute.loot.namegen;

import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * NameGenerator that invokes another NameGenerator a random number of times between
 * the specified minimum and maximum. The results are concatenated together and returned
 * as the generated name.
 */
@EqualsAndHashCode
public class RepeatedNameGenerator implements NameGenerator {

    private final NameGenerator baseGenerator;
    private final int minRepetitions;
    private final int maxRepetitions;

    private final @EqualsAndHashCode.Exclude Random random = new Random();

    /**
     * Construct a new RepeatedNameGenerator with the given base NameGenerator and repetition range.
     *
     * @param baseGenerator  the base NameGenerator, must not be null
     * @param minRepetitions the minimum number of repetitions, must be positive
     * @param maxRepetitions the maximum number of repetitions, must be positive and greater than minRepetitions
     */
    public RepeatedNameGenerator(NameGenerator baseGenerator, int minRepetitions, int maxRepetitions) {
        if (minRepetitions <= 0 || maxRepetitions <= 0) {
            throw new IllegalArgumentException("min and max repetitions must be positive");
        }
        if (minRepetitions >= maxRepetitions) {
            throw new IllegalArgumentException("Max repetitions must be greater than min repetitions");
        }
        this.baseGenerator = Objects.requireNonNull(baseGenerator);
        this.minRepetitions = minRepetitions;
        this.maxRepetitions = maxRepetitions;
    }

    @Override
    public String generate(final Map<String, String> parameters) {
        final int length = minRepetitions + random.nextInt((maxRepetitions + 1) - minRepetitions);
        return IntStream.range(0, length)
                        .mapToObj(i -> baseGenerator.generate(parameters))
                        .collect(Collectors.joining());
    }

    @Override
    public Optional<Long> countNumberOfNames() {
        final Optional<Long> baseNames = baseGenerator.countNumberOfNames();
        if (!baseNames.isPresent()) {
            return Optional.empty();
        }
        final long sum = IntStream.rangeClosed(minRepetitions, maxRepetitions)
                                  .mapToLong(i -> (long) Math.pow(baseNames.get(), i))
                                  .sum();
        return Optional.of(sum);
    }
}
