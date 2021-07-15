package acute.loot;

import base.util.Checks;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.Objects;

/**
 * AcuteLoot utilities.
 */
public final class Util {

    private Util() {}

    /**
     * Return a HoverEvent that will display information about the given piece of loot.
     *
     * @param name the name of the loot, must be non-empty
     * @param loot the loot's LootItem, must be non-null
     * @return a HoverEvent that will display information about the given piece of loot
     */
    public static HoverEvent getLootHover(final String name, final LootItem loot) {
        Checks.requireNonEmpty(name);
        Objects.requireNonNull(loot);
        final BaseComponent[] styledName = TextComponent.fromLegacyText(loot.rarity().getRarityColor() + name);
        final ComponentBuilder stats = new ComponentBuilder()
                .append(styledName)
                .append("\n\n")
                .color(ChatColor.WHITE)
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
             .append(loot.lootCode())
             .color(net.md_5.bungee.api.ChatColor.AQUA);

        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(stats.create()));
    }

}
