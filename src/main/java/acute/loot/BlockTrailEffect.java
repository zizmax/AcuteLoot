package acute.loot;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Effect that leaves a trail of phantom blocks behind the player.
 */
public class BlockTrailEffect extends AcuteLootSpecialEffect {

    public BlockTrailEffect(String name, int id, List<LootMaterial> validLootMaterials, AcuteLoot plugin) {
        super(name, id, validLootMaterials, plugin);
    }

    @Override
    public void applyEffect(Event origEvent) {
        if (origEvent instanceof PlayerMoveEvent) {
            PlayerMoveEvent event = (PlayerMoveEvent) origEvent;

            // Gardener effect
            Location flowerTrail = event.getFrom();
            Material soilBlock = flowerTrail.clone().subtract(0, 1, 0).getBlock().getType();
            if (soilBlock.equals(Material.DIRT) ||
                    soilBlock.equals(Material.GRASS_BLOCK) ||
                    soilBlock.equals(Material.GRASS_PATH) ||
                    soilBlock.equals(Material.COARSE_DIRT)) {
                int f = AcuteLoot.random.nextInt(FLOWER_TYPES.length);
                if (flowerTrail.getBlock().getType().equals(Material.AIR)) {
                    for (Player p : plugin.getServer().getOnlinePlayers()) {
                        p.sendBlockChange(flowerTrail, FLOWER_TYPES[f].createBlockData());
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (Player p : plugin.getServer().getOnlinePlayers()) {
                                p.sendBlockChange(flowerTrail, Material.AIR.createBlockData());
                            }
                        }

                    }.runTaskLater(plugin, 40L);
                }
            }
        }
    }

    // Flowers used by the effect
    private static final Material[] FLOWER_TYPES = new Material[] { Material.DANDELION, Material.POPPY,
                                                                    Material.BLUE_ORCHID, Material.ALLIUM,
                                                                    Material.AZURE_BLUET, Material.RED_TULIP,
                                                                    Material.ORANGE_TULIP, Material.WHITE_TULIP,
                                                                    Material.PINK_TULIP, Material.CORNFLOWER,
                                                                    Material.OXEYE_DAISY, Material.LILY_OF_THE_VALLEY,
                                                                    Material.WITHER_ROSE, Material.LILAC,
                                                                    Material.ROSE_BUSH, Material.PEONY
    };

}
