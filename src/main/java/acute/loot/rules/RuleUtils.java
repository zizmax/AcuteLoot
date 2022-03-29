package acute.loot.rules;

import com.github.phillip.h.acutelib.util.Pair;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class RuleUtils {

    private static final Map<String, Class<? extends Entity>> entityTypeMap = new HashMap<>();

    static {
        entityTypeMap.put("monster", Monster.class);
        entityTypeMap.put("creature", Creature.class);
    }

    private RuleUtils() {}

    static Stream<String> streamConfigList(final ConfigurationSection config, final String key) {
        return Optional.ofNullable(config.getStringList(key))
                .map(Collection::stream)
                .orElse(Stream.empty());
    }

    static List<EntityType> readMobType(final ConfigurationSection config) {
        return streamConfigList(config, "mob-type")
                .map(String::toUpperCase)
                .map(EntityType::valueOf)
                .collect(Collectors.toList());
    }

    static List<Class<? extends Entity>> readMobClass(final ConfigurationSection config) {
        return streamConfigList(config, "mob-class")
                .map(String::toLowerCase)
                .map(entityTypeMap::get)
                .collect(Collectors.toList());
    }

    static Pair<List<EntityType>, List<Class<? extends Entity>>> readMobConditions(final ConfigurationSection config) {
        final List<EntityType> entityType = readMobType(config);
        final List<Class<? extends Entity>> entityClass = readMobClass(config);

        if (entityClass.isEmpty() && entityType.isEmpty()) {
            throw new IllegalArgumentException("Either mob-type or mob-class must be specified and non-empty lists");
        } else if (!entityClass.isEmpty() && !entityType.isEmpty()) {
            throw new IllegalArgumentException("Both mob-type and mob-class cannot be specified and non-empty lists");
        }

        return new Pair<>(entityType, entityClass);
    }

}
