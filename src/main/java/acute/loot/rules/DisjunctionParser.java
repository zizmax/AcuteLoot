package acute.loot.rules;

import com.github.phillip.h.acutelib.util.Pair;
import lombok.AllArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class DisjunctionParser implements RuleParser.SubRuleParser {

    private final RuleParser.SubRuleParser partParser;

    @Override
    public List<Pair<Condition, Spawner>> parseSub(ConfigurationSection config) {
        final ConfigurationSection section = config.getConfigurationSection("loot-rules");
        return section.getKeys(false).stream()
                .map(section::getConfigurationSection)
                .flatMap(ruleDefinition -> partParser.parseSub(ruleDefinition).stream())
                .collect(Collectors.toList());
    }
}
