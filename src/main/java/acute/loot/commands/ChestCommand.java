package acute.loot.commands;

import acute.loot.AcuteLoot;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

public class ChestCommand extends AcuteLootCommand<Player> {

    public ChestCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    @Override
    protected void doHandle(Player sender, String[] args) {
        int numFoundChests = 0;
        NamespacedKey key = new NamespacedKey(plugin(), "chestMetadataKey");
        // chestMetadataCode Version 1.0 = "1.0:currentTimeMillis():refillCooldown (minutes)"
        // i.e. "1.0:1606604412:90"
        String version = "1.0";
        String currentTime = String.valueOf(System.currentTimeMillis());
        String refillCooldown = "-1";
        for (BlockState tileEntity : sender.getLocation().getChunk().getTileEntities()){
            if (tileEntity instanceof Chest){
                if (((Chest) tileEntity).getPersistentDataContainer().has(key, PersistentDataType.STRING)){
                    String chestMetadata = ((Chest) tileEntity).getPersistentDataContainer().get(key, PersistentDataType.STRING);
                }
                else {
                    if (args.length >= 2){
                        try {
                            Integer.parseInt(args[1]);
                            refillCooldown = args[1];
                        }
                        catch (NumberFormatException e){
                            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Refill cooldown (minutes) must be an integer");
                            return;
                        }
                    }
                    String chestMetadataCode = String.format("%s:%s:%s", version, currentTime, refillCooldown);
                    if (plugin().debug) sender.sendMessage("Code: " + chestMetadataCode);
                    ((Chest) tileEntity).getPersistentDataContainer().set(key, PersistentDataType.STRING, chestMetadataCode);
                    tileEntity.update();
                    BlockState state = tileEntity.getBlock().getState();

                    if (state instanceof Chest) {
                        Chest chest = (Chest) state;
                        Inventory inventory = chest.getInventory();
                        if (inventory instanceof DoubleChestInventory) {
                            //TODO: Check if it's a double chest
                            //TODO: Adjust numFoundChests accordingly
                            //DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                        }

                    }
                    tileEntity.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, tileEntity.getBlock().getLocation().add(.5, 1, .5), 100);
                    numFoundChests++;
                }
            }
        }
        //TODO: Play sound
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.YELLOW + "============= " + ChatColor.GRAY + "Chest Creator"
                + ChatColor.YELLOW + " =============");
        if(numFoundChests == 0){
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "0" + ChatColor.GRAY
                    + " non-AcuteLoot chests found in current chunk!");
        }
        else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Chests created: " + ChatColor.AQUA + numFoundChests);
            if (refillCooldown == "-1"){
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Refill cooldown: " + ChatColor.AQUA + "none"
                        + ChatColor.GRAY + "*");
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "*Will only generate loot once, when first opened");
            }
            else{

                int seconds = (Integer.parseInt(refillCooldown) * 60) % 60;
                int minutes = Integer.parseInt(refillCooldown) % 60;
                int hours   = Integer.parseInt(refillCooldown) / 60;
                sender.sendMessage(String.format(AcuteLoot.CHAT_PREFIX + "Refill cooldown: " + ChatColor.AQUA
                        + "%dh:%dm:%ds", hours, minutes, seconds));
            }
        }
    }
}
