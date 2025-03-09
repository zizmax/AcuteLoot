package acute.loot;

import com.cryptomorin.xseries.particles.XParticle;
import com.github.phillip.h.acutelib.util.Util;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Random;

/**
 * Tool particle effect class.
 */
public class ToolParticleEffect extends AcuteLootSpecialEffect {

    private final Particle particle;
    private final boolean beamVisible;

    /**
     * Constructs a new tool particle effect object.
     *
     * @param name string
     * @param id effect ID
     * @param validLootMaterials loot materials that can have tool particle effect
     * @param particle particle type
     * @param beamVisible boolean to make beam visible like laser
     * @param plugin AcuteLoot object
     */
    public ToolParticleEffect(String name, int id, List<LootMaterial> validLootMaterials,
                              Particle particle, boolean beamVisible, AcuteLoot plugin) {
        super(name, id, validLootMaterials, plugin);
        this.particle = particle;
        this.beamVisible = beamVisible;
    }

    @Override
    public void applyEffect(Event origEvent) {
        if (origEvent instanceof PlayerInteractEvent) {
            PlayerInteractEvent event = (PlayerInteractEvent) origEvent;
            Player player = event.getPlayer();
            int maxBeamDistance = plugin.getConfig().getInt("effects.weapons.max-distance");
            double beamSegmentLength = 0.3;
            final Random random = AcuteLoot.random;

            Particle.DustOptions dustOptions = null;

            if (particle == XParticle.DUST.get()) {
                if (this.getName().contains("laser")) {
                    dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1);
                }
                if (this.getName().equals("redstone")) {
                    dustOptions = new Particle.DustOptions(Color.fromRGB(random.nextInt(255), random.nextInt(255), random
                            .nextInt(255)), 6);
                }
            }

            final List<Location> locations = Util.getLine(player.getEyeLocation(), maxBeamDistance, beamSegmentLength);
            for (int i = 0; i < locations.size(); i++) {
                if (beamVisible) {
                    if (particle == XParticle.DUST.get()) {
                        player.getWorld().spawnParticle(particle, locations.get(i), 1, dustOptions);
                    } else if (particle == XParticle.DRAGON_BREATH.get()) { // Add other directional/velocity particles
                        player.getWorld().spawnParticle(particle, locations.get(i), 0, 0, 0, 0);
                    } else {
                        player.getWorld().spawnParticle(particle, locations.get(i), 1);
                    }
                }

                final List<Entity> entities = (List<Entity>) player.getWorld()
                                                                   .getNearbyEntities(locations.get(i), 0.5, 0.5, 0.5);
                for (int n = 0; n < entities.size(); n++) {
                    if (entities.get(n).equals(player)) {
                        entities.remove(entities.get(n));
                    }
                }

                if (!locations.get(i).getBlock().getType().isAir() || entities.size() > 0) {
                    Location location;
                    if (locations.size() == 0 || i == 0) {
                        location = player.getEyeLocation();
                    } else {
                        location = locations.get(i - 1);
                    }

                    if (particle == XParticle.DUST.get()) {
                        player.getWorld().spawnParticle(particle, location, 1, dustOptions);
                    } else if (particle == XParticle.DRAGON_BREATH.get()) { // Add other directional/velocity particles
                        player.getWorld().spawnParticle(particle, location, 0, 0, 0, 0);
                    } else if (particle == XParticle.ENCHANT.get()) {
                        player.getWorld().spawnParticle(particle, location, 100);
                    } else if (particle == XParticle.NAUTILUS.get()) {
                        player.getWorld().spawnParticle(particle, location.add(0, 1, 0), 100);
                    } else if (particle == XParticle.ENTITY_EFFECT.get()) {
                        Color spellColor = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
                        // 1.20.5/1.20.6 changed SPELL_MOB to ENTITY_EFFECT and requires a color
                        try {
                            player.getWorld().spawnParticle(particle, location, 10, spellColor);
                        } catch (IllegalArgumentException e) {
                            player.getWorld().spawnParticle(particle, location, 10);
                        }
                    } else if (particle == XParticle.ITEM_SLIME.get() || particle == XParticle.SPLASH.get()) {
                        player.getWorld().spawnParticle(particle, location, 40);
                    } else if (particle == XParticle.LAVA.get()) {
                        player.getWorld().spawnParticle(particle, location, 5);
                    } else {
                        player.getWorld().spawnParticle(particle, location, 100);
                    }

                    return;
                }
            }
        }
    }
}
