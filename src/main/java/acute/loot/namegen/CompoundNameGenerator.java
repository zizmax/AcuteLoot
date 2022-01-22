package acute.loot.namegen;

import com.github.phillip.h.acutelib.util.Checks;
import com.github.phillip.h.acutelib.util.Util;
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
        this.parts = Checks.requireNonEmpty(parts);
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
                                  .map(NameGenerator::countNumberOfNames)
                                  .flatMap(Util::stream)
                                  .mapToLong(x -> x)
                                  .reduce(1, (a, b) -> a * b);
        return Optional.of(product);
    }
}
