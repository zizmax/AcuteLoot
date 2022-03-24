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
        final String condition = Objects.requireNonNull(config.getString("condition"), "Rule missing condition");
        final ConfigurationSection section = Objects.requireNonNull(config.getConfigurationSection(condition), "Condition configuration missing");
        return Optional.ofNullable(delegates.get(condition))
                .map(r -> r.parseSub(section))
                .orElseThrow(() -> new IllegalArgumentException("Unknown condition " + config.getString("condition")));
    }

    private Generator parseGenerator(final ConfigurationSection config) {
        return Generator.withChance(config.getDouble("generator.chance"), lootItemGenerator);
    }
}
