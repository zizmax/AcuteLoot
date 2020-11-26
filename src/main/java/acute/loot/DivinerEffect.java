package acute.loot;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class DivinerEffect extends LootSpecialEffect {

    public DivinerEffect(String name, String ns, int id, List<LootMaterial> validLootMaterials, AcuteLoot plugin) {
        super(name, ns, id, validLootMaterials, plugin);
    }

    @Override
    public void apply(Event origEvent) {
        if (origEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) origEvent;
            event.setCancelled(true);
            if(event.getDamager() instanceof Player) {
                printStats(analyzeEntity(event.getEntity()), (Player) event.getDamager());
            }
        }
        if (origEvent instanceof PlayerInteractEvent) {
            PlayerInteractEvent event = (PlayerInteractEvent) origEvent;
            event.setCancelled(true);
            Player player = event.getPlayer();
            int max_beam_distance = plugin.getConfig().getInt("effects.weapons.max-distance");
            double beam_segment_length = 0.3;
            final List<Location> locations = Util.getLine(player.getEyeLocation(), max_beam_distance, beam_segment_length);
            for (Location location : locations) {
                final List<Entity> entities = (List<Entity>) player.getWorld().getNearbyEntities(location, 0.2, 0.2, 0.2);
                for (int n = 0; n < entities.size(); n++) {
                    if (entities.get(n).equals(player)) {
                        entities.remove(entities.get(n));
                    }
                }

                if (!location.getBlock().getType().isAir() && location.getBlock().getType() != Material.WATER) {
                    printStats(analyzeBlock(location.getBlock()), player);
                    break;
                }

                if (entities.size() > 0) {
                    printStats(analyzeEntity(entities.get(0)), player);
                    break;
                }
            }
        }

    }

    public ArrayList<String[]> analyzeBlock(Block block) {
        ArrayList<String[]> stats = new ArrayList<>();
        stats.add(new String[]{"Material", block.getType().toString().toLowerCase()});
        stats.add(new String[]{"Biome", block.getBiome().toString().toLowerCase()});
        stats.add(new String[]{"Power", String.valueOf(block.getBlockPower())});
        stats.add(new String[]{"Light", String.valueOf(block.getLightLevel())});
        stats.add(new String[]{"Data", block.getBlockData().getAsString().toLowerCase()});
        stats.add(new String[]{"Temp", String.format("%.3f", block.getTemperature())});
        stats.add(new String[]{"Location", getLocationString(block.getLocation())});


        return stats;
    }
    public ArrayList<String[]> analyzeEntity(Entity entity) {
        ArrayList<String[]> stats = new ArrayList<>();
        stats.add(new String[]{"Type", entity.getType().toString().toLowerCase()});
        stats.add(new String[]{"Location", getLocationString(entity.getLocation())});
        stats.add(new String[]{"Ticks lived", String.valueOf(entity.getTicksLived())});
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            stats.add(new String[]{"Health", String.valueOf(livingEntity.getHealth())});
            if (livingEntity.getType() == EntityType.HORSE || livingEntity.getType() == EntityType.ZOMBIE_HORSE
                    || livingEntity.getType() == EntityType.SKELETON_HORSE) {
                for (String[] stat : getHorseStats(livingEntity)){
                    stats.add(stat);
                }
            }
        }
        return stats;
    }

    private String getLocationString(Location location){
        return String.format("X: %.3f, Y: %.3f, Z: %.3f", location.getX(), location.getY(), location.getZ());
    }


    private ArrayList<String[]> getHorseStats(LivingEntity entity) {
        double speed = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue() * 42.157787584;
        double scaled = scale(speed, 4.74, 14.23, 1.0, 10.0);
        double jumpStrength = entity.getAttribute(Attribute.HORSE_JUMP_STRENGTH).getValue();
        double jumpHeight = -0.1817584952 * Math.pow(jumpStrength, 3) + 3.689713992
                * Math.pow(jumpStrength, 2) + 2.128599134
                * jumpStrength - 0.343930367;
        ArrayList<String[]> stats = new ArrayList<>();
        stats.add(new String[]{"Max Health", String.format("%.3f", entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())});
        stats.add(new String[]{"Speed (bps)", String.format("%.3f", speed)});
        stats.add(new String[]{"Speed (scaled)", String.format("%.3f", scaled)});
        stats.add(new String[]{"Jump Height", String.format("%.3f", jumpHeight)});

        return stats;
    }

    private static double scale(final double valueIn, final double baseMin, final double baseMax,
                                final double limitMin, final double limitMax) {
        return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
    }

    private void printStats(ArrayList<String[]> stats, Player player){
        player.sendMessage(ChatColor.GRAY + "============== [Diviner] =============");
        for (String[] stat : stats){
            player.sendMessage(ChatColor.GREEN + stat[0] + ChatColor.GRAY + ": " + ChatColor.GOLD + stat[1]);
        }
        player.sendMessage(ChatColor.GRAY + "====================================");

    }

}
