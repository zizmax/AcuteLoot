package acute.loot;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.graalvm.compiler.nodes.calc.IntegerTestNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeadEyeEffect extends LootSpecialEffect{

    public static HashMap<Player, Integer> deadEyeArrowsShot = new HashMap<>();

    public DeadEyeEffect(String name, int id, List<LootMaterial> validLootMaterials, AcuteLoot plugin) {
            super(name, id, validLootMaterials, plugin);
    }

    @Override
    public void apply(Event origEvent) {
        // -1: In cooldown period
        // -2: Out of arrows
        // -3: No bow
        // -4: No bow and no arrows

        if(origEvent instanceof PlayerInteractEvent) {
            if (plugin.getConfig().getDouble("effects.dead-eye.chance") > 0) {
                Player player = ((PlayerInteractEvent) origEvent).getPlayer();
                ItemStack item = player.getInventory().getItemInMainHand();
                if (deadEyeArrowsShot.get(player) != null && deadEyeArrowsShot.get(player) <= -1) {
                    player.playSound(player.getEyeLocation(), Sound.BLOCK_LANTERN_BREAK, 1, 1);
                    return;
                }

                int deadEyeLength = plugin.getConfig().getInt("effects.dead-eye.effect-time");
                if (item.getType().equals(Material.BOW)) {
                    if (deadEyeArrowsShot.containsKey(player)) {
                        player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .5f, 1);
                        deadEyeArrowsShot.replace(player, deadEyeArrowsShot.get(player) + 1);
                        final List<Location> locations = getLine(player.getEyeLocation(), 30, .3);
                        for (int i = 0; i < locations.size(); i++) {

                            final List<Entity> entities = (List<Entity>) player.getWorld()
                                    .getNearbyEntities(locations.get(i), 0.5, 0.5, 0.5);
                            for (int n = 0; n < entities.size(); n++) {
                                if (entities.get(n).equals(player)) {
                                    entities.remove(entities.get(n));
                                }
                            }

                            if (!locations.get(i).getBlock().getType().isAir() || entities.size() > 0 || i == locations.size() - 1) {
                                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 2);
                                Location location;
                                if (locations.size() == 0 || i == 0) {
                                    location = player.getEyeLocation();
                                } else {
                                    location = locations.get(i - 1);
                                }

                                // Respawn particle every two ticks to keep it visible until Dead Eye is over
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (!player.hasPotionEffect(PotionEffectType.SLOW)) this.cancel();
                                        player.getWorld().spawnParticle(Particle.REDSTONE, location, 1, dustOptions);
                                    }

                                }.runTaskTimer(plugin, 0L, 2L);
                                long timeLeft = 0L;
                                if(player.hasPotionEffect(PotionEffectType.SLOW)) timeLeft =  player.getPotionEffect(PotionEffectType.SLOW).getDuration();
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if ((player.getInventory().contains(Material.ARROW) && player.getInventory().contains(Material.BOW)) || (player.getGameMode().equals(GameMode.CREATIVE) && player.getInventory().contains(Material.BOW))) {
                                            int bowSlot = -1;
                                            for (int i = 0; i < 36; i++) {
                                                if(player.getInventory().getItem(i) != null && player.getInventory().getItem(i).getType().equals(Material.BOW) && player.getInventory().getItem(i).hasItemMeta() && player.getInventory().getItem(i).getItemMeta().hasLore() && player.getInventory().getItem(i).getItemMeta().getLore().get(player.getInventory().getItem(i).getItemMeta().getLore().size() -1).contains("Activated") ){
                                                    bowSlot = i;
                                                    break;
                                                }
                                            }

                                            if(bowSlot == -1){
                                                if(deadEyeArrowsShot.get(player) != -3) {
                                                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED + "No bow"));
                                                    player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                                                }
                                                deadEyeArrowsShot.replace(player, -3);
                                                return;
                                            }

                                            ItemStack bow = player.getInventory().getItem(bowSlot);
                                            ItemMeta bowMeta = bow.getItemMeta();
                                            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                                                int arrowSlot = player.getInventory().first(Material.ARROW);
                                                int arrowAmount = player.getInventory().getItem(arrowSlot).getAmount();
                                                if((!plugin.getConfig().getBoolean("effects.dead-eye.vanilla-enchantments")) || (plugin.getConfig().getBoolean("effects.dead-eye.vanilla-enchantments") && !bow.getEnchantments().containsKey(Enchantment.ARROW_INFINITE))) {
                                                    player.getInventory().getItem(arrowSlot).setAmount(arrowAmount - 1);
                                                }
                                                if (((Damageable) bowMeta).getDamage() > bow.getType().getMaxDurability()) {
                                                    player.getInventory().setBoots(null);
                                                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                                                    deadEyeArrowsShot.replace(player, -3);
                                                    player.getInventory().setItem(bowSlot, null);
                                                    return;
                                                }
                                                ((Damageable) bowMeta).setDamage(((Damageable) bowMeta).getDamage() + plugin.getConfig().getInt("effects.dead-eye.durability-modifier"));
                                                bow.setItemMeta(bowMeta);
                                            }
                                            Vector from = player.getEyeLocation().toVector();
                                            Vector to = location.toVector();
                                            Vector direction = to.subtract(from);
                                            direction.normalize();
                                            direction.multiply(3); // Set speed
                                            //FIXME: "x not finite" error
                                            Arrow arrow;
                                            try{
                                                direction.checkFinite();
                                                arrow = player.launchProjectile(Arrow.class, direction);
                                            }
                                            catch(IllegalArgumentException e){
                                                arrow = player.launchProjectile(Arrow.class, direction);
                                            }
                                            arrow.setMetadata("deadEye", new FixedMetadataValue(plugin, true));
                                            arrow.setCritical(true);
                                            player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_ARROW_SHOOT, 1, 1);
                                            if(plugin.getConfig().getBoolean("effects.dead-eye.vanilla-enchantments")) {
                                                if (bow.getEnchantments().containsKey(Enchantment.ARROW_KNOCKBACK)) {
                                                    arrow.setKnockbackStrength(bow.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK));
                                                }
                                                if (bow.getEnchantments().containsKey(Enchantment.ARROW_FIRE)) {
                                                    arrow.setFireTicks(2000);
                                                }
                                                if (bow.getEnchantments().containsKey(Enchantment.ARROW_DAMAGE)) {
                                                    arrow.setDamage(arrow.getDamage() * (1 + (0.25 * (bow.getEnchantmentLevel(Enchantment.ARROW_DAMAGE) + 1))));
                                                }
                                            }
                                        } else {
                                            if (deadEyeArrowsShot.get(player) >= -1 && player.getInventory().contains(Material.BOW)) {
                                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED + "Out of arrows"));
                                                player.playSound(player.getEyeLocation(), Sound.ENTITY_VILLAGER_WORK_FLETCHER, 1, 1);
                                                deadEyeArrowsShot.replace(player, -2);
                                            }
                                            else if(deadEyeArrowsShot.get(player) >= -1 && player.getInventory().contains(Material.ARROW)) {
                                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED + "No bow"));
                                                player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                                                deadEyeArrowsShot.replace(player, -3);
                                            }
                                            else if (deadEyeArrowsShot.get(player) >= -1 && (!player.getInventory().contains(Material.ARROW) && !player.getInventory().contains(Material.BOW))){
                                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED + "No bow or arrows"));
                                                player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                                                deadEyeArrowsShot.replace(player, -4);
                                            }

                                        }

                                    }

                                }.runTaskLater(plugin, timeLeft + (deadEyeArrowsShot.get(player) * 2));
                                return;

                            }
                        }

                    } else {
                        // Activate Dead Eye
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GOLD + "[" + ChatColor.GREEN + "Dead Eye" + ChatColor.GOLD + "]"));
                        deadEyeArrowsShot.put(player, 0);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, deadEyeLength, 5, true));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, deadEyeLength, 5, true));
                        ItemMeta bowMeta = player.getInventory().getItemInMainHand().getItemMeta();
                        List<String> bowLore = bowMeta.getLore();
                        bowLore.add(ChatColor.GREEN + "Activated");
                        bowMeta.setLore(bowLore);
                        player.getInventory().getItemInMainHand().setItemMeta(bowMeta);
                        player.playSound(player.getEyeLocation(), Sound.AMBIENT_UNDERWATER_LOOP, 2, 1);
                        player.playSound(player.getEyeLocation(), Sound.BLOCK_CAMPFIRE_CRACKLE, 2, 1);
                        List<Entity> entities = player.getNearbyEntities(20, 20, 20);
                        for (Entity entity : entities) {
                            if (entity instanceof LivingEntity) {
                                LivingEntity livingEntity = (LivingEntity) entity;
                                entity.setMetadata("deadEyeSlowness", new FixedMetadataValue(plugin, true));
                                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, deadEyeLength, 3, true));
                                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, deadEyeLength, 3, true));
                            }

                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                // Deactivate Dead Eye
                                List<Entity> entities = player.getNearbyEntities(20, 20, 20);
                                for (Entity entity : entities) {
                                    if (entity instanceof LivingEntity) {
                                        LivingEntity livingEntity = (LivingEntity) entity;
                                        livingEntity.removePotionEffect(PotionEffectType.SLOW);
                                        livingEntity.removePotionEffect(PotionEffectType.SLOW_FALLING);
                                    }

                                }
                                player.stopSound(Sound.AMBIENT_UNDERWATER_LOOP);
                                player.stopSound(Sound.BLOCK_CAMPFIRE_CRACKLE);
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GOLD + "[" + ChatColor.RED + "Dead Eye" + ChatColor.GOLD + "]"));
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        // Remove lore that marks bow as Dead Eye "active" after all arrows have been shot
                                        int bowSlot = -1;
                                        for (int i = 0; i < 36; i++) {
                                            if(player.getInventory().getItem(i) != null && player.getInventory().getItem(i).getType().equals(Material.BOW) && player.getInventory().getItem(i).hasItemMeta() && player.getInventory().getItem(i).getItemMeta().hasLore() && player.getInventory().getItem(i).getItemMeta().getLore().get(player.getInventory().getItem(i).getItemMeta().getLore().size() -1).contains("Activated") ){
                                                bowSlot = i;
                                                break;
                                            }
                                        }
                                        if(bowSlot != -1) {
                                            ItemMeta bowMeta = player.getInventory().getItem(bowSlot).getItemMeta();
                                            List<String> bowLore = bowMeta.getLore();
                                            bowLore.remove(bowLore.size() - 1);
                                            bowMeta.setLore(bowLore);
                                            player.getInventory().getItem(bowSlot).setItemMeta(bowMeta);
                                        }
                                    }
                                }.runTaskLater(plugin, deadEyeArrowsShot.get(player) * 2);

                                deadEyeArrowsShot.replace(player, -1);
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        // End Dead Eye cooldown
                                        deadEyeArrowsShot.remove(player);
                                    }

                                }.runTaskLater(plugin, plugin.getConfig().getInt("effects.dead-eye.cooldown-time"));
                            }

                        }.runTaskLater(plugin, deadEyeLength);
                    }
                }
            }

        }

    }

    private List<Location> getLine(Location from, double distance, double addition) {
        List<Location> locations = new ArrayList<>();
        final Vector direction = from.getDirection(); // End - Begin | length to 1

        for (double d = addition; d < distance; d += addition) {
            locations.add(from.clone().add(direction.clone().normalize().multiply(d)));
        }
        return locations;
    }
}
