package acute.loot.commands;

import acute.loot.AcuteLoot;
import base.util.Either;
import base.util.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Chest command super class.
 */
public abstract class ChestCommand extends AcuteLootCommand<Player> {

    protected final NamespacedKey key;

    public ChestCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
        key = new NamespacedKey(plugin, "chestMetadataKey");
    }

    protected Either<Chest, List<Chest>> selectChests(final Player sender, final int range) {
        final Block targetedBlock = sender.getTargetBlockExact(20);
        if (targetedBlock != null && targetedBlock.getType().equals(Material.CHEST) && targetedBlock.getState() instanceof Chest) {
            return Either.ofLeft((Chest) targetedBlock.getState());
        } else {
            return Either.ofRight(Util.chunksAround(sender, range)
                                      .stream()
                                      .map(Chunk::getTileEntities)
                                      .flatMap(Arrays::stream)
                                      .filter(t -> t instanceof Chest)
                                      .map(t -> (Chest) t)
                                      .collect(Collectors.toList()));
        }
    }

    protected void updateChestData(Chest chest, Consumer<PersistentDataContainer> updater) {
        final List<Chest> chests;
        if (chest.getInventory() instanceof DoubleChestInventory) {
            // There's definitely a cleaner way of making sure the other half of a double chest is processed...
            final DoubleChest doubleChest = (((DoubleChestInventory) chest.getInventory()).getHolder());
            chests = Arrays.asList((Chest) doubleChest.getLeftSide(), (Chest) doubleChest.getRightSide());
        } else {
            chests = Collections.singletonList(chest);
        }

        chests.forEach(c -> {
            updater.accept(c.getPersistentDataContainer());
            c.update();
        });
    }

    /**
     * Handler for /al chest.
     */
    public static class CreateChestCommand extends ChestCommand {

        public CreateChestCommand(String permission, AcuteLoot plugin) {
            super(permission, plugin);
        }

        @Override
        protected void doHandle(Player sender, String[] args) {
            final Optional<Integer> cd = args.length >= 2 ? Util.parseIntOptional(args[1]) : Optional.of(-1);
            if (!cd.isPresent()) {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Refill cooldown (minutes) must be an integer");
                return;
            } else if (cd.get() < -1) {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Refill cooldown (minutes) must be -1 or greater");
                return;
            }

            final Optional<Integer> range = args.length >= 3 ? Util.parseIntOptional(args[2]) : Optional.of(1);
            if (!range.isPresent() || range.get() < 1) {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Range must be a positive integer");
                return;
            } else if (range.get() > 8) {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Range must not exceed 8 (this is to prevent server lag / crashes)");
                return;
            }

            final boolean overwrite = args.length >= 4 && args[3].equals("true");

            sender.sendMessage(String.format("%s%s============== %sChest Creator%s ==============",
                    AcuteLoot.CHAT_PREFIX, ChatColor.YELLOW, ChatColor.GRAY, ChatColor.YELLOW));

            final String refillCooldown = cd.get().toString();
            selectChests(sender, range.get()).acceptOr(c -> createTargetChest(sender, refillCooldown, c, overwrite),
                                                       cx -> createRangeChests(sender, refillCooldown, cx, overwrite));
        }

        private void createRangeChests(final Player sender, final String refillCooldown,
                                       final List<Chest> targetChests, final boolean overwrite) {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.GRAY + "Mode: " + ChatColor.AQUA + "chunk search");
            final List<Chest> nonAlChests = targetChests.stream()
                                                        .filter(c -> overwrite || !c.getPersistentDataContainer()
                                                                                    .has(key, PersistentDataType.STRING))
                                                        .collect(Collectors.toList());
            if (nonAlChests.isEmpty()) {
                sender.sendMessage(String.format("%s%s0%s target chests found in current chunk!",
                        AcuteLoot.CHAT_PREFIX, ChatColor.AQUA, ChatColor.GRAY));
            } else {
                nonAlChests.forEach(c -> createChest(c, sender, refillCooldown));
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Chests created: " + ChatColor.AQUA + nonAlChests.size());
                printRefillInfo(sender, refillCooldown);
            }
        }

        private void createTargetChest(final Player sender, final String refillCooldown,
                                       final Chest target, final boolean overwrite) {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.GRAY + "Mode: " + ChatColor.AQUA + "targeted block");
            if (!overwrite && target.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
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

        public RemoveChestCommand(String permission, AcuteLoot plugin) {
            super(permission, plugin);
        }

        @Override
        protected void doHandle(Player sender, String[] args) {
            final Optional<Integer> range = args.length >= 2 ? Util.parseIntOptional(args[1]) : Optional.of(1);
            if (!range.isPresent() || range.get() < 1) {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Range must be a positive integer");
                return;
            } else if (range.get() > 8) {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Range must not exceed 8 (this is to prevent server lag / crashes)");
                return;
            }

            sender.sendMessage(String.format("%s%s============== %sChest Remover%s ==============",
                                             AcuteLoot.CHAT_PREFIX, ChatColor.YELLOW, ChatColor.GRAY, ChatColor.YELLOW));

            final List<Chest> chests = selectChests(sender, range.get()).reduce(Collections::singletonList, x -> x)
                                                                        .stream()
                                                                        .filter(c -> c.getPersistentDataContainer()
                                                                                      .has(key, PersistentDataType.STRING))
                                                                        .collect(Collectors.toList());
            if (chests.isEmpty()) {
                sender.sendMessage(String.format("%s%s0%s AcuteLoot chests found!",
                                                 AcuteLoot.CHAT_PREFIX, ChatColor.AQUA, ChatColor.GRAY));
            } else {
                chests.forEach(c -> updateChestData(c, pdc -> pdc.remove(key)));
                chests.forEach(c -> c.getWorld().spawnParticle(Particle.SMOKE_NORMAL, c.getBlock().getLocation().add(.5, 1, .5), 100));
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Chests reset: " + ChatColor.AQUA + chests.size());
            }
        }
    }
}
