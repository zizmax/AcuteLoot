package acute.loot;

import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class BowTeleportEffect extends AcuteLootSpecialEffect {

    public BowTeleportEffect(String name, String ns, int id, List<LootMaterial> validLootMaterials, AcuteLoot plugin) {
        super(name, ns, id, validLootMaterials, plugin);
    }

    @Override
    public void apply(Event origEvent) {
        if (origEvent instanceof EntityShootBowEvent) {
            if (plugin.getConfig().getBoolean("effects.enderbow.enabled")) {
                EntityShootBowEvent event = (EntityShootBowEvent) origEvent;
                Player player = (Player) event.getEntity();
                Arrow arrow = (Arrow) event.getProjectile();
                arrow.addPassenger(player);
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1, 1);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (arrow.isOnGround() || arrow.isDead()) {
                            arrow.removePassenger(player);
                            player.playSound(player.getLocation(), Sound.BLOCK_HONEY_BLOCK_FALL, 1, 1);
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 0L);
            }
        }
    }

}
