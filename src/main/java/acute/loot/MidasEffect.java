package acute.loot;

import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Effect to turn things to gold.
 */
public class MidasEffect extends AcuteLootSpecialEffect {
    //TODO: Config option to only send block change packets, not actually change the blocks
    //FIXME: Does calling setType() work as intended for all types of blocks?
    private final AcuteLoot plugin;

    public MidasEffect(String name, int id, List<LootMaterial> validLootMaterials, AcuteLoot plugin) {
        super(name, id, validLootMaterials, plugin);
        this.plugin = plugin;
    }

    @Override
    public void applyEffect(Event origEvent) {
        if (origEvent instanceof PlayerMoveEvent) {


            PlayerMoveEvent event = (PlayerMoveEvent) origEvent;
            Player player = event.getPlayer();
            if (plugin.getConfig().getBoolean("effects.midas.affect-passive-blocks")) {
                if (!player.getLocation().subtract(0, 1, 0).getBlock().isEmpty()) {
                    player.getLocation().subtract(0, 1, 0).getBlock().setType(Material.GOLD_BLOCK);
                }
            }
            List<Entity> entities = player.getNearbyEntities(.5, .5, .5);
            if (entities.size() >= 1) {
                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity) {
                        entity.playEffect(EntityEffect.ENTITY_POOF);
                        entity.getWorld().dropItemNaturally(((LivingEntity) entity).getEyeLocation(), getGoldItemStack());
                        entity.remove();
                    }
                }
            }
        }

        if (origEvent instanceof PlayerItemHeldEvent) {
            PlayerItemHeldEvent event = (PlayerItemHeldEvent) origEvent;
            Player player = event.getPlayer();
            if (player.getInventory().getItem(event.getNewSlot()) != null &&
                    !player.getInventory().getItem(event.getNewSlot()).getType().isAir()) {
                int amount = player.getInventory().getItem(event.getNewSlot()).getAmount();
                ItemStack itemStack = getGoldItemStack();
                itemStack.setAmount(amount);
                player.getInventory().setItem(event.getNewSlot(), itemStack);
            }
        }

        if (origEvent instanceof PlayerDropItemEvent) {
            PlayerDropItemEvent event = (PlayerDropItemEvent) origEvent;
            ItemStack itemStack = getGoldItemStack();
            itemStack.setAmount(event.getItemDrop().getItemStack().getAmount());
            event.getItemDrop().setItemStack(itemStack);
        }

        if (origEvent instanceof InventoryClickEvent) {
            InventoryClickEvent event = (InventoryClickEvent) origEvent;
            ItemStack itemStack = getGoldItemStack();
            itemStack.setAmount(event.getCurrentItem().getAmount());
            event.setCurrentItem(itemStack);
        }

        if (origEvent instanceof PlayerInteractEvent) {
            PlayerInteractEvent event = (PlayerInteractEvent) origEvent;
            event.setCancelled(true);
            if (event.getClickedBlock() != null && !event.getClickedBlock().getType().isAir()) {
                event.getClickedBlock().setType(Material.GOLD_BLOCK);
            }
        }

        if (origEvent instanceof PlayerPortalEvent) {
            PlayerPortalEvent event = (PlayerPortalEvent) origEvent;
            if (plugin.getConfig().getBoolean("effects.midas.affect-passive-blocks")) {
                event.setCancelled(true);
                event.getFrom().getBlock().setType(Material.GOLD_BLOCK);
            }
        }

        if (origEvent instanceof PlayerBedEnterEvent) {
            PlayerBedEnterEvent event = (PlayerBedEnterEvent) origEvent;
            if (plugin.getConfig().getBoolean("effects.midas.affect-passive-blocks")) {
                event.setCancelled(true);
                event.getBed().setType(Material.GOLD_BLOCK);
            }
        }

        if (origEvent instanceof EntityPickupItemEvent) {
            EntityPickupItemEvent event = (EntityPickupItemEvent) origEvent;
            ItemStack itemStack = getGoldItemStack();
            itemStack.setAmount(event.getItem().getItemStack().getAmount());
            event.getItem().setItemStack(getGoldItemStack());
        }

        if (origEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) origEvent;
            event.getEntity().playEffect(EntityEffect.ENTITY_POOF);
            event.getEntity().getWorld().dropItemNaturally(((LivingEntity) event.getEntity()).getEyeLocation(), getGoldItemStack());
            event.getEntity().remove();
        }
    }

    private ItemStack getGoldItemStack() {
        Material[] materials = {Material.GOLD_NUGGET, Material.GOLD_INGOT};
        return new ItemStack(materials[AcuteLoot.random.nextInt(materials.length)], 1);
    }

}
