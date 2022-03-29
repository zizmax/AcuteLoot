package acute.loot.rules;

import com.github.phillip.h.acutelib.util.Pair;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

interface RuleParser {

    List<Rule> parse(ConfigurationSection config);

    interface SubRuleParser {
        List<Pair<Condition, Spawner>> parseSub(ConfigurationSection config);
    }


}
