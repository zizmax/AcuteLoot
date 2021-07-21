package acute.loot;

import base.util.Checks;
import base.util.Pair;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
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
     * @param plugin the AcuteLoot instance, used for config
     * @return a HoverEvent that will display information about the given piece of loot
     */
    public static HoverEvent getLootHover(final String name, final LootItem loot, final AcuteLoot plugin) {
        Checks.requireNonEmpty(name);
        Objects.requireNonNull(loot);

        final BaseComponent[] effects;
        if (loot.getEffects().isEmpty()) {
            effects = liftString(plugin.getConfig().getString("msg.hover-no-effects"));
        } else {
            final BaseComponent[] effectsList = loot.getEffects().stream().map(e -> {
                final Map<String, String> varMap = new HashMap<String, String>() {{
                        put("[effect_name]", plugin.getConfig().getString("loot-effect-color") + e.getDisplayName());
                    }};
                return substituteAndBuildMessage(
                        plugin.getConfig().getString("msg.hover-effects-item"),
                        varMap,
                        x -> liftString(x.getValue())
                );
            }).flatMap(Stream::of).toArray(BaseComponent[]::new);

            final Map<String, String> varMap = new HashMap<String, String>() {{
                    put("[effects]", "[effects]");
                }};
            effects = substituteAndBuildMessage(
                    base.util.Util.trimTrailingNewlines(plugin.getConfig().getString("msg.hover-effects")),
                    varMap,
                    x -> x.getKey().right().equals("[effects]") ? effectsList : liftString(x.getValue())
            );
        }

        final Map<String, String> variableMap = new HashMap<String, String>() {{
                put("[rarity]", loot.rarity().getRarityColor() + loot.rarity().getName());
                put("[name]", name);
                put("[loot_code]", ChatColor.AQUA + loot.lootCode());
                put("[effects]", "[effects]");
            }};

        final BaseComponent[] message = substituteAndBuildMessage(
                base.util.Util.trimTrailingNewlines(plugin.getConfig().getString("msg.hover")),
                variableMap,
                i -> i.getKey().right().equals("[effects]") ? effects : liftString(i.getValue())
        );
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(message));
    }

    public static BaseComponent[] liftString(final String s) {
        return new TextComponent[] {new TextComponent(s)};
    }

    public static BaseComponent[] colorLootName(final String name, final LootRarity rarity) {
        return TextComponent.fromLegacyText(rarity.getRarityColor() + name);
    }

    /**
     * This method abstracts over a common pattern where we substitute the pattern
     * with respect to the variable map, then convert resulting tokens into BaseComponent[]'s
     * using some rule (for example, passing through unless the token is for an AcuteLoot item,
     * in which case a component with hover text is used). The pattern and variable values will
     * also be passed through ChatColor.translateAlternativeColorCodes(), enabling support fot
     * '&amp;' colors as is used in the config.
     *
     * @param pattern the pattern, will be passed to base.util.Util.substituteVariables()
     * @param variableMap the variable map, will be passed to base.util.Util.substituteVariables(),
     * @param mapper mapper for the resulting tokens, must be non-null
     * @return the substituted pattern, passed through the mapper and aggregated
     */
    public static BaseComponent[] substituteAndBuildMessage(final String pattern,
                                                            final Map<String, String> variableMap,
                                                            final Function<Map.Entry<Pair<Integer, String>, String>,
                                                                           BaseComponent[]> mapper) {
        final Map<String, String> correctedVarMap = variableMap.entrySet()
                                                               .stream()
                                                               .collect(Collectors.toMap(Map.Entry::getKey,
                                                                        e -> ChatColor.translateAlternateColorCodes('&', e.getValue())));
        Objects.requireNonNull(mapper);
        return base.util.Util.substituteVariables(ChatColor.translateAlternateColorCodes('&', pattern), correctedVarMap)
                             .entrySet()
                             .stream()
                             .map(mapper)
                             .flatMap(Stream::of)
                             .toArray(BaseComponent[]::new);
    }

}
