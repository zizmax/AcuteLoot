package acute.loot.commands;

import acute.loot.AcuteLoot;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;

/**
 * Class for the rmChest command.
 */
public class RmChestCommand extends AcuteLootCommand<Player> {

    public RmChestCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    @Override
    protected void doHandle(Player sender, String[] args) {
        int numFoundChests = 0; // Unclear count of double chests depending on mode
        NamespacedKey key = new NamespacedKey(plugin(), "chestMetadataKey");

        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.YELLOW + "============== " + ChatColor.GRAY +
                "Chest Remover" + ChatColor.YELLOW + " ==============");

        Block targetedBlock = sender.getTargetBlockExact(20);

        if (targetedBlock != null && targetedBlock.getType()
                                                  .equals(Material.CHEST) && targetedBlock.getState() instanceof Chest) {
            clearChest((Chest) (targetedBlock.getState()), key);
            numFoundChests = 1;
        } else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.GRAY + "Mode: " + ChatColor.AQUA + "chunk search");
            for (BlockState tileEntity : sender.getLocation().getChunk().getTileEntities()) {
                if (tileEntity instanceof Chest) {
                    clearChest((Chest) tileEntity, key);
                    numFoundChests++;
                }
            }
        }

        if (numFoundChests == 0) {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "0" + ChatColor.GRAY +
                    " AcuteLoot chests found in current chunk!");
        } else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Chests reset: " + ChatColor.AQUA + numFoundChests);
        }
    }

    private void clearChest(Chest chest, NamespacedKey key) {
        chest.getPersistentDataContainer().remove(key);
        chest.update();

        Inventory inventory = chest.getInventory();
        if (inventory instanceof DoubleChestInventory) {
            // There's definitely a cleaner way of making sure the other half of a double chest is processed...
            DoubleChest doubleChest = (((DoubleChestInventory) inventory).getHolder());
            Chest leftChest = (Chest) doubleChest.getLeftSide();
            Chest rightChest = (Chest) doubleChest.getRightSide();
            leftChest.getPersistentDataContainer().remove(key);
            leftChest.update();
            rightChest.getPersistentDataContainer().remove(key);
            rightChest.update();
        }

        chest.getWorld().spawnParticle(Particle.SMOKE_NORMAL, chest.getBlock().getLocation().add(.5, 1, .5), 100);
    }
}
