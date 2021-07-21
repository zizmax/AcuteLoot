package acute.loot.commands;

import acute.loot.AcuteLoot;
import acute.loot.LootItem;
import acute.loot.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Share command class.
 */
public class ShareCommand extends AcuteLootCommand<Player>  {

    public ShareCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    @Override
    protected void doHandle(Player sender, String[] args) {
        final String lootCode = plugin().getLootCode(sender.getInventory().getItemInMainHand());
        if (lootCode != null && sender.getInventory().getItemInMainHand().getItemMeta() != null) {
            final String name = sender.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
            final LootItem loot = new LootItem(lootCode);
            final BaseComponent[] message = new ComponentBuilder()
                    .append(AcuteLoot.CHAT_PREFIX)
                    .append(sender.getDisplayName())
                    .append(" shared [")
                    .append(Util.colorLootName(name, loot.rarity()))
                    .event(Util.getLootHover(name, loot))
                    .append("]")
                    .color(ChatColor.WHITE)
                    .create();
            Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(message));
        }
    }
}
