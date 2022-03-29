package acute.loot.rules;

import com.github.phillip.h.acutelib.util.Pair;
import lombok.AllArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static acute.loot.rules.RuleUtils.readMobConditions;

@AllArgsConstructor
class EntityDeathParser implements RuleParser.SubRuleParser {

    @Override
    public List<Pair<Condition, Spawner>> parseSub(ConfigurationSection config) {
        // TODO MAKE THIS BETTER
        //final String rain = config.getString("weather");
        //final List<Condition> conditions = new ArrayList<>();
        //if ("raining".equals(rain)) {
        //    conditions.add(EventConditions.isRainingOnEntityDeath());
        //}

        final Pair<List<EntityType>, List<Class<? extends Entity>>> mobSettings = readMobConditions(config);

        final List<Condition> conditions = new ArrayList<>();
        if (!mobSettings.left().isEmpty()) {
            conditions.add(EventConditions.onEntityDeath(mobSettings.left()));
        }
        if (!mobSettings.right().isEmpty()) {
            conditions.add(EventConditions.onEntityClassDeath(mobSettings.right()));
        }

        return Collections.singletonList(new Pair<>(Condition.allOf(conditions), Spawner.intoMobDrop()));
    }
}
