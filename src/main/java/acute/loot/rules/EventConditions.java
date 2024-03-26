package acute.loot.rules;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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

    static Condition clickedChestHasLootTable() {
        return e -> {
            if (!(e instanceof PlayerInteractEvent)) return false;
            final PlayerInteractEvent pie = (PlayerInteractEvent) e;
            if (pie.getClickedBlock() == null || pie.getClickedBlock().getType() != Material.CHEST) return false;
            final Chest chest = (Chest) pie.getClickedBlock().getState();
            return chest.getLootTable() != null;
        };
    }

    static Condition isClickedBlockInBiome(final Biome biome) {
        return e -> {
            if (!(e instanceof PlayerInteractEvent)) return false;
            final PlayerInteractEvent pie = (PlayerInteractEvent) e;
            if (pie.getClickedBlock() == null) return false;
            return pie.getClickedBlock().getBiome().equals(biome);
        };
    }


}
