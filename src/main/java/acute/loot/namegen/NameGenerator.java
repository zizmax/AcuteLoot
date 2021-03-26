package acute.loot.namegen;

import acute.loot.LootMaterial;
import acute.loot.LootRarity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interface for generating names for loot.
 */
public interface NameGenerator {

    /**
     * Generate a name for an item with the given material and rarity. Implementations
     * need not use the material and rarity, and may optionally permit null values for
     * one or both parameters.
     *
     * @param lootMaterial the LootMaterial of the item to generate a name for
     * @param rarity the LootRarity of the item to generate a name for
     * @return a name for the item
     */
    String generate(LootMaterial lootMaterial, LootRarity rarity);

    /**
     * Return the number of different names the NameGenerator can produce. This may be
     * an estimate, or can return 0 to signal that returning a count is not supported.
     * @return the number of different names the NameGenerator can produce
     */
    long countNumberOfNames();

    /**
     * Utility method for reading a names file. Returns an empty list on
     * null input or if an IOException occurs while reading.
     *
     * @param file the names file
     * @return the lines of the file, or an empty list if it cannot be read
     */
    static List<String> readNames(String file) {
        if (file == null) {
            return Collections.emptyList();
        }

        try (Stream<String> stream = Files.lines(Paths.get(file))) {
            return stream.collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Compile an expression into a NameGenerator using the given variable map. This is a shorthand for
     *
     * <pre>
     * {@code
     * NameGenerator.compile(expression, variableMap, s -> {}).
     * }
     *</pre>
     *
     * @param expression the expression to compile
     * @param variableMap the variable map to use for compilation
     * @return a NameGenerator compiled from the expression and variable map
     */
    static NameGenerator compile(final String expression, final Map<String, NameGenerator> variableMap) {
        return compile(expression, variableMap, s -> {});
    }

    /**
     * Compile an expression into a NameGenerator using the given variable map. The expression is
     * first split into words which are then evaluated in one of three ways:
     *
     * <p>* if the word is of the form [variable_name], it is treated as a variable referencing the name
     * generator variable_name in the variable map, throwing an IllegalArgumentException if it does not
     * exist.
     *
     * <p>* if the word is of the form [variable_name](min-max), it is treated as a variable referencing the
     * name generator variable_name in the variable map, throwing an IllegalArgumentException if it does not exist.
     * the name generator referenced by the variable will be repeated between min and max times, where min and max
     * are integers. A NumberFormatException will be thrown if min or max cannot be parsed into integers.
     *
     * <p>* otherwise the word is treated as constant expression. If a constant expression looks like it may be intended
     * to be a variable or a repeated variable, but was not parsed as such, a warning message will be supplied
     * to the warningLogger.
     *
     * <p>This method requires all parameters to be non-null and will throw an IllegalArgumentException if
     * if the expression is all-whitespace. The result of this method is a name generator that will join
     * the result of each parsed word together with spaces.
     *
     * <p>Examples:
     *
     * <p>* "[foo]" parses to a NameGenerator that invokes the NameGenerator foo from the variable map
     *
     * <p>* "[foo] of [bar]" parses to a NameGenerator that produces names of the form "$foo of $bar" where
     * "$foo" and "$bar" are results of invoking the name generators foo and bar from the variable map.
     *
     * @param expression the expression to compile
     * @param variableMap the variable map to use for compilation
     * @param warningLogger a consumer of warning messages, may do nothing
     * @return a NameGenerator compiled from the expression and variable map
     */
    static NameGenerator compile(final String expression,
                                 final Map<String, NameGenerator> variableMap,
                                 final Consumer<String> warningLogger) {
        Objects.requireNonNull(expression);
        Objects.requireNonNull(variableMap);
        Objects.requireNonNull(warningLogger);
        if (expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression cannot be empty");
        }
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

        // Should never happen
        if (parts.isEmpty()) {
            throw new IllegalArgumentException("Pattern parsed empty");
        }
        return new CompoundNameGenerator(parts);
    }

}
