package acute.loot.commands;

import acute.loot.AcuteLoot;
import base.util.Either;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Chest command super class.
 */
public abstract class ChestCommand extends AcuteLootCommand<Player> {

    public ChestCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    protected Either<Chest, List<Chest>> selectChests(final Player sender) {
        final Block targetedBlock = sender.getTargetBlockExact(20);
        if (targetedBlock != null && targetedBlock.getType().equals(Material.CHEST) && targetedBlock.getState() instanceof Chest) {
            return Either.ofLeft((Chest) targetedBlock.getState());
        } else {
            return Either.ofRight(Arrays.stream(sender.getLocation().getChunk().getTileEntities())
                                        .filter(t -> t instanceof Chest)
                                        .map(t -> (Chest) t)
                                        .collect(Collectors.toList()));
        }
    }

    protected void updateChestData(Chest chest, Consumer<PersistentDataContainer> updater) {
        updater.accept(chest.getPersistentDataContainer());
        chest.update();

        final Inventory inventory = chest.getInventory();
        if (inventory instanceof DoubleChestInventory) {
            // There's definitely a cleaner way of making sure the other half of a double chest is processed...
            final DoubleChest doubleChest = (((DoubleChestInventory) inventory).getHolder());
            final Chest leftChest = (Chest) doubleChest.getLeftSide();
            final Chest rightChest = (Chest) doubleChest.getRightSide();
            updater.accept(leftChest.getPersistentDataContainer());
            leftChest.update();
            updater.accept(rightChest.getPersistentDataContainer());
            rightChest.update();
        }

        chest.getWorld().spawnParticle(Particle.SMOKE_NORMAL, chest.getBlock().getLocation().add(.5, 1, .5), 100);
    }

    /**
     * Handler for /al chest.
     */
    public static class CreateChestCommand extends ChestCommand {

        private final NamespacedKey key;

        public CreateChestCommand(String permission, AcuteLoot plugin) {
            super(permission, plugin);
            key = new NamespacedKey(plugin, "chestMetadataKey");
        }

        @Override
        protected void doHandle(Player sender, String[] args) {
            final String refillCooldown;
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
            } else {
                refillCooldown = "-1";
            }

            sender.sendMessage(String.format("%s%s============== %sChest Creator%s ==============",
                    AcuteLoot.CHAT_PREFIX, ChatColor.YELLOW, ChatColor.GRAY, ChatColor.YELLOW));

            selectChests(sender).acceptOr(c -> createTargetChest(sender, refillCooldown, c),
                    cx -> createRangeChests(sender, refillCooldown, cx));
        }

        private void createRangeChests(final Player sender, final String refillCooldown, final List<Chest> targetChests) {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.GRAY + "Mode: " + ChatColor.AQUA + "chunk search");
            final List<Chest> nonAlChests = targetChests.stream()
                                                        .filter(c -> !c.getPersistentDataContainer()
                                                                       .has(key, PersistentDataType.STRING))
                                                        .collect(Collectors.toList());
            if (nonAlChests.isEmpty()) {
                sender.sendMessage(String.format("%s%s0%s non-AcuteLoot chests found in current chunk!",
                        AcuteLoot.CHAT_PREFIX, ChatColor.AQUA, ChatColor.GRAY));
            } else {
                nonAlChests.forEach(c -> createChest(c, sender, refillCooldown));
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Chests created: " + ChatColor.AQUA + nonAlChests.size());
                printRefillInfo(sender, refillCooldown);
            }
        }

        private void createTargetChest(final Player sender, final String refillCooldown, final Chest target) {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.GRAY + "Mode: " + ChatColor.AQUA + "targeted block");
            if (target.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Targeted chest is already an AcuteLoot chest!");
            } else {
                createChest(target, sender, refillCooldown);
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Chest created!");
                printRefillInfo(sender, refillCooldown);
            }
        }

        private void createChest(Chest chest, Player sender, String refillCooldown) {
            // chestMetadataCode Version 1.0 = "1.0:currentTimeMillis():refillCooldown (minutes)"
            // i.e. "1.0:1606604412:90"
            final String version = "1.0";
            final String currentTime = String.valueOf(System.currentTimeMillis());
            final String chestMetadataCode = String.format("%s:%s:%s", version, currentTime, refillCooldown);

            if (plugin().debug) {
                sender.sendMessage("Code: " + chestMetadataCode);
            }
            updateChestData(chest, pdc -> pdc.set(key, PersistentDataType.STRING, chestMetadataCode));
            chest.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, chest.getBlock().getLocation().add(.5, 1, .5), 100);
        }

        private void printRefillInfo(final Player sender, final String refillCooldown) {
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

    /**
     * Class for the rmChest command.
     */
    public static class RemoveChestCommand extends ChestCommand {

        private final NamespacedKey key;

        public RemoveChestCommand(String permission, AcuteLoot plugin) {
            super(permission, plugin);
            key = new NamespacedKey(plugin, "chestMetadataKey");
        }

        @Override
        protected void doHandle(Player sender, String[] args) {
            sender.sendMessage(String.format("%s%s============== %sChest Remover%s ==============",
                                             AcuteLoot.CHAT_PREFIX, ChatColor.YELLOW, ChatColor.GRAY, ChatColor.YELLOW));

            final List<Chest> chests = selectChests(sender).reduce(Collections::singletonList, x -> x);
            if (chests.isEmpty()) {
                sender.sendMessage(String.format("%s%s0%s AcuteLoot chests found in current chunk!",
                                                 AcuteLoot.CHAT_PREFIX, ChatColor.AQUA, ChatColor.GRAY));
            } else {
                chests.forEach(this::clearChest);
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Chests reset: " + ChatColor.AQUA + chests.size());
            }
        }

        private void clearChest(Chest chest) {
            updateChestData(chest, pdc -> pdc.remove(key));
            chest.getWorld().spawnParticle(Particle.SMOKE_NORMAL, chest.getBlock().getLocation().add(.5, 1, .5), 100);
        }
    }
}
