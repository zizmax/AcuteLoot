package acute.loot;

import base.util.Checks;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

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

    public static BaseComponent[] colorLootName(final String name, final LootRarity rarity) {
        return TextComponent.fromLegacyText(rarity.getRarityColor() + name);
    }

    /**
     * This method abstracts over a common pattern where we substitute the pattern
     * with respect to the variable map, then convert resulting tokens into BaseComponent[]'s
     * using some rule (for example, passing through unless the token is for an AcuteLoot item,
     * in which case a component with hover text is used).
     *
     * @param pattern the pattern, will be passed to base.util.Util.substituteVariables()
     * @param variableMap the variable map, will be passed to base.util.Util.substituteVariables(),
     * @param mapper mapper for the resulting tokens, must be non-null
     * @return the substituted pattern, passed through the mapper and aggregated
     */
    public static BaseComponent[] substituteAndBuildMessage(final String pattern,
                                                            final Map<String, String> variableMap,
                                                            final Function<Map.Entry<String, String>, BaseComponent[]> mapper) {
        Objects.requireNonNull(mapper);
        return base.util.Util.substituteVariables(pattern, variableMap)
                             .entrySet()
                             .stream()
                             .map(mapper)
                             .flatMap(Stream::of)
                             .toArray(BaseComponent[]::new);
    }

}
