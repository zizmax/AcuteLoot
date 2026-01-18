package acute.loot;

import com.cryptomorin.xseries.XAttribute;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Effect that leaves a trail of particles behind fired arrows.
 */
public class BowParticleEffect extends AcuteLootSpecialEffect {

    private final Particle particle;

    public BowParticleEffect(String name, int id, List<LootMaterial> validLootMaterials, Particle particle, AcuteLoot plugin) {
        super(name, id, validLootMaterials, plugin);
        this.particle = particle;
    }

    public double healEntity(LivingEntity entity, double health) {
        return Math.min(entity.getHealth() + health, entity.getAttribute(XAttribute.MAX_HEALTH.get()).getValue());
    }

    @Override
    public void applyEffect(Event origEvent) {
        if (origEvent instanceof EntityShootBowEvent) {
            EntityShootBowEvent event = (EntityShootBowEvent) origEvent;
            Entity arrow = event.getProjectile();
            World world = arrow.getWorld();
            // Heal Effect
            if (this.getName().equals("bows_heart")) {
                if (arrow instanceof Arrow) {
                    ((Arrow) arrow).setColor(Color.RED);
                }
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    world.spawnParticle(particle, arrow.getLocation(), 1);
                    if (arrow.isOnGround() || arrow.isDead()) {
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 0L);
        } else if (origEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) origEvent;
            if (event.getEntity() instanceof LivingEntity && event.getDamager() instanceof AbstractArrow) {
                LivingEntity target = (LivingEntity) event.getEntity();
                if (particle == XParticle.HEART.get()) {
                    AbstractArrow arrow = (AbstractArrow) event.getDamager();
                    if (arrow.getShooter() instanceof Player) {
                        Player player = (Player) arrow.getShooter();
                        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
                    }
                    target.setHealth(healEntity(target, event.getFinalDamage()));
                    event.setDamage(0);
                }
            }
        }
    }
}
