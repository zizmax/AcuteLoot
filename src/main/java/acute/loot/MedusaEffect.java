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

import java.util.ArrayList;
import java.util.List;

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
                Arrow arrow = (Arrow) event.getDamager();
                event.getEntity().playEffect(EntityEffect.ENTITY_POOF);
                World world = event.getEntity().getWorld();
                world.playSound(event.getEntity().getLocation(), Sound.BLOCK_STONE_PLACE, 2, 1);
                Material[] stoneBlockTypes;
                if (world.getEnvironment().equals(World.Environment.NETHER)) {
                    if (AcuteLoot.serverVersion > 15) {
                        stoneBlockTypes = new Material[]{Material.BLACKSTONE, Material.CRACKED_POLISHED_BLACKSTONE_BRICKS, Material.POLISHED_BLACKSTONE_BRICKS, Material.POLISHED_BLACKSTONE};
                    } else {
                        stoneBlockTypes = new Material[]{Material.GRAVEL, Material.SOUL_SAND};
                    }

                } else if (world.getEnvironment().equals(World.Environment.THE_END)) {
                    stoneBlockTypes = new Material[]{Material.END_STONE};
                } else {
                    stoneBlockTypes = new Material[]{Material.COBBLESTONE, Material.MOSSY_COBBLESTONE};
                }

                if (event.getEntity() instanceof Player) {
                    Player player = (Player) event.getEntity();
                    if (plugin.getConfig().getBoolean("effects.medusa.affect-players")) {
                        player.setMetadata("turnedToStone", new FixedMetadataValue(plugin, true));
                        player.setHealth(0);
                    } else return;
                } else {
                    if (plugin.getConfig().getBoolean("effects.medusa.drop-loot")) {
                        for (ItemStack item : livingEntity.getEquipment().getArmorContents()) {
                            if (item != null && !item.getType().isAir())
                                livingEntity.getWorld().dropItemNaturally(livingEntity.getLocation(), item);
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
                        block.setType(stoneBlockTypes[AcuteLoot.random.nextInt(stoneBlockTypes.length)]);
                    }
                }
            }
        }
    }

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
