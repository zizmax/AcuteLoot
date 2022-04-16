package acute.loot;

import acute.loot.generator.LootItemGenerator;
import com.github.unldenis.hologram.Hologram;
import com.github.unldenis.hologram.HologramPool;
import com.github.unldenis.hologram.animation.Animation;
import com.github.unldenis.hologram.event.PlayerHologramHideEvent;
import com.github.unldenis.hologram.event.PlayerHologramShowEvent;
import com.github.unldenis.hologram.line.ItemLine;
import com.github.unldenis.hologram.line.TextLine;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class LootHologram implements Listener {

    private final AcuteLoot plugin;
    private final HologramPool hologramPool;

    public ArrayList<Hologram> holograms = new ArrayList<>();

    /**
     * @param plugin The plugin which uses the lib
     */
    public LootHologram(AcuteLoot plugin) {
        this.plugin = plugin;
        this.hologramPool = new HologramPool(plugin, 70);
    }

    /**
     * Appends a new Hologram to the pool.
     *
     * @param location  The location the Hologram will be spawned at
     */
    public void appendHOLO(Location location, ItemStack item) {
        // building the NPC
        LootItemGenerator generator = plugin.lootGenerator;

        String name = Util.rollName(item,
                generator.generate(AcuteLoot.random.nextDouble(), LootMaterial.lootMaterialForMaterial(item.getType())).rarity(),
                plugin.nameGeneratorNames.get("prefixSuffixGenerator"));
        Hologram hologram = Hologram.builder()
                .location(location)
                .addLine(name)
                .addLine(item)
                .build(hologramPool);

        hologram.getLines().get(1).setAnimation(Animation.CIRCLE);
        // simple changing animating block and text
        //timingBlock(hologram);
    }

    private final static Material[] materials = new Material[] { Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK};

    /**
     * Update the block and the first line of text of the hologram
     * @param hologram The hologram to update
     */
    private void timingBlock(Hologram hologram) {
        new BukkitRunnable() {
            int j=1;
            final TextLine firstLine = (TextLine) hologram.getLines().get(0);
            final ItemLine itemLine = (ItemLine) hologram.getLines().get(3);
            @Override
            public void run() {
                if(j==materials.length) j=0;
                firstLine.set(String.valueOf(j));
                itemLine.set(new ItemStack(materials[j++]));
            }
        }
                .runTaskTimer(plugin, 30L, 30L);
    }

    /**
     * Doing something when a Hologram is shown for a certain player.
     * @param event The event instance
     */
    @EventHandler
    public void onHologramShow(PlayerHologramShowEvent event) {
        Hologram holo = event.getHologram();
        Player player = event.getPlayer();
        player.sendMessage("showing!");

    }

    /**
     * Doing something when a Hologram is hidden for a certain player.
     * @param event The event instance
     */
    @EventHandler
    public void onHologramHide(PlayerHologramHideEvent event) {
        Hologram holo = event.getHologram();
        Player player = event.getPlayer();
        player.sendMessage("hiding!");
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        for (Hologram hologram:holograms) {
            hologram.isShownFor(event.getPlayer());
        }
        Player player = event.getPlayer();
        player.sendMessage("hiding!");
    }
}
