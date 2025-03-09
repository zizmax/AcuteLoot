package acute.loot;

import com.cryptomorin.xseries.XPotion;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * EventListeners related to effects. Most of these are passthroughs.
 */
public class EffectEventListener implements Listener {

    private final AcuteLoot plugin;

    public EffectEventListener(AcuteLoot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerHitMob(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            if (DeadEyeEffect.deadEyeArrowsShot.containsKey(event.getDamager())) {
                // Cancel player's attacks while in Dead Eye
                event.setCancelled(true);
            }
            applyEventWithPlayer(event, (Player) event.getDamager());
        }

        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                if (plugin.effectsEnabled(arrow.getWorld()) && ((Player) arrow.getShooter()).getInventory()
                                                                                                     .getItemInMainHand()
                                                                                                     .getType() == Material.BOW) {
                    if (event.getEntity() instanceof LivingEntity) {
                        if (arrow.hasMetadata("deadEye")) {
                            LivingEntity livingEntity = (LivingEntity) event.getEntity();
                            livingEntity.setMaximumNoDamageTicks(0);
                            livingEntity.setNoDamageTicks(0);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    livingEntity.setNoDamageTicks(0);
                                }
                            }.runTaskLater(plugin, 1L);
                            // Set maximumNoDamageTicks back to default after all arrows have hit
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    livingEntity.setMaximumNoDamageTicks(20);
                                }

                            }.runTaskLater(plugin, 20L);
                        }

                        applyEventToItem(event, ((Player) arrow.getShooter()).getInventory().getItemInMainHand(), arrow.getWorld());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().hasMetadata("turnedToStone")) {
            String message = plugin.getConfig().getString("effects.medusa.death-message");
            event.setDeathMessage(event.getEntity().getDisplayName() + " " + message);
            event.getEntity().removeMetadata("turnedToStone", plugin);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
            applyPlayerEvent(event);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isOnGround() && (event.getFrom().getBlockX() != (event.getTo().getBlockX()) ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ() ||
            event.getFrom().getBlockY() != event.getTo().getBlockY())) {
            applyPlayerEvent(event);
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            applyEventWithPlayer(event, (Player) event.getEntity());
        }

        if (event.getEntity() instanceof Skeleton &&
                event.getEntity().hasPotionEffect(XPotion.SLOWNESS.getPotionEffectType()) &&
                event.getEntity().hasMetadata("deadEyeSlowness")) {
            // It ruins the Dead Eye slo-mo effect when skeletons can shoot you during it
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (DeadEyeEffect.deadEyeArrowsShot.get(player) != null && event.getItemDrop()
                                                                        .getItemStack()
                                                                        .getType()
                                                                        .equals(Material.BOW) && event.getItemDrop()
                                                                                                      .getItemStack()
                                                                                                      .hasItemMeta() && event
                .getItemDrop()
                .getItemStack()
                .getItemMeta()
                .hasLore()) {
            ItemMeta bowMeta = event.getItemDrop().getItemStack().getItemMeta();
            List<String> bowLore = bowMeta.getLore();
            if (bowLore.get(bowLore.size() - 1).contains("Activated")) {
                bowLore.remove(bowLore.size() - 1);
                bowMeta.setLore(bowLore);
                event.getItemDrop().getItemStack().setItemMeta(bowMeta);
            }
        }
    }

    //XP Boost Effect
    @EventHandler
    public void onExpGain(PlayerExpChangeEvent event) {
        applyPlayerEvent(event);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        applyPlayerEvent(event);
        /*
        // May be used in the future to prevent Dead Eye from activating while viewing inventory
        // This "bug" could potentially be fixed by putting the entire Dead Eye logic into a Runnable that
        // executes in the next tick instead of the current tick. This would allow this event to mark that
        // PlayerInteractEvent was actually associated with dropping an item out of inventory and NOT a true
        // interaction. This event is called after PlayerInteractEvent so without delaying it by a tick,
        // setting the association won't do anything.

        // Example of what this event could do to mark its association with PlayerInteractEvent
        deadEyeArrowsShot.putIfAbsent((Player) event.getPlayer(), -3);
         */
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            applyEventWithPlayer(event, (Player) event.getEntity());
        }
    }

    @EventHandler
    public void onPlayerStatisticIncrement(PlayerStatisticIncrementEvent event) {
        applyPlayerEvent(event);
    }

    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        applyPlayerEvent(event);
    }

    @EventHandler
    public void onPlayerEnterPortal(PlayerPortalEvent event) {
        applyPlayerEvent(event);
    }

    @EventHandler
    public void onPlayerHoldItem(PlayerItemHeldEvent event) {
        applyPlayerEvent(event);
    }

    @EventHandler
    public void onPlayerClickEvent(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            applyEventWithPlayer(event, (Player) event.getWhoClicked());
        }
    }

    private void applyPlayerEvent(final PlayerEvent event) {
        applyEventWithPlayer(event, event.getPlayer());
    }

    private void applyEventWithPlayer(final Event event, final Player player) {
        applyEventToItem(event, player.getInventory().getItemInMainHand(), player.getWorld());
        applyEventToItem(event, player.getInventory().getItemInOffHand(), player.getWorld());
        applyEventToItem(event, player.getInventory().getHelmet(), player.getWorld());
        applyEventToItem(event, player.getInventory().getChestplate(), player.getWorld());
        applyEventToItem(event, player.getInventory().getLeggings(), player.getWorld());
        applyEventToItem(event, player.getInventory().getBoots(), player.getWorld());
    }

    private void applyEventToItem(final Event event, final ItemStack item, final World world) {
        if (!plugin.effectsEnabled(world)) {
            return;
        }
        String lootCode = plugin.getLootCode(item);
        if (lootCode != null) {
            new LootItem(lootCode).getEffects().forEach(e -> e.apply(event));
        }
    }

}
