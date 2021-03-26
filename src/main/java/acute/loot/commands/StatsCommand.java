package acute.loot.commands;

import acute.loot.AcuteLoot;
import acute.loot.namegen.PermutationCounts;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public abstract class StatsCommand<T extends CommandSender> extends AcuteLootCommand<T>  {

    public StatsCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    @Override
    protected void doHandle(T sender, String[] args) {
        final long birthdayCount = PermutationCounts.birthdayProblem(PermutationCounts.totalPermutations(plugin().nameGenChancePool), 0.5, 0.0001);
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "General stats:");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + String.format("Total number of possible names: ~%,d",
                                                                 PermutationCounts.totalPermutations(plugin().nameGenChancePool)));
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + String.format("Names for 50%% chance of duplicate: ~%,d", birthdayCount));
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Number of rarities: " + plugin().rarityChancePool.values().size());
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Number of effects: " + plugin().effectChancePool.values().size());
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Number of loot materials: " + plugin().lootMaterials.size());
    }

    public static class PlayerStatsCommand extends StatsCommand<Player> {

        public PlayerStatsCommand(String permission, AcuteLoot plugin) {
            super(permission, plugin);
        }

        @Override
        protected void doHandle(Player sender, String[] args) {
            if (sender.getInventory().getItemInMainHand().getType() != Material.AIR) {
                String lootCode = plugin().getLootCode(sender.getInventory().getItemInMainHand());
                if (lootCode != null) {
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Loot code: " + ChatColor.AQUA + lootCode);
                } else {
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Item is not AcuteLoot");
                }
            } else {
                super.doHandle(sender, args);
            }
        }
    }

    public static class ConsoleStatsCommand extends StatsCommand<ConsoleCommandSender> {

        public ConsoleStatsCommand(String permission, AcuteLoot plugin) {
            super(permission, plugin);
        }
    }

}
