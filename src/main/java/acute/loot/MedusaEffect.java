package acute.loot;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Medusa effect class.
 */
public class MedusaEffect extends AcuteLootSpecialEffect {


    public MedusaEffect(String name, int id, List<LootMaterial> validLootMaterials, AcuteLoot plugin) {
        super(name, id, validLootMaterials, plugin);
    }

    @Override
    public void applyEffect(Event origEvent) {
        if (origEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) origEvent;
            if (event.getDamager() instanceof Arrow && event.getEntity() instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) event.getEntity();
                event.getEntity().playEffect(EntityEffect.ENTITY_POOF);
                World world = event.getEntity().getWorld();
                XSound.BLOCK_STONE_PLACE.play(event.getEntity().getLocation(), 2.0f, 1.0f);
                List<XMaterial> stoneXMaterials;
                if (world.getEnvironment().equals(World.Environment.NETHER)) {
                    stoneXMaterials = Arrays.asList(
                            XMaterial.BLACKSTONE,
                            XMaterial.CRACKED_POLISHED_BLACKSTONE_BRICKS,
                            XMaterial.POLISHED_BLACKSTONE_BRICKS,
                            XMaterial.POLISHED_BLACKSTONE,
                            XMaterial.GRAVEL,
                            XMaterial.SOUL_SAND
                    );
                } else if (world.getEnvironment().equals(World.Environment.THE_END)) {
                    stoneXMaterials = Arrays.asList(XMaterial.END_STONE);
                } else {
                    stoneXMaterials = Arrays.asList(XMaterial.COBBLESTONE, XMaterial.MOSSY_COBBLESTONE);
                }

                List<Material> stoneBlockTypes = stoneXMaterials.stream()
                                                                .map(XMaterial::parseMaterial)
                                                                .filter(java.util.Objects::nonNull)
                                                                .collect(Collectors.toList());

                if (event.getEntity() instanceof Player) {
                    Player player = (Player) event.getEntity();
                    if (plugin.getConfig().getBoolean("effects.medusa.affect-players")) {
                        player.setMetadata("turnedToStone", new FixedMetadataValue(plugin, true));
                        player.setHealth(0);
                    } else {
                        return;
                    }
                } else {
                    if (plugin.getConfig().getBoolean("effects.medusa.drop-loot")) {
                        for (ItemStack item : livingEntity.getEquipment().getArmorContents()) {
                            if (item != null && !item.getType().isAir()) {
                                livingEntity.getWorld().dropItemNaturally(livingEntity.getLocation(), item);
                            }
                        }
                        ItemStack mainHand = livingEntity.getEquipment().getItemInMainHand();
                        ItemStack offHand = livingEntity.getEquipment().getItemInOffHand();
                        if (mainHand != null && !mainHand.getType().isAir()) {
                            livingEntity.getWorld().dropItemNaturally(livingEntity.getLocation(), mainHand);
                        }
                        if (offHand != null && !offHand.getType().isAir()) {
                            livingEntity.getWorld().dropItemNaturally(livingEntity.getLocation(), offHand);
                        }
                    }
                    livingEntity.remove();
                }
                Arrow arrow = (Arrow) event.getDamager();

                arrow.remove();
                event.setCancelled(true);
                List<Block> blocks = getMobBoundingBlocks(event.getEntity()
                                                               .getLocation(), (int) Math.round(event.getEntity()
                                                                                                     .getBoundingBox()
                                                                                                     .getWidthX() / 2), (int) Math
                        .round(event.getEntity().getBoundingBox().getHeight() / 2), (int) Math.round(event.getEntity()
                                                                                                          .getBoundingBox()
                                                                                                          .getWidthZ() / 2));
                for (Block block : blocks) {
                    if (block.getType().equals(Material.AIR) || block.isLiquid() || !block.getType().isSolid()) {
                        block.setType(stoneBlockTypes.get(AcuteLoot.random.nextInt(stoneBlockTypes.size())));
                    }
                }
            }
        }
    }

    /**
     * Returns blocks within the bounding box of a mob.
     *
     * @param location location of mob
     * @param xLength length of bounding box x-axis
     * @param yLength length of bounding box y-axis
     * @param zLength length of bounding box z-axis
     * @return list of blocks within bounding box
     */
    public static List<Block> getMobBoundingBlocks(Location location, int xLength, int yLength, int zLength) {
        List<Block> blocks = new ArrayList<>();
        for (int x = location.getBlockX(); x <= location.getBlockX() + xLength; x++) {
            for (int y = location.getBlockY(); y <= location.getBlockY() + yLength; y++) {
                for (int z = location.getBlockZ(); z <= location.getBlockZ() + zLength; z++) {
                    blocks.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }
}
