package acute.loot.rules;

import acute.loot.AlApi;
import acute.loot.Module;
import acute.loot.generator.LootItemGenerator;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class LootRulesModule implements Module {

    private final AlApi alApi;

    private Listeners listeners;

    public LootRulesModule(AlApi alApi) {
        this.alApi = alApi;
    }

    @Override
    public void enable() {
        final LootItemGenerator lootItemGenerator = alApi.getBaseLootGenerator();
        final ConfigurationSection config = alApi.getBaseConfiguration();

        final Map<String, RuleParser.SubRuleParser> ruleParsers = new HashMap<>();
        final DelegateParser delegateParser = new DelegateParser(alApi, ruleParsers);
        ruleParsers.put("entity-death", new EntityDeathParser());
        ruleParsers.put("any-of", new DisjunctionParser(delegateParser));
        ruleParsers.put("entity-spawn", new EntitySpawnParser());
        ruleParsers.put("chest-open", new ChestOpenedParser());

        final Set<Rule> rules = new HashSet<>();
        final ConfigurationSection section = config.getConfigurationSection("loot-rules");
        for (String ruleSection : section.getKeys(false)) {
            alApi.log().info("Parsing rule " + ruleSection);
            try {
                rules.addAll(delegateParser.parse(section.getConfigurationSection(ruleSection)));
            } catch (Exception e) {
                alApi.log().warning("Failed to parse rule: " + e.getMessage());
            }
        }
        alApi.log().info("Parsed " + rules.size() + " rules");

        RuleRunner runner = new RunAllRunner(rules);
        listeners = new Listeners(runner::run);


        listeners = new Listeners(runner::run);
        alApi.addListener(listeners);
    }

    @Override
    public void disable() {
        alApi.removeListener(listeners);
    }
}
