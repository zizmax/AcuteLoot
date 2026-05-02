package acute.loot;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Deluminator Effect.
 * Toggles lights in a 3D radius.
 */
public class DeluminatorEffect extends AcuteLootSpecialEffect {

    private static final NamespacedKey DELUMINATOR_KEY = new NamespacedKey(AcuteLoot.getPlugin(AcuteLoot.class), "deluminator_data");
    private static final int RADIUS = 15;

    // Custom material lookups per user preference
    // Using string matching to avoid compilation errors with XMaterial versions
    private static final Material CHAIN = XMaterial.matchXMaterial("CHAIN").map(XMaterial::parseMaterial).orElse(null);
    private static final Material ORANGE_GLASS = XMaterial.matchXMaterial("ORANGE_STAINED_GLASS").map(XMaterial::parseMaterial).orElse(null);
    private static final Material GLASS = XMaterial.matchXMaterial("GLASS").map(XMaterial::parseMaterial).orElse(null);
    
    private static final Material SOUL_TORCH = Material.getMaterial("SOUL_TORCH");
    private static final Material SOUL_WALL_TORCH = Material.getMaterial("SOUL_WALL_TORCH");
    private static final Material SOUL_LANTERN = Material.getMaterial("SOUL_LANTERN");

    public DeluminatorEffect(String name, int id, List<LootMaterial> validMaterials, AcuteLoot plugin) {
        super(name, id, validMaterials, plugin);
    }

    @Override
    protected void applyEffect(Event event) {
        if (!(event instanceof PlayerInteractEvent)) return;
        PlayerInteractEvent e = (PlayerInteractEvent) event;
        
        if (!e.getAction().name().contains("LEFT_CLICK")) return;
        if (!e.hasItem()) return;

        Player player = e.getPlayer();
        if (!onItem(e.getItem())) return;

        e.setCancelled(true);
        toggleLights(player);
    }

    private void toggleLights(Player player) {
        List<Block> restored = restoreFromPDC(player);
        
        if (!restored.isEmpty()) {
            XSound.BLOCK_BEACON_ACTIVATE.play(player, 1.0f, 1.5f);
            return;
        }

        List<Block> absorbed = absorbLights(player);
        if (!absorbed.isEmpty()) {
            XSound.ENTITY_ILLUSIONER_CAST_SPELL.play(player, 1.0f, 0.5f);
        } else {
            XSound.ITEM_FLINTANDSTEEL_USE.play(player, 1.0f, 1.0f);
        }
    }

    private List<Block> absorbLights(Player player) {
        List<Block> affected = new ArrayList<>();
        Map<Chunk, List<String>> chunkUpdates = new HashMap<>();

        for (Block block : getNearbyBlocks(player.getLocation(), RADIUS)) {
            if (!canBuild(player, block)) continue;
            
            boolean needsPDC = requiresReplacement(block);
            if (needsPDC && !hasChunkPDC(block.getChunk())) continue;

            if (isLightSource(block)) {
                String originalData = saveBlockState(block);
                
                if (turnOff(block)) {
                    affected.add(block);
                    if (hasChunkPDC(block.getChunk())) {
                        chunkUpdates.computeIfAbsent(block.getChunk(), k -> new ArrayList<>()).add(originalData);
                    }
                    playParticleTrail(block.getLocation().add(0.5, 0.5, 0.5), player.getEyeLocation().subtract(0, 0.3, 0));
                }
            }
        }

        saveToPDC(chunkUpdates);
        return affected;
    }

    private boolean turnOff(Block block) {
        Material type = block.getType();
        BlockData data = block.getBlockData();
        String name = type.name();

        // 1. Toggleable Lightables (Campfires, Candles, Lamps)
        if (data instanceof Lightable && isToggleableLightSource(type)) {
            Lightable lightable = (Lightable) data;
            if (lightable.isLit()) {
                lightable.setLit(false);
                block.setBlockData(lightable);
                return true;
            }
            return false;
        }

        // 2. Replacement Logic
        
        // Torches (Generic Check) -> Redstone Torches
        if (name.contains("TORCH") && !name.contains("REDSTONE")) {
             if (name.contains("WALL")) {
                 block.setType(Material.REDSTONE_WALL_TORCH);
             } else {
                 block.setType(Material.REDSTONE_TORCH);
             }
             if (block.getBlockData() instanceof Lightable) {
                 Lightable l = (Lightable) block.getBlockData();
                 l.setLit(false);
                 block.setBlockData(l);
             }
             return true;
        }
        
        // Lanterns (Generic Check) -> Chains
        if (name.contains("LANTERN") && !name.contains("SEA") && !name.contains("JACK")) {
            block.setType(XMaterial.IRON_CHAIN.get());
            return true;
        }

        // Jack o Lantern -> Carved Pumpkin
        if (type == Material.JACK_O_LANTERN) {
            BlockData old = block.getBlockData();
            block.setType(Material.CARVED_PUMPKIN);
            if (old instanceof Directional && block.getBlockData() instanceof Directional) {
                Directional d = (Directional) block.getBlockData();
                d.setFacing(((Directional) old).getFacing());
                block.setBlockData(d);
            }
            return true;
        }
        
        // Glowstone/Sea Lantern/Shroomlight -> Orange Glass
        if (type == Material.GLOWSTONE || name.equals("SEA_LANTERN") || name.equals("SHROOMLIGHT")) {
            block.setType(Material.ORANGE_STAINED_GLASS);
            return true;
        }

        return false;
    }

    private List<Block> restoreFromPDC(Player player) {
        List<Block> restored = new ArrayList<>();
        Set<Chunk> loadedChunks = new HashSet<>();
        int chunkRadius = (RADIUS / 16) + 1;
        int px = player.getLocation().getBlockX() >> 4;
        int pz = player.getLocation().getBlockZ() >> 4;
        
        for (int x = px - chunkRadius; x <= px + chunkRadius; x++) {
            for (int z = pz - chunkRadius; z <= pz + chunkRadius; z++) {
                if (player.getWorld().isChunkLoaded(x, z)) {
                    loadedChunks.add(player.getWorld().getChunkAt(x, z));
                }
            }
        }

        for (Chunk chunk : loadedChunks) {
            if (!hasChunkPDC(chunk)) continue;
            List<String> data = loadFromPDC(chunk);
            if (data == null || data.isEmpty()) continue;

            List<String> remaining = new ArrayList<>();
            boolean changed = false;

            for (String entry : data) {
                try {
                    String[] parts = entry.split(":");
                    String[] coords = parts[0].split(",");
                    int bx = Integer.parseInt(coords[0]);
                    int by = Integer.parseInt(coords[1]);
                    int bz = Integer.parseInt(coords[2]);
                    Material mat = Material.matchMaterial(parts[1]);

                    Block block = chunk.getWorld().getBlockAt(bx, by, bz);
                    
                    if (block.getWorld().equals(player.getWorld()) && 
                        block.getLocation().distanceSquared(player.getLocation()) <= RADIUS * RADIUS) {
                        
                        if (canRestore(block)) {
                            if (mat != null) {
                                BlockData oldData = block.getBlockData();
                                block.setType(mat);
                                if (oldData instanceof Directional && block.getBlockData() instanceof Directional) {
                                    Directional d = (Directional) block.getBlockData();
                                    d.setFacing(((Directional) oldData).getFacing());
                                    block.setBlockData(d);
                                }
                                if (block.getBlockData() instanceof Lightable) {
                                    Lightable l = (Lightable) block.getBlockData();
                                    l.setLit(true);
                                    block.setBlockData(l);
                                }
                            }
                            restored.add(block);
                            changed = true;
                            playParticleTrail(player.getEyeLocation().subtract(0, 0.3, 0), block.getLocation().add(0.5, 0.5, 0.5));
                        } else {
                            changed = true; 
                        }
                    } else {
                        remaining.add(entry);
                    }
                } catch (Exception ex) {
                    changed = true;
                }
            }
            if (changed) updatePDC(chunk, remaining);
        }
        return restored;
    }

    private boolean hasChunkPDC(Chunk chunk) {
        return chunk instanceof PersistentDataHolder;
    }

    private boolean requiresReplacement(Block block) {
        Material type = block.getType();
        String name = type.name();
        if (name.contains("TORCH") && !name.contains("REDSTONE")) return true;
        if (name.contains("LANTERN") && !name.contains("SEA") && !name.contains("JACK")) return true;
        return type == Material.GLOWSTONE || type == Material.JACK_O_LANTERN || name.equals("SEA_LANTERN") || name.equals("SHROOMLIGHT");
    }

    private boolean isLightSource(Block block) {
        Material type = block.getType();
        BlockData data = block.getBlockData();
        String name = type.name();

        if (data instanceof Lightable) {
            if (isToggleableLightSource(type)) {
                return ((Lightable) data).isLit();
            }
        }
        return requiresReplacement(block);
    }

    private boolean isToggleableLightSource(Material material) {
        String name = material.name();
        return name.contains("CAMPFIRE") || name.contains("CANDLE") || name.contains("LAMP");
    }

    private boolean canRestore(Block block) {
        Material type = block.getType();
        if (type == Material.AIR || type == Material.ORANGE_STAINED_GLASS || type == Material.CARVED_PUMPKIN || type == XMaterial.IRON_CHAIN.get()) return true;
        if (type.name().contains("REDSTONE_TORCH")) return true;
        if (block.getBlockData() instanceof Lightable) {
            return !((Lightable) block.getBlockData()).isLit();
        }
        return false;
    }

    private String saveBlockState(Block block) {
        return block.getX() + "," + block.getY() + "," + block.getZ() + ":" + block.getType().name();
    }

    private List<Block> getNearbyBlocks(Location loc, int r) {
        List<Block> blocks = new ArrayList<>();
        int bx = loc.getBlockX();
        int by = loc.getBlockY();
        int bz = loc.getBlockZ();
        for (int x = bx - r; x <= bx + r; x++) {
            for (int y = by - r; y <= by + r; y++) {
                for (int z = bz - r; z <= bz + r; z++) {
                    if ((bx - x) * (bx - x) + (by - y) * (by - y) + (bz - z) * (bz - z) <= r * r) {
                        blocks.add(loc.getWorld().getBlockAt(x, y, z));
                    }
                }
            }
        }
        return blocks;
    }

    private boolean canBuild(Player player, Block block) {
        BlockBreakEvent event = new BlockBreakEvent(block, player);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }
    
    private void saveToPDC(Map<Chunk, List<String>> updates) {
        for (Map.Entry<Chunk, List<String>> entry : updates.entrySet()) {
            Chunk chunk = entry.getKey();
            List<String> current = loadFromPDC(chunk);
            if (current == null) current = new ArrayList<>();
            current.addAll(entry.getValue());
            updatePDC(chunk, current);
        }
    }

    private List<String> loadFromPDC(Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        String raw = pdc.get(DELUMINATOR_KEY, PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) return null;
        return new ArrayList<>(Arrays.asList(raw.split(";")));
    }

    private void updatePDC(Chunk chunk, List<String> data) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        if (data == null || data.isEmpty()) {
            pdc.remove(DELUMINATOR_KEY);
        } else {
            pdc.set(DELUMINATOR_KEY, PersistentDataType.STRING, String.join(";", data));
        }
    }
    
    private void playParticleTrail(Location start, Location end) {
        new BukkitRunnable() {
            double t = 0;
            double dist = start.distance(end);
            Vector dir = end.toVector().subtract(start.toVector()).normalize();

            @Override
            public void run() {
                t += 1.5;
                if (t >= dist) {
                    this.cancel();
                    return;
                }
                Location point = start.clone().add(dir.clone().multiply(t));
                ParticleDisplay.of(XParticle.FLAME.get()).spawn(point);
            }
        }.runTaskTimer(plugin, 0, 2);
    }
}
