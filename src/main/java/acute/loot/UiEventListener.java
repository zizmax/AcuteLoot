package acute.loot;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

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
                final String name = base.util.Util.stripLegacyFormattingCodes(item.getItemMeta().getDisplayName());
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
                final LinkedHashMap<String, String> substituted = base.util.Util.substituteVariables(pattern, variableMap);

                final BaseComponent[] hover = new ComponentBuilder().append(Util.colorLootName(name, loot.rarity()))
                                              .event(Util.getLootHover(name, loot))
                                              .create();
                final BaseComponent[] message = substituted.entrySet().stream()
                        .map(i -> i.getKey().equals("[item]") ? hover : new TextComponent[] {new TextComponent(i.getValue())})
                        .flatMap(Stream::of)
                        .toArray(BaseComponent[]::new);
                Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(message));
                e.setDeathMessage(null);
            }
        }
    }

}
