package acute.loot;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class LootWell {

    private final AcuteLoot plugin;

    public LootWell(AcuteLoot plugin) { this.plugin = plugin; }

    public void onWish(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        if (plugin.debug) {
            if (plugin.getConfig().getBoolean("loot-well.enabled") && player.hasPermission("acuteloot.use-well")) {
                new BukkitRunnable() {
                    int i = 0;

                    @Override
                    public void run() {
                        if (i >= 6 || !event.getItemDrop().isValid() || event.getItemDrop() == null) {
                            cancel();
                            return;
                        }
                        Location well = new Location(plugin.getServer().getWorld(plugin.getConfig().getString("loot-well.world")), plugin.getConfig().getDouble("loot-well.x"), plugin.getConfig().getDouble("loot-well.y"), plugin.getConfig().getDouble("loot-well.z"));
                        if (event.getItemDrop().getWorld().equals(well.getWorld())) {
                            if (event.getItemDrop().isOnGround() || event.getItemDrop().isInWater()) {
                                if (event.getItemDrop().getLocation().distance(well) < plugin.getConfig().getDouble("loot-well.max-distance")) {
                                    event.getItemDrop().setItemStack(new ItemStack(Material.DIAMOND, event.getItemDrop().getItemStack().getAmount()));
                                    event.getItemDrop().setVelocity((player.getEyeLocation().add(0, 10, 0).toVector().subtract(event.getItemDrop().getLocation().toVector())).normalize().multiply(Double.parseDouble(player.getInventory().getItemInMainHand().getItemMeta().getDisplayName())));
                                    player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, well.add(0, .25, 0), 100);
                                    player.getWorld().playSound(well, Sound.ENTITY_GENERIC_EXPLODE, 1f, 2.5f);
                                }
                            }
                        }
                        i += 1;
                    }
                }.runTaskTimer(plugin, 20, 10);
            }
        }
    }
}
