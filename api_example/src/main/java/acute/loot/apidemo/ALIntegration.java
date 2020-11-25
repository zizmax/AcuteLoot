package acute.loot.apidemo;

import acute.loot.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Plugin class. ALL references to AcuteLoot here!
 */
public class ALIntegration {

    private static final String AL_API_VERSION = "1.0.0-beta";

    private final Logger logger;

    private final API api;

    public ALIntegration(final Plugin plugin) {
        logger = plugin.getLogger();
        api = new API(plugin);
    }

    public boolean checkVersion() {
        return ALApi.apiVersionNewerThan(AL_API_VERSION);
    }

    public void addEffects() {
        logger.info("Adding AcuteLoot effects");
        final DemoEffect effect = new DemoEffect("Demo Effect", 101, Collections.singletonList(LootMaterial.HOE), api.getAcuteLoot());
        api.registerEffect(effect, 100);
    }

    public void removeEffects() {
        logger.info("Removing AcuteLoot effects");
        api.unregisterPluginEffects();
    }

    private class DemoEffect extends LootSpecialEffect {

        public DemoEffect(String name, int id, List<LootMaterial> validMaterials, AcuteLoot plugin) {
            super(name, id, validMaterials, plugin);
        }

        @Override
        public void apply(Event event) {
            if (event instanceof PlayerInteractEvent) {
                final Player player = ((PlayerInteractEvent) event).getPlayer();
                final Optional<LootItem> lootItem = api.getLootItem(player.getInventory().getItemInMainHand());
                player.sendMessage("Hello from DemoEffect!");
                player.sendMessage("LootCode for this item: " + lootItem.map(LootItem::lootCode).orElse("<NOT FOUND>"));
            }
        }
    }

}
