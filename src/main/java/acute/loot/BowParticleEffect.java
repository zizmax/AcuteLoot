package acute.loot;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class BowParticleEffect extends LootSpecialEffect {

    private final Particle particle;

    public BowParticleEffect(String name, String ns, int id, List<LootMaterial> validLootMaterials, Particle particle, AcuteLoot plugin) {
        super(name, ns, id, validLootMaterials, plugin);
        this.particle = particle;
    }

    public double healEntity(LivingEntity entity, double health) {
        return Math.min(entity.getHealth() + health, entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
    }

    @Override
    public void apply(Event origEvent) {
        if (origEvent instanceof EntityShootBowEvent) {
            EntityShootBowEvent event = (EntityShootBowEvent) origEvent;
            Arrow arrow = (Arrow) event.getProjectile();
            World world = arrow.getWorld();
            // Heal Effect
            if (this.getName().equals("bows_heart")) {
                arrow.setColor(Color.RED);
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
            if (event.getEntity() instanceof LivingEntity && event.getDamager() instanceof Arrow) {
                LivingEntity target = (LivingEntity) event.getEntity();
                if (particle == Particle.HEART) {
                    Arrow arrow = (Arrow) event.getDamager();
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
