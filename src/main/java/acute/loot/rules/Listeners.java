package acute.loot.rules;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.function.Consumer;

@AllArgsConstructor
class Listeners implements Listener {

    private final @NonNull Consumer<Event> callback;

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        callback.accept(e);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        callback.accept(e);
    }

}
