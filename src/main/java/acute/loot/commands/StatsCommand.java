package acute.loot.commands;

import acute.loot.AcuteLoot;
import acute.loot.LootItem;
import acute.loot.LootSpecialEffect;
import acute.loot.namegen.PermutationCounts;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Stats command class.
 *
 * @param <T> Command sender
 */
public abstract class StatsCommand<T extends CommandSender> extends AcuteLootCommand<T>  {

    public StatsCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    protected void printGeneralStats(final CommandSender sender, boolean isPlayer) {
        final long birthdayCount = PermutationCounts.birthdayProblem(PermutationCounts.totalPermutations(plugin()
                .nameGenChancePool), 0.5, 0.0001);

        final BaseComponent[] prefix = TextComponent.fromLegacyText(AcuteLoot.CHAT_PREFIX);
        if (isPlayer) {
            final BaseComponent[] stats = new ComponentBuilder()
                    .append(String.format("Total number of possible names: ~%,d\n",
                            PermutationCounts.totalPermutations(plugin().nameGenChancePool)))
                    .append(String.format("Names for 50%% chance of duplicate: ~%,d\n", birthdayCount))
                    .append("Number of rarities: " + plugin().rarityChancePool.values().size() + "\n")
                    .append("Number of effects: " + plugin().effectChancePool.values().size() + "\n")
                    .append("Number of loot materials: " + plugin().lootMaterials.size() + "\n")
                    .create();
            final HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(stats));
            final BaseComponent[] message = new ComponentBuilder()
                    .append(prefix)
                    .append("[General Stats]")
                    .color(ChatColor.GOLD)
                    .event(hoverEvent)
                    .append(" (hover to view)")
                    .color(net.md_5.bungee.api.ChatColor.WHITE)
                    .create();
            sender.spigot().sendMessage(message);
        } else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "General stats:");
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + String.format("Total number of possible names: ~%,d",
                    PermutationCounts.totalPermutations(plugin().nameGenChancePool)));
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + String.format("Names for 50%% chance of duplicate: ~%,d", birthdayCount));
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Number of rarities: " + plugin().rarityChancePool.values().size());
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Number of effects: " + plugin().effectChancePool.values().size());
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Number of loot materials: " + plugin().lootMaterials.size());
        }
    }

    protected void printItemStats(final Player sender, final ItemStack item) {
        final String lootCode = plugin().getLootCode(item);
        if (lootCode != null) {
            final LootItem loot = new LootItem(lootCode);
            String name;
            try {
                name = item.getItemMeta().getDisplayName();
            } catch (NullPointerException e) {
                name = item.getType().name();
            }

            final ComponentBuilder stats = new ComponentBuilder()
                    .append(name + "\n\n")
                    .append("Rarity: ")
                    .append(TextComponent.fromLegacyText(loot.rarity().getRarityColor() + loot.rarity().getName()))
                    .append("\n")
                    .color(ChatColor.WHITE);
            if (!loot.getEffects().isEmpty()) {
                stats.append("Effects:\n");
                for (LootSpecialEffect effect : loot.getEffects()) {
                    stats.append(" - ");
                    stats.append(effect.getDisplayName() + "\n");
                }
            }
            stats.append("Loot code: ")
                 .append(lootCode)
                 .color(net.md_5.bungee.api.ChatColor.AQUA);

            final BaseComponent[] prefix = TextComponent.fromLegacyText(AcuteLoot.CHAT_PREFIX);
            final BaseComponent[] message = new ComponentBuilder()
                    .append(prefix)
                    .append("\"" + name + "\"")
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(stats.create())))
                    .append(" (hover me!)")
                    .color(net.md_5.bungee.api.ChatColor.WHITE)
                    .create();
            sender.spigot().sendMessage(message);
        } else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Item is not AcuteLoot");
        }
    }

    /**
     * Stats command sent by player.
     */
    public static class PlayerStatsCommand extends StatsCommand<Player> {

        public PlayerStatsCommand(String permission, AcuteLoot plugin) {
            super(permission, plugin);
        }

        @Override
        protected void doHandle(Player sender, String[] args) {
            if (sender.getInventory().getItemInMainHand().getType() != Material.AIR) {
                printItemStats(sender, sender.getInventory().getItemInMainHand());
            } else {
                printGeneralStats(sender, true);
            }
        }
    }

    /**
     * Stats command sent by console.
     */
    public static class ConsoleStatsCommand extends StatsCommand<ConsoleCommandSender> {

        public ConsoleStatsCommand(String permission, AcuteLoot plugin) {
            super(permission, plugin);
        }

        @Override
        protected void doHandle(ConsoleCommandSender sender, String[] args) {
            printGeneralStats(sender, false);
        }
    }

}
