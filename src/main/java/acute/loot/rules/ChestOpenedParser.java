package acute.loot.rules;

import com.github.phillip.h.acutelib.util.Pair;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChestOpenedParser implements RuleParser.SubRuleParser {

    @Override
    public List<Pair<Condition, Spawner>> parseSub(ConfigurationSection config) {
        final List<Condition> conditions = new ArrayList<>();

        // (Probably) always need this, so loot only generates for natural chests on first open
        conditions.add(EventConditions.clickedChestHasLootTable());

        // Read the config section to create the conditions, e.g.
        if (config.contains("in-biome")) {
            final String biome = config.getString("in-biome");
            conditions.add(EventConditions.isClickedBlockInBiome(Biome.valueOf(biome)));
        }

        // TODO: The Spawner.intoChest() method is not completely implemented
        return Collections.singletonList(new Pair<>(Condition.allOf(conditions), Spawner.intoChest()));
    }
}
