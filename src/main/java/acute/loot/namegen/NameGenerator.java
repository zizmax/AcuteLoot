package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface NameGenerator {

    String generate(LootMaterial lootMaterial, LootRarity rarity);

    long countNumberOfNames();

    static List<String> readNames(String file) {
        if (file == null) return Collections.emptyList();

        try (Stream<String> stream = Files.lines(Paths.get(file))) {
            return stream.collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    static NameGenerator compile(final String expression, final Map<String, NameGenerator> variableMap) {
        return compile(expression, variableMap, s -> {});
    }

    static NameGenerator compile(final String expression,
                                 final Map<String, NameGenerator> variableMap,
                                 final Consumer<String> warningLogger) {
        Objects.requireNonNull(expression);
        Objects.requireNonNull(variableMap);
        Objects.requireNonNull(warningLogger);
        if (expression.trim().isEmpty()) throw new IllegalArgumentException("Expression cannot be empty");
        final String[] words = expression.split(" ");

        final List<NameGenerator> parts = new ArrayList<>();
        for (String word : words) {
            if (word.startsWith("[") && word.endsWith("]")) {
                final String variableName = word.substring(1, word.length() - 1);
                if (!variableMap.containsKey(variableName)) {
                    throw new IllegalArgumentException(String.format("Unknown name pool '%s'", variableName));
                }
                parts.add(variableMap.get(variableName));
            } else if (word.startsWith("[") && word.endsWith(")") && word.contains("](") && word.contains("-")) {
                final String variableName = word.substring(1, word.lastIndexOf("]("));
                if (!variableMap.containsKey(variableName)) {
                    throw new IllegalArgumentException(String.format("Unknown name pool '%s'", variableName));
                }

                final String range = word.substring(word.lastIndexOf("](") + 2, word.length() - 1);
                final String min = range.substring(0, range.indexOf('-'));
                final String max = range.substring(range.indexOf('-') + 1);
                parts.add(new RepeatedNameGenerator(variableMap.get(variableName), Integer.parseInt(min), Integer.parseInt(max)));
            } else {
                if (word.contains("[") || word.contains("]") || word.contains("(") || word.contains(")")) {
                    final String warningMessage = "Warning, the token '" + word + "' appears like it may be " +
                            "a malformed variable.\nIf you intended this token to reference a " +
                            "name pool, please correct the name generator template.";
                    warningLogger.accept(warningMessage);
                }
                parts.add(new FixedListNameGenerator(word));
            }
        }

        if (parts.isEmpty()) {
            throw new IllegalArgumentException("Pattern parsed empty");
        }
        return new CompoundNameGenerator(parts);
    }

}
