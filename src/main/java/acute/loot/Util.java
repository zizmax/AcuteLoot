package acute.loot;

import acute.loot.namegen.NameGenerator;
import com.github.phillip.h.acutelib.collections.IntegerChancePool;
import com.github.phillip.h.acutelib.util.Checks;
import com.github.phillip.h.acutelib.util.Pair;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * AcuteLoot utilities.
 */
public final class Util {

    private Util() {}

    /**
     * Generate a random name for an item.
     * If a name cannot be generated after 100 attempts an error
     * will be emitted to the errorLogger and null returned.
     *
     * @param item the item to generate a name for
     * @param rarity the rarity to generate a name for
     * @param namePool the name pool to use for name generation
     * @param errorLogger the consumer for errors
     * @return a name for the item
     */
    public static String rollName(final ItemStack item,
                                  final LootRarity rarity,
                                  final IntegerChancePool<NameGenerator> namePool,
                                  final Consumer<String> errorLogger) {
        final LootMaterial lootMaterial = item == null ? null : LootMaterial.lootMaterialForMaterial(item.getType());

        String name = null;
        int attempts = 100;
        NameGenerator nameGenerator = null;
        do {
            try {
                nameGenerator = namePool.draw();
                name = nameGenerator.generate(lootMaterial, rarity);
            } catch (NoSuchElementException e) {
                // Couldn't draw a name for some reason, try again
                attempts--;
            }
        } while (name == null && attempts > 0);
        if (attempts == 0) {
            errorLogger.accept("Could not generate a name in 100 attempts! Are name files empty or corrupted?");
            errorLogger.accept("Name Generator: " + nameGenerator.toString());
        }
        return name;
    }

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
                    com.github.phillip.h.acutelib.util.Util.trimTrailingNewlines(plugin.getConfig().getString("msg.hover-effects")),
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
                com.github.phillip.h.acutelib.util.Util.trimTrailingNewlines(plugin.getConfig().getString("msg.hover")),
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
        return com.github.phillip.h.acutelib.util.Util.substituteVariables(
                ChatColor.translateAlternateColorCodes('&', pattern), correctedVarMap
        )
                                                      .entrySet()
                                                      .stream()
                                                      .map(mapper)
                                                      .flatMap(Stream::of)
                                                      .toArray(BaseComponent[]::new);
    }
}
