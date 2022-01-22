package acute.loot.namegen;

import lombok.EqualsAndHashCode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * NameGenerator that invokes one or more other NameGenerators,
 * joining the results with a possibly-empty joining string.
 */
@EqualsAndHashCode
public class CompoundNameGenerator implements NameGenerator {

    private final List<NameGenerator> parts;
    private final String joiningString;

    /**
     * Construct a new CompoundNameGenerator with the given NameGenerator
     * parts and " " as the joining string.
     *
     * @param parts the NameGenerator parts, must be non-null and non-empty
     */
    public CompoundNameGenerator(NameGenerator... parts) {
        this(Arrays.asList(parts));
    }

    /**
     * Construct a new CompoundNameGenerator with the given NameGenerator
     * parts and " " as the joining string.
     *
     * @param parts the NameGenerator parts, must be non-null and non-empty
     */
    public CompoundNameGenerator(List<NameGenerator> parts) {
        this(parts, " ");
    }

    /**
     * Construct a new CompoundNameGenerator with the given NameGenerator
     * parts and joining string.
     *
     * @param parts         the NameGenerator parts, must be non-null and non-empty
     * @param joiningString the joining string, must be non-null
     */
    public CompoundNameGenerator(List<NameGenerator> parts, String joiningString) {
        if (parts.isEmpty()) {
            throw new IllegalArgumentException("At least one name generator required");
        }
        this.parts = parts;
        this.joiningString = Objects.requireNonNull(joiningString);
    }

    @Override
    public String generate(final Map<String, String> parameters) {
        return parts.stream()
                    .map(part -> part.generate(parameters))
                    .collect(Collectors.joining(joiningString));
    }

    @Override
    public Optional<Long> countNumberOfNames() {
        final long product = parts.stream()
                                  .flatMapToLong(acute.loot.namegen.Util::nameCount)
                                  .reduce(1, (a, b) -> a * b);
        return Optional.of(product);
    }
}
