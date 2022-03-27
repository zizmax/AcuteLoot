package acute.loot.mobdrops;

import acute.loot.AlApi;
import acute.loot.Module;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;

import java.util.HashMap;
import java.util.Map;

import static com.github.phillip.h.acutelib.util.Checks.requireNonEmpty;

public class MobDropLootModule implements Module {

    private final AlApi alApi;
    private final Map<String, Class<? extends Entity>> entityTypeMap;

    private MobDropLootListener mobDropLootListener;

    public MobDropLootModule(AlApi alApi) {
        this.alApi = alApi;
        entityTypeMap = new HashMap<>();
        entityTypeMap.put("monster", Monster.class);
        entityTypeMap.put("creature", Creature.class);
    }

    @Override
    public void enable() {
        final String entityType = requireNonEmpty(alApi.getBaseConfiguration().getString("loot-sources.mobs.drops.global.type"),
                                                  "entitytype must be present and non-empty");
        final Class<? extends Entity> entityClass = entityTypeMap.get(entityType.toLowerCase());
        if (entityClass == null) {
            throw new IllegalArgumentException("Unknown entity type " + entityType);
        }

        if (!alApi.getBaseConfiguration().contains("loot-sources.mobs.drops.global.chance")) {
            throw new IllegalArgumentException("Mob drop chance missing");
        }

        final double chance = alApi.getBaseConfiguration().getDouble("loot-sources.mobs.drops.global.chance") / 100.0;

        mobDropLootListener = new MobDropLootListener(alApi.getBaseLootGenerator(), entityClass, chance);
        alApi.addListener(mobDropLootListener);
    }

    @Override
    public void disable() {
        alApi.removeListener(mobDropLootListener);
    }
}
