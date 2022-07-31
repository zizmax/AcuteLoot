package acute.loot;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

/**
 * Effect that leaves a trail of phantom blocks behind the player.
 */
public class BlockTrailEffect extends AcuteLootSpecialEffect {
    Random random = AcuteLoot.random;

    public BlockTrailEffect(String name, int id, List<LootMaterial> validLootMaterials, AcuteLoot plugin) {
        super(name, id, validLootMaterials, plugin);
    }

    @Override
    public void applyEffect(Event origEvent) {
        if (origEvent instanceof PlayerMoveEvent) {
            PlayerMoveEvent event = (PlayerMoveEvent) origEvent;
            if (!onItem(event.getPlayer().getInventory().getBoots())) {
                return;
            }


            ItemStack boots = event.getPlayer().getInventory().getBoots();
            Location trailBlock = event.getFrom();
            Material soilBlock = trailBlock.clone().subtract(0, 1, 0).getBlock().getType();
            BlockData trailBlockData = Material.AIR.createBlockData();

            // Gardener effect
            if (this.getName().equals("gardener")) {
                if (soilBlock.equals(Material.DIRT) ||
                        soilBlock.equals(Material.GRASS_BLOCK) ||
                        soilBlock.equals(Material.COARSE_DIRT)) {
                    int f = AcuteLoot.random.nextInt(FLOWER_TYPES.length);
                    trailBlockData = FLOWER_TYPES[f].createBlockData();
                }

            } else if (this.getName().equals("light-walker")) {
                // Light Walker effect
                trailBlockData = Material.LIGHT.createBlockData();
                ((Light) trailBlockData).setLevel(12);
                if (random.nextDouble() <= 0.2) {
                    ItemMeta meta = boots.getItemMeta();
                    ((Damageable) meta).setDamage(((Damageable) meta).getDamage() + plugin.getConfig()
                            .getInt("effects.light-walker.durability-modifier"));
                    boots.setItemMeta(meta);
                }
            }


            if (trailBlock.getBlock().getType().isAir()) {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    p.sendBlockChange(trailBlock, trailBlockData);
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (trailBlock.getBlock().getType().isAir()) {
                            for (Player p : plugin.getServer().getOnlinePlayers()) {
                                p.sendBlockChange(trailBlock, Material.AIR.createBlockData());
                            }
                        }
                    }

                }.runTaskLater(plugin, 40L);
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
