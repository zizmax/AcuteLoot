package acute.loot.namegen;

import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * NameGenerator that wraps another NameGenerator and applies a "transformation"
 * to its output. A transformation can be any function String {@literal ->} String, typically
 * a small change such as adjusting capitalization.
 */
@AllArgsConstructor
public class TransformationNameGenerator implements NameGenerator {

    private final @NonNull NameGenerator baseGenerator;
    private final @NonNull Function<String, String> transform;

    @Override
    public String generate(final Map<String, String> parameters) {
        return transform.apply(baseGenerator.generate(parameters));
    }

    @Override
    public Optional<Long> countNumberOfNames() {
        return baseGenerator.countNumberOfNames();
    }

    /**
     * Wrap the given NameGenerator in a TransformationNameGenerator that
     * ensures the first character in the generated name is uppercase.
     *
     * @param baseGenerator the base NameGenerator to wrap
     * @return a TransformationNameGenerator that ensures the first character of the name is uppercase
     */
    public static TransformationNameGenerator uppercaser(final NameGenerator baseGenerator) {
        return new TransformationNameGenerator(baseGenerator,
                                               s -> s.substring(0, 1).toUpperCase() + s.substring(1));
    }
}
