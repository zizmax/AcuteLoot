package acute.loot.mobdrops;

import acute.loot.generator.LootItemGenerator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

@RequiredArgsConstructor
class MobDropLootListener implements Listener {

    private final @NonNull LootItemGenerator lootItemGenerator;
    private final @NonNull Class<? extends Entity> entityParent;
    private final double chance;

    @EventHandler
    public void onMobDeath(final EntityDeathEvent event) {
        if (entityParent.isInstance(event.getEntity())) {
            event.getDrops().addAll(lootItemGenerator.createLootWithChance(chance));
        }
    }

}
