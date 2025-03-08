package acute.loot;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Time walk effect class.
 */
public class TimewalkEffect extends AcuteLootSpecialEffect {


    public TimewalkEffect(String name, int id, List<LootMaterial> validLootMaterials, AcuteLoot plugin) {
        super(name, id, validLootMaterials, plugin);
    }

    @Override
    public void applyEffect(Event origEvent) {
        if (origEvent instanceof PlayerMoveEvent) {
            final PlayerMoveEvent event = (PlayerMoveEvent) origEvent;
            if (!onItem(event.getPlayer().getInventory().getBoots())) {
                return;
            }
            Player player = ((PlayerMoveEvent) origEvent).getPlayer();
            ItemStack boots = player.getInventory().getBoots();
            ItemMeta meta = boots.getItemMeta();
            if (((Damageable) meta).getDamage() > boots.getType().getMaxDurability()) {
                player.getInventory().setBoots(null);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                return;
            }
            ((Damageable) meta).setDamage(((Damageable) meta).getDamage() + plugin.getConfig()
                                                                                  .getInt("effects.timewalker.durability-modifier"));
            boots.setItemMeta(meta);
            Vector travel = event.getFrom().toVector().subtract(event.getTo().toVector());
            float direction = player.getLocation().getDirection().angle(travel);
            int timeWarp = plugin.getConfig().getInt("effects.timewalker.time-shift");
            int cropGrowthFactor = 1;
            boolean forward = true;
            // Only execute effect if moving forwards or backwards, NOT sideways
            if (direction > 1.65 || direction < 1.2) {
                if (direction < 1.2) {
                    timeWarp = -timeWarp;
                    cropGrowthFactor = -cropGrowthFactor;
                    forward = false;
                }
                if (plugin.getConfig().getBoolean("effects.timewalker.affect-world-time")) {
                    World world = player.getWorld();
                    world.setTime(world.getTime() + timeWarp);
                }
                List<Entity> entities = player.getNearbyEntities(30, 5, 30);
                for (Entity entity : entities) {
                    if (entity instanceof org.bukkit.entity.Ageable) {
                        org.bukkit.entity.Ageable ageable = (org.bukkit.entity.Ageable) entity;
                        double roll = AcuteLoot.random.nextDouble();
                        double chance = 20 / 100.0;
                        if (roll < chance) {
                            if (ageable.getAge() < 0 || (ageable.getAge() >= 0 && timeWarp < 0)) {
                                if (ageable instanceof Villager &&
                                        !plugin.getConfig().getBoolean("effects.timewalker.affect-villagers")) {
                                    // Don't age villagers if config disallows
                                    continue;
                                }
                                playGrowthParticles(forward, entity.getLocation());
                                ageable.setAge(ageable.getAge() + timeWarp);
                            }
                        }
                    }
                }
                List<Block> blocks = getNearbyBlocks(player.getLocation(), 20, 5, 20);
                for (Block block : blocks) {
                    if (block.getBlockData() instanceof org.bukkit.block.data.Ageable) {
                        Ageable ageable = (Ageable) block.getBlockData();
                        double roll = AcuteLoot.random.nextDouble();
                        double chance = 10 / 100.0;
                        if (((ageable.getAge() < ageable.getMaximumAge() && forward) ||
                             (ageable.getAge() > 0 && !forward)) && roll < chance) {
                            ageable.setAge(ageable.getAge() + cropGrowthFactor);
                            block.setBlockData(ageable);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns all blocks within a specified linear distance.
     *
     * @param location location of center point
     * @param xLength x-axis length
     * @param yLength y-axis length
     * @param zLength z-axis length
     * @return list of found blocks
     */
    public static List<Block> getNearbyBlocks(Location location, int xLength, int yLength, int zLength) {
        List<Block> blocks = new ArrayList<Block>();
        for (int x = location.getBlockX() - xLength; x <= location.getBlockX() + xLength; x++) {
            for (int y = location.getBlockY() - yLength; y <= location.getBlockY() + yLength; y++) {
                for (int z = location.getBlockZ() - zLength; z <= location.getBlockZ() + zLength; z++) {
                    blocks.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

    /**
     * Spawns particles indicating growth or decay near specified location.
     *
     * @param forward boolean indicating growth or decay
     * @param location location for center of particle animation
     */
    public void playGrowthParticles(boolean forward, Location location) {
        location = location.add(0, .5, 0);
        for (double x = location.getX() - .4; x <= location.getX() + .4; x += .1) {
            for (double y = location.getY() - .3; y <= location.getY() + .3; y += .1) {
                for (double z = location.getZ() - .4; z <= location.getZ() + +.4; z += .2) {
                    double roll = AcuteLoot.random.nextDouble();
                    double chance = 5 / 100.0;
                    if (roll < chance) {
                        if (forward) {
                            //location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, x, y, z, 1);
                        } else {
                            //location.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, x, y, z, 0, 0, 0, 0, 1);
                        }
                    }

                }
            }
        }
    }
}
