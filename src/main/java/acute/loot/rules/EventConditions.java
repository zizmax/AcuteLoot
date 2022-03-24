package acute.loot.rules;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

final class EventConditions {

    private EventConditions() {}

    static Condition onEntitySpawn(final Collection<EntityType> entityTypes) {
        final Set<EntityType> asSet = new HashSet<>(entityTypes);
        return e -> e instanceof EntitySpawnEvent && asSet.contains(((EntitySpawnEvent) e).getEntity().getType());
    }

    static Condition onEntityClassSpawn(final Collection<Class<? extends Entity>> entityClasses) {
        return e -> e instanceof EntitySpawnEvent && entityClasses.stream().anyMatch(c -> c.isInstance(((EntitySpawnEvent) e).getEntity()));
    }

    static Condition onEntityDeath(final Collection<EntityType> entityTypes) {
        final Set<EntityType> asSet = new HashSet<>(entityTypes);
        return e -> e instanceof EntityDeathEvent && asSet.contains(((EntityDeathEvent) e).getEntity().getType());
    }

    static Condition onEntityClassDeath(final Collection<Class<? extends Entity>> entityClasses) {
        return e -> e instanceof EntityDeathEvent && entityClasses.stream().anyMatch(c -> c.isInstance(((EntityDeathEvent) e).getEntity()));
    }

    static Condition isRainingOnEntityDeath() {
        return e -> e instanceof EntityDeathEvent && !((EntityDeathEvent) e).getEntity().getWorld().isClearWeather();
    }


}
