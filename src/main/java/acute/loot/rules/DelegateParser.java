package acute.loot.rules;

import acute.loot.generator.LootItemGenerator;
import com.github.phillip.h.acutelib.util.Pair;
import lombok.AllArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
class DelegateParser implements RuleParser, RuleParser.SubRuleParser {

    private final LootItemGenerator lootItemGenerator;
    private final Map<String, SubRuleParser> delegates;

    @Override
    public List<Rule> parse(final ConfigurationSection config) {
        final Generator generator = parseGenerator(config);
        return parseSub(config).stream()
                .map(s -> new Rule(s.left(), generator, s.right()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Pair<Condition, Spawner>> parseSub(ConfigurationSection config) {
        final List<String> candidates = config.getKeys(false).stream().filter(delegates::containsKey).collect(Collectors.toList());
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("Rule missing condition (or condition unknown)");
        } else if (candidates.size() > 1) {
            throw new IllegalArgumentException("Ambiguous condition, candidates are: " + String.join(", ", candidates));
        }

        final String condition = candidates.get(0);
        return delegates.get(condition).parseSub(config.getConfigurationSection(condition));
    }

    private Generator parseGenerator(final ConfigurationSection config) {
        return Generator.withChance(config.getDouble("generator.chance") / 100, lootItemGenerator);
    }
}
