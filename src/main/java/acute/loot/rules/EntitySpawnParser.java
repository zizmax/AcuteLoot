package acute.loot.rules;

import com.github.phillip.h.acutelib.util.Pair;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static acute.loot.rules.RuleUtils.*;

public class EntitySpawnParser implements RuleParser.SubRuleParser {

    @Override
    public List<Pair<Condition, Spawner>> parseSub(ConfigurationSection config) {
        final Pair<List<EntityType>, List<Class<? extends Entity>>> mobSettings = readMobConditions(config);

        final List<Condition> conditions = new ArrayList<>();
        if (!mobSettings.left().isEmpty()) {
            conditions.add(EventConditions.onEntitySpawn(mobSettings.left()));
        }
        if (!mobSettings.right().isEmpty()) {
            conditions.add(EventConditions.onEntityClassSpawn(mobSettings.right()));
        }

        return Collections.singletonList(new Pair<>(Condition.allOf(conditions), Spawner.intoMobHand()));
    }
}
