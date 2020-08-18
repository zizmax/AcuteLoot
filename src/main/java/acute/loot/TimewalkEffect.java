package acute.loot;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class TimewalkEffect extends LootSpecialEffect{


    public TimewalkEffect(String name, int id, List<LootMaterial> validLootMaterials, AcuteLoot plugin) {
        super(name, id, validLootMaterials, plugin);
    }

    @Override
    public void apply(Event origEvent) {
        if (origEvent instanceof PlayerMoveEvent) {
            PlayerMoveEvent event = (PlayerMoveEvent) origEvent;
            if (plugin.getConfig().getDouble("effects.timewalker.chance") > 0) {
                Player player = ((PlayerMoveEvent) origEvent).getPlayer();
                ItemStack boots = player.getInventory().getBoots();
                ItemMeta meta = boots.getItemMeta();
                if (((Damageable) meta).getDamage() > boots.getType().getMaxDurability()) {
                    player.getInventory().setBoots(null);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                    return;
                }
                ((Damageable) meta).setDamage(((Damageable) meta).getDamage() + plugin.getConfig().getInt("effects.timewalker.durability-modifier"));
                boots.setItemMeta(meta);
                Vector travel = event.getFrom().toVector().subtract(event.getTo().toVector());
                float direction = player.getLocation().getDirection().angle(travel);
                int timeWarp = plugin.getConfig().getInt("effects.timewalker.time-shift");
                int cropGrowthFactor = 1;
                boolean forward = true;
                //TODO: Remove debug titles
                if (forward) {
                    if(direction > 1.65) player.sendTitle("forward", "" + direction, 0, 1, 0);
                    else if (direction < 1.2) {
                        timeWarp = -timeWarp;
                        cropGrowthFactor = -cropGrowthFactor;
                        forward = false;
                        player.sendTitle("backward", "" + direction, 0, 1, 0);
                    }
                    else player.sendTitle("???", "" + direction, 0, 1, 0);
                    World world = player.getWorld();
                    world.setTime(world.getTime() + timeWarp);
                    List<Entity> entities = player.getNearbyEntities(30, 5, 30);
                    for (Entity entity : entities) {
                        if (entity instanceof org.bukkit.entity.Ageable) {
                            org.bukkit.entity.Ageable ageable = (org.bukkit.entity.Ageable) entity;
                            entity.setCustomName(String.valueOf(ageable.getAge()));
                            double roll = AcuteLoot.random.nextDouble();
                            double chance = 20 / 100.0;
                            if (ageable.getAge() < 0 && roll < chance) {
                                playGrowthParticles(forward, entity.getLocation());
                                ageable.setAge(ageable.getAge() + timeWarp);
                                //ageable.setCustomName(String.valueOf(ageable.getAge()));
                                //ageable.setCustomNameVisible(true);
                            }
                        }
                    }
                    List<Block> blocks = getNearbyBlocks(player.getLocation(), 20, 5, 20);
                    for (Block block : blocks) {
                        if (block.getBlockData() instanceof org.bukkit.block.data.Ageable) {
                            Ageable ageable = (Ageable) block.getBlockData();
                            double roll = AcuteLoot.random.nextDouble();
                            double chance = 10 / 100.0;
                            if (((ageable.getAge() < ageable.getMaximumAge() && forward) || (ageable.getAge() > 0 && !forward)) && roll < chance) {
                                ageable.setAge(ageable.getAge() + cropGrowthFactor);
                                block.setBlockData(ageable);
                            }
                        }
                    }
                }
            }
        }
    }

    public static List<Block> getNearbyBlocks(Location location, int x_length, int y_length, int z_length) {
        List<Block> blocks = new ArrayList<Block>();
        for(int x = location.getBlockX() - x_length; x <= location.getBlockX() + x_length; x++) {
            for(int y = location.getBlockY() - y_length; y <= location.getBlockY() + y_length; y++) {
                for(int z = location.getBlockZ() - z_length; z <= location.getBlockZ() + z_length; z++) {
                    blocks.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }


    public void playGrowthParticles(boolean forward, Location location) {
        location = location.add(0, .5, 0);
        for(double x = location.getX() - .4; x <= location.getX() + .4; x += .1) {
            for (double y = location.getY() - .3; y <= location.getY() + .3; y += .1) {
                for (double z = location.getZ() - .4; z <= location.getZ() + + .4; z += .2) {
                    double roll = AcuteLoot.random.nextDouble();
                    double chance = 5 / 100.0;
                    if(roll < chance) {
                        if(forward) location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, x, y, z, 1);
                        else location.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, x, y, z, 0, 0, 0, 0, 1);
                    }

                }
            }
        }
    }
}
