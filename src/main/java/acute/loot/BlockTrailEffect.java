package acute.loot;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.cryptomorin.xseries.XMaterial;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

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
                    List<Material> validFlowers = Arrays.stream(FLOWER_TYPES)
                                                        .map(XMaterial::parseMaterial)
                                                        .filter(Objects::nonNull)
                                                        .collect(Collectors.toList());
                    if (!validFlowers.isEmpty()) {
                        int f = AcuteLoot.random.nextInt(validFlowers.size());
                        trailBlockData = validFlowers.get(f).createBlockData();
                    }
                }

            } else if (this.getName().equals("light-walker")) {
                // Light Walker effect
                Material lightMat = XMaterial.LIGHT.parseMaterial();
                if (lightMat != null) {
                    trailBlockData = createLightBlockData(lightMat);
                    if (random.nextDouble() <= 0.2) {
                        ItemMeta meta = boots.getItemMeta();
                        ((Damageable) meta).setDamage(((Damageable) meta).getDamage() + plugin.getConfig()
                                .getInt("effects.light-walker.durability-modifier"));
                        boots.setItemMeta(meta);
                    }
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
    private static final XMaterial[] FLOWER_TYPES = new XMaterial[] {
            XMaterial.DANDELION, XMaterial.POPPY,
            XMaterial.BLUE_ORCHID, XMaterial.ALLIUM,
            XMaterial.AZURE_BLUET, XMaterial.RED_TULIP,
            XMaterial.ORANGE_TULIP, XMaterial.WHITE_TULIP,
            XMaterial.PINK_TULIP, XMaterial.CORNFLOWER,
            XMaterial.OXEYE_DAISY, XMaterial.LILY_OF_THE_VALLEY,
            XMaterial.WITHER_ROSE, XMaterial.LILAC,
            XMaterial.ROSE_BUSH, XMaterial.PEONY
    };

    private static BlockData createLightBlockData(final Material lightMat) {
        try {
            return Bukkit.createBlockData(lightMat, "[level=12]");
        } catch (IllegalArgumentException e) {
            return lightMat.createBlockData();
        }
    }

}
