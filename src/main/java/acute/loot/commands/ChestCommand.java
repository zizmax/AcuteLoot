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
import org.bukkit.persistence.PersistentDataType;

/**
 * Handler for /al chest.
 */
public class ChestCommand extends AcuteLootCommand<Player> {

    public ChestCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    private void createChest(Chest chest, Player sender, NamespacedKey key, String refillCooldown) {
        // chestMetadataCode Version 1.0 = "1.0:currentTimeMillis():refillCooldown (minutes)"
        // i.e. "1.0:1606604412:90"
        String version = "1.0";
        String currentTime = String.valueOf(System.currentTimeMillis());
        String chestMetadataCode = String.format("%s:%s:%s", version, currentTime, refillCooldown);

        if (plugin().debug) {
            sender.sendMessage("Code: " + chestMetadataCode);
        }
        chest.getPersistentDataContainer().set(key, PersistentDataType.STRING, chestMetadataCode);
        chest.update();

        Inventory inventory = chest.getInventory();
        if (inventory instanceof DoubleChestInventory) {
            // There's definitely a cleaner way of making sure the other half of a double chest is processed...
            DoubleChest doubleChest = (((DoubleChestInventory) inventory).getHolder());
            Chest leftChest = (Chest) doubleChest.getLeftSide();
            Chest rightChest = (Chest) doubleChest.getRightSide();
            leftChest.getPersistentDataContainer().set(key, PersistentDataType.STRING, chestMetadataCode);
            leftChest.update();
            rightChest.getPersistentDataContainer().set(key, PersistentDataType.STRING, chestMetadataCode);
            rightChest.update();
        }

        chest.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, chest.getBlock().getLocation().add(.5, 1, .5), 100);
    }

    @Override
    protected void doHandle(Player sender, String[] args) {
        int numFoundChests = 0; // Unclear count of double chests depending on mode
        NamespacedKey key = new NamespacedKey(plugin(), "chestMetadataKey");
        String refillCooldown = "-1";

        if (args.length >= 2) {
            try {
                int parsedRefillCooldown = Integer.parseInt(args[1]);
                if (parsedRefillCooldown < 0) {
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Refill cooldown (minutes) must be 0 or greater");
                    return;
                }
                refillCooldown = args[1];
            } catch (NumberFormatException e) {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Refill cooldown (minutes) must be an integer");
                return;
            }
        }

        sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.YELLOW + "============== " + ChatColor.GRAY +
                "Chest Creator" + ChatColor.YELLOW + " ==============");

        Block targetedBlock = sender.getTargetBlockExact(20);

        if (targetedBlock != null && targetedBlock.getType()
                                                  .equals(Material.CHEST) && targetedBlock.getState() instanceof Chest) {
            if (((Chest) (targetedBlock.getState())).getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                String chestMetadata = ((Chest) targetedBlock.getState()).getPersistentDataContainer()
                                                              .get(key, PersistentDataType.STRING);
            } else {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.GRAY + "Mode: " + ChatColor.AQUA + "targeted block");
                createChest((Chest) (targetedBlock.getState()), sender, key, refillCooldown);
                numFoundChests++;
            }
        } else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.GRAY + "Mode: " + ChatColor.AQUA + "chunk search");
            for (BlockState tileEntity : sender.getLocation().getChunk().getTileEntities()) {
                if (tileEntity instanceof Chest) {
                    if (((Chest) tileEntity).getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                        String chestMetadata = ((Chest) tileEntity).getPersistentDataContainer()
                                                                   .get(key, PersistentDataType.STRING);
                    } else {
                        createChest((Chest) tileEntity, sender, key, refillCooldown);
                        numFoundChests++;
                    }
                }
            }
        }

        if (numFoundChests == 0) {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.AQUA + "0" + ChatColor.GRAY +
                    " non-AcuteLoot chests found in current chunk!");
        } else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Chests created: " + ChatColor.AQUA + numFoundChests);
            if (refillCooldown.equals("-1")) {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Refill cooldown: " + ChatColor.AQUA + "none" + ChatColor.GRAY + "*");
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "*Only attempts to generate loot on first open");
            } else {

                int seconds = (Integer.parseInt(refillCooldown) * 60) % 60;
                int minutes = Integer.parseInt(refillCooldown) % 60;
                int hours = Integer.parseInt(refillCooldown) / 60;
                sender.sendMessage(String.format(AcuteLoot.CHAT_PREFIX + "Refill cooldown: " + ChatColor.AQUA +
                        "%dh:%dm:%ds", hours, minutes, seconds));
            }
        }
    }
}
