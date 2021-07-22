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
        if (sender.getInventory().getItemInMainHand().getType().isAir()) {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + plugin().getConfig().getString("msg.generic.empty-hand"));
            return;
        }

        final String lootCode = plugin().getLootCode(sender.getInventory().getItemInMainHand());
        if (lootCode != null && sender.getInventory().getItemInMainHand().getItemMeta() != null) {
            final String name = sender.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
            final LootItem loot = new LootItem(lootCode);
            final BaseComponent[] hover = new ComponentBuilder().append(Util.colorLootName(name, loot.rarity()))
                                                                .event(Util.getLootHover(name, loot, plugin()))
                                                                .create();
            final Map<String, String> variableMap = new HashMap<String, String>() {{
                    put("[name]", sender.getDisplayName());
                    put("[item]", name);
                }};
            final BaseComponent[] message = Util.substituteAndBuildMessage(
                    AcuteLoot.CHAT_PREFIX + plugin().getConfig().getString("msg.share.shared"),
                    variableMap,
                    i -> i.getKey().right().equals("[item]") ? hover : Util.liftString(i.getValue())
            );
            Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(message));
        } else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + plugin().getConfig().getString("msg.generic.not-acuteloot"));
        }
    }
}
