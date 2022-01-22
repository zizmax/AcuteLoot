package acute.loot.namegen;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.Map;
import java.util.Optional;

/**
 * Name generator that delegates based on a parameter key.
 */
@AllArgsConstructor
@EqualsAndHashCode
public class MappedNameGenerator implements NameGenerator {

    private final @NonNull String parameterKey;
    private final @NonNull Map<String, NameGenerator> mapping;

    @Override
    public String generate(final Map<String, String> parameters) {
        return mapping.get(parameters.get(parameterKey)).generate(parameters);
    }

    @Override
    public Optional<Long> countNumberOfNames() {
        final long sum = mapping.values()
                                .stream()
                                .flatMapToLong(acute.loot.namegen.Util::nameCount)
                                .sum();
        return Optional.of(sum);
    }
}
