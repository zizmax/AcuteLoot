package acute.loot;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Class for interfacing with AcuteLoot from external plugins.
 */
//CHECKSTYLE:OFF
public class API {
    //CHECKSTYLE:ON

    /**
     * The current API version.
     */
    public static final String API_VERSION = "1.1.0";

    // Known API versions
    private static final List<String> API_ITERATIONS = Arrays.asList("1.1.0", "1.0.0-beta", "1.0.0");

    // The AcuteLoot instance, shared between API instances
    private static AcuteLoot acuteLoot;

    // The plugin that is using the API
    private final Plugin user;

    // The EffectId namespace the plugin will be using
    private final String namespace;

    // List of effects the plugin has registered through the API
    private final List<LootSpecialEffect> registeredEffects = new ArrayList<>();

    /**
     * Construct a new API instance. Throws an AcuteLootException is the
     * provided namespace is not valid.
     *
     * @param user      the Plugin that will be using the API
     * @param namespace the namespace the Plugin will be using for LootSpecialEffect id's
     */
    public API(final Plugin user, String namespace) {
        if (!EffectId.validNamespace(namespace)) {
            throw new AcuteLootException("Invalid namespace");
        }
        this.user = user;
        this.namespace = namespace;
    }

    /**
     * Register a LootSpecialEffect with AcuteLoot with the given relative chance.
     * Throws an AcuteLootException if the effect's namespace does not match
     * the API instance's namespace.
     *
     * @param effect the LootSpecialEffect to register
     * @param chance the relative chance of this effect being applied during loot generation
     */
    public void registerEffect(final LootSpecialEffect effect, final int chance) {
        if (!effect.ns().equals(namespace)) {
            throw new AcuteLootException("Effect namespace does not match API namespace");
        }
        LootSpecialEffect.registerEffect(effect);
        acuteLoot.effectChancePool.add(effect, chance);
        acuteLoot.effectNames.put(effect.getName(), effect.effectId().toString());
        registeredEffects.add(effect);
    }

    /**
     * Unregister all effects registered through this API instance. This can be used to
     * handle config reloads, for example.
     */
    public void unregisterPluginEffects() {
        registeredEffects.forEach(LootSpecialEffect::unregisterEffect);
        registeredEffects.forEach(effect -> acuteLoot.effectNames.remove(effect.getName()));
        acuteLoot.effectChancePool.removeWithPredicate(e -> !registeredEffects.contains(e));
        registeredEffects.clear();
    }

    /**
     * Get the LootItem stored on the provided ItemStack, or Empty
     * if there is none.
     *
     * @param itemStack the ItemStack to read the LootItem from
     * @return the LootItem stored on the provided ItemStack, if any, else Empty
     */
    public Optional<LootItem> getLootItem(final ItemStack itemStack) {
        return Optional.ofNullable(acuteLoot.getLootCode(itemStack)).map(LootItem::new);
    }

    /**
     * Return if the ItemStack has the specified effect.
     *
     * @param itemStack the ItemStack to check for the effect
     * @param effect    the LootSpecialEffect to check for
     * @return if the provided ItemStack has the specified effect
     */
    public boolean itemHasEffect(final ItemStack itemStack, final LootSpecialEffect effect) {
        return getLootItem(itemStack).map(li -> li.getEffects().contains(effect)).orElse(false);
    }

    /**
     * Return if the ItemStack has the specified rarity.
     *
     * @param itemStack the ItemStack to check for the rarity
     * @param rarity    the LootRarity to check for
     * @return if the provided ItemStack has the specified rarity
     */
    public boolean itemHasRarity(final ItemStack itemStack, final LootRarity rarity) {
        return getLootItem(itemStack).map(li -> li.rarity() == rarity).orElse(false);
    }

    /**
     * Get the AcuteLoot instance associated with this API instance.
     *
     * @return the AcuteLoot instance associated with this API instance.
     */
    public AcuteLoot getAcuteLoot() {
        return acuteLoot;
    }

    /**
     * Shorthand for {@code rollName(ItemStack, LootRarity)} with a randomly
     * drawn rarity.
     *
     * @param itemStack the ItemStack to roll a name for, may be null
     * @return a new name
     * @throws AcuteLootException if a name could not be created
     */
    public String rollName(final ItemStack itemStack) throws AcuteLootException {
        return rollName(itemStack, acuteLoot.rarityChancePool().draw());
    }

    /**
     * Roll a new name for the give ItemStack and LootRarity. Either of these may be null,
     * but this may shrink the number of available name generators (as these may depend on
     * the item type or rarity) and may make it impossible to generate a name.
     *
     * @param itemStack the ItemStack to roll a name for, may be null
     * @param rarity the LootRarity to roll a name for, may be null
     * @return a new name
     * @throws AcuteLootException if a name could not be created
     */
    public String rollName(final ItemStack itemStack, final LootRarity rarity) throws AcuteLootException {
        final String name = Util.rollName(itemStack, rarity, acuteLoot.namePool(), s -> acuteLoot.getLogger().severe(s));
        if (name == null) {
            throw new AcuteLootException("Could not roll name");
        }
        return name;
    }

    /**
     * Return if the current API version is newer than or equal to
     * "version". If "version" does not exist in the versions list an
     * AcuteLootException is thrown.
     *
     * <p>Note that a newer API version is not necessarily compatible with
     * an older one, use this function together with API.apiVersionOlderThan()
     * to check for a range of known working versions for complete accuracy.
     *
     * @param version version to check
     * @return if the version is newer than or equal to the current API version
     */
    public static boolean apiVersionNewerThan(final String version) {
        final int thisIteration = API_ITERATIONS.indexOf(API_VERSION);
        final int baseIteration = API_ITERATIONS.indexOf(version);
        if (baseIteration == -1) {
            throw new AcuteLootException("Unknown API version: " + version);
        }
        if (thisIteration == -1) {
            throw new AcuteLootException("Current API version not in iterations list!");
        }

        return thisIteration >= baseIteration;
    }

    /**
     * Return if the current API version is older than
     * "version". If "version" does not exist in the versions list an
     * AcuteLootException is thrown.
     *
     * @param version version to check
     * @return if the version is newer than or equal to the current API version
     */
    public static boolean apiVersionOlderThan(final String version) {
        return !apiVersionNewerThan(version);
    }

    // Set the API AcuteLoot instance
    protected static void setAcuteLoot(final AcuteLoot acuteLoot) {
        API.acuteLoot = acuteLoot;
    }

}
