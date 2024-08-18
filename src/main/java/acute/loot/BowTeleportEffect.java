package acute.loot;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Effect that teleports the shooter to the location an arrow strikes.
 */
public class BowTeleportEffect extends AcuteLootSpecialEffect {

    private Scheduler.Task endArrowRideTask;

    public BowTeleportEffect(String name, int id, List<LootMaterial> validLootMaterials, AcuteLoot plugin) {
        super(name, id, validLootMaterials, plugin);
    }

    @Override
    public void applyEffect(Event origEvent) {
        if (origEvent instanceof EntityShootBowEvent) {
            EntityShootBowEvent event = (EntityShootBowEvent) origEvent;
            Player launchee = null;
            Arrow arrow = (Arrow) event.getProjectile();
            if (this.getName() == "enderbow"){
                launchee = (Player) event.getEntity();
            }
            if (this.getName() == "flingbow"){
                Player shooter = (Player) event.getEntity();
                shooter.sendMessage("tried to shoot y u no work??");
                List<Player> players = event.getEntity().getWorld().getPlayers();
                if (players.get(0).equals(shooter)){
                    launchee = players.get(1);
                }
                else {
                    launchee = players.get(0);
                }
                arrow.teleport(launchee.getEyeLocation());
            }

            arrow.addPassenger(launchee);
            launchee.playSound(launchee.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1, 1);

            Player finalLaunchee = launchee;
            endArrowRideTask = Scheduler.runTimerEntity(arrow, new Runnable() {
                @Override
                public void run() {
                    if ((arrow.isOnGround() || arrow.isDead()) && arrow.getPassengers().contains(finalLaunchee)) {
                        arrow.removePassenger(finalLaunchee);
                        finalLaunchee.playSound(finalLaunchee.getLocation(), Sound.BLOCK_HONEY_BLOCK_FALL, 1, 1);
                        endArrowRideTask.cancel();
                    }
                }
            }, 0L, 0L);
        }
    }

}
