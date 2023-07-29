package acute.loot;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Event listeners for UI stuff such as chat.
 */
public class UiEventListener implements Listener {

    private final AcuteLoot acuteLoot;

    public UiEventListener(AcuteLoot acuteLoot) {
        this.acuteLoot = acuteLoot;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e)  {
        if (!acuteLoot.getConfig().getBoolean("death-messages.enabled")) {
            return;
        }

        // The logic here is somewhat crude but it does its job:
        // if a player is killed by another player who is holding AcuteLoot,
        // AND the death message includes the name of the AcuteLoot, then we
        // build out a custom death message highlighting the AcuteLoot and
        // displaying its stats on hover. Note that this is done through a
        // message to each player, NOT the actual death message, this is because
        // the death message takes a string and and it's not (?) possible to
        // format hover text using the legacy format codes. The actual death message
        // is null'd out.
        if (e.getEntity().getKiller() != null && e.getDeathMessage() != null) {
            final ItemStack item = e.getEntity().getKiller().getInventory().getItemInMainHand();
            if (acuteLoot.getLootCode(item) != null && item.getItemMeta() != null) {
                final String name = com.github.phillip.h.acutelib.util.Util.stripLegacyFormattingCodes(item.getItemMeta().getDisplayName());
                final LootItem loot = new LootItem(acuteLoot.getLootCode(item));
                if (!e.getDeathMessage().contains(name)) {
                    return;
                }
                final String patternItem = acuteLoot.getConfig().getString("death-messages.item-death-message-pattern");
                final String patternArrow = acuteLoot.getConfig().getString("death-messages.bow-death-message-pattern");
                final String pattern;
                if (e.getEntity().getLastDamageCause() != null &&
                    e.getEntity().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                    pattern = patternArrow;
                } else {
                    pattern = patternItem;
                }

                if (pattern == null) {
                    acuteLoot.getLogger().warning("Null death message pattern.");
                    return;
                }

                final Map<String, String> variableMap = new HashMap<String, String>() {{
                        put("[killed]", e.getEntity().getDisplayName());
                        put("[killer]", e.getEntity().getKiller().getDisplayName());
                        put("[item]", name);
                    }};

                final BaseComponent[] hover = new ComponentBuilder().append(Util.colorLootName(name, loot.rarity()))
                                              .event(Util.getLootHover(item.getItemMeta().getDisplayName(), loot, acuteLoot))
                                              .create();
                final BaseComponent[] message = Util.substituteAndBuildMessage(
                        pattern,
                        variableMap,
                        i -> i.getKey().right().equals("[item]") ? hover : Util.liftString(i.getValue())
                );
                Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(message));
                e.setDeathMessage(null);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerHitItemFrame(EntityDamageByEntityEvent e)  {
        if (!acuteLoot.getConfig().getBoolean("msg.sneak-click-itemframes.enabled")) {
            return;
        }
        if (e.getEntity() instanceof ItemFrame && e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            if (p.isSneaking()) {
                e.setCancelled(true);
                ItemFrame frame = (ItemFrame) e.getEntity();
                if (acuteLoot.getLootCode(frame.getItem()) != null) {
                    final String name = frame.getItem().getItemMeta().getDisplayName();
                    final LootItem loot = new LootItem(acuteLoot.getLootCode(frame.getItem()));
                    final BaseComponent[] hover = new ComponentBuilder().append(Util.colorLootName(name, loot.rarity()))
                            .event(Util.getLootHover(name, loot, acuteLoot))
                            .create();
                    final Map<String, String> variableMap = new HashMap<String, String>() {{
                        put("[name]", p.getDisplayName());
                        put("[item]", name);
                    }};
                    final BaseComponent[] message = Util.substituteAndBuildMessage(
                            AcuteLoot.CHAT_PREFIX + acuteLoot.getConfig().getString("msg.sneak-click-itemframes.clicked"),
                            variableMap,
                            i -> i.getKey().right().equals("[item]") ? hover : Util.liftString(i.getValue())
                    );
                    p.spigot().sendMessage(message);
                }
            }
        }
    }

}
