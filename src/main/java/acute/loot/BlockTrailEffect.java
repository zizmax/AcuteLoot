package acute.loot;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class BlockTrailEffect extends LootSpecialEffect {

    public BlockTrailEffect(String name, int id, List<LootMaterial> validLootMaterials, AcuteLoot plugin) {
        super(name, id, validLootMaterials, plugin);
    }

    @Override
    public void apply(Event origEvent) {
        if (origEvent instanceof PlayerMoveEvent) {
            PlayerMoveEvent event = (PlayerMoveEvent) origEvent;
            Player player = event.getPlayer();

            // Gardener effect
            if (plugin.getConfig().getDouble("effects.gardener.chance") > 0) {
                Location flower_trail = event.getFrom();
                Material soilBlock = flower_trail.clone().subtract(0, 1, 0).getBlock().getType();
                if (soilBlock.equals(Material.DIRT) ||
                        soilBlock.equals(Material.GRASS_BLOCK) ||
                        soilBlock.equals(Material.GRASS_PATH) ||
                        soilBlock.equals(Material.COARSE_DIRT)) {
                    int f = AcuteLoot.random.nextInt(FLOWER_TYPES.length);
                    if (flower_trail.getBlock().getType().equals(Material.AIR)) {
                        for (Player p : plugin.getServer().getOnlinePlayers()) {
                            p.sendBlockChange(flower_trail, FLOWER_TYPES[f].createBlockData());
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                for (Player p : plugin.getServer().getOnlinePlayers()) {
                                    p.sendBlockChange(flower_trail, Material.AIR.createBlockData());
                                }
                            }

                        }.runTaskLater(plugin, 40L);
                    }
                }
            }
        }
    }

    // Flowers used by the effect
    private static final Material[] FLOWER_TYPES = new Material[]{
            Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM,
            Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP,
            Material.PINK_TULIP, Material.CORNFLOWER, Material.OXEYE_DAISY, Material.LILY_OF_THE_VALLEY,
            Material.WITHER_ROSE, Material.LILAC, Material.ROSE_BUSH, Material.PEONY
    };

}
