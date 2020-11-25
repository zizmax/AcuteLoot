package acute.loot;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class API {

    public static final String API_VERSION = "1.0.0-beta";
    private static final List<String> API_ITERATIONS = Collections.singletonList("1.0.0-beta");

    private static AcuteLoot acuteLoot;

    private final Plugin user;
    private final List<LootSpecialEffect> registeredEffects = new ArrayList<>();

    public API(final Plugin user) {
        this.user = user;
    }

    public void registerEffect(final LootSpecialEffect effect, final int chance) {
        LootSpecialEffect.registerEffect(effect);
        AcuteLoot.effectChancePool.add(effect, chance);
        AcuteLoot.effectNames.put(effect.getName(), effect.getId());
        registeredEffects.add(effect);
    }

    public void unregisterPluginEffects() {
        registeredEffects.forEach(LootSpecialEffect::unregisterEffect);
        AcuteLoot.effectChancePool.removeWithPredicate(e -> !registeredEffects.contains(e));
        registeredEffects.clear();
    }

    public Optional<LootItem> getLootItem(final ItemStack itemStack) {
        return Optional.ofNullable(Events.getLootCode(acuteLoot, itemStack)).map(LootItem::new);
    }

    public boolean itemHasEffect(final ItemStack itemStack, final LootSpecialEffect effect) {
        return getLootItem(itemStack).map(li -> li.getEffects().contains(effect)).orElse(false);
    }

    public boolean itemHasRarity(final ItemStack itemStack, final LootRarity rarity) {
        return getLootItem(itemStack).map(li -> li.rarity() == rarity).orElse(false);
    }

    public AcuteLoot getAcuteLoot() {
        return acuteLoot;
    }

    public static boolean apiVersionNewerThan(final String base) {
        final int thisIteration = API_ITERATIONS.indexOf(API_VERSION);
        final int baseIteration = API_ITERATIONS.indexOf(base);
        if (baseIteration == -1) {
            throw new AcuteLootException("Unknown API version: " + base);
        }
        if (thisIteration == -1) {
            throw new AcuteLootException("Current API version not in iterations list!");
        }

        return thisIteration >= baseIteration;
    }

    public static boolean apiVersionOlderThan(final String base) {
        return !apiVersionNewerThan(base);
    }

    protected static void setAcuteLoot(final AcuteLoot acuteLoot) {
        API.acuteLoot = acuteLoot;
    }

}
