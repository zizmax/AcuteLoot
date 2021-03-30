package acute.loot;

import base.util.Checks;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing a special effect that may be applied on a item.
 */
public abstract class LootSpecialEffect {

    // Materials this effect can be applied to
    private final List<LootMaterial> validMaterials;

    // internal name for this effect, used in command interface
    private final String name;

    // display name for this effect, used in item lore
    private final String displayName;

    // effect Id for this effect, must be unique if effect is registered
    private final EffectId id;

    /**
     * Construct a new LootSpecialEffect.
     *
     * @param name the internal name, must not be empty
     * @param ns the effect namespace
     * @param id the effect id
     * @param validMaterials the list of materials the effect can be applied to, cannot be empty
     * @param displayName the effect display name
     */
    public LootSpecialEffect(String name, String ns, int id, List<LootMaterial> validMaterials, String displayName) {
        this.name = Checks.requireNonEmpty(name, "Name cannot be empty");
        if (name.contains(" ")) {
            throw new IllegalArgumentException("Name cannot have spaces");
        }
        this.id = new EffectId(ns, id);
        this.validMaterials = Checks.requireNonEmpty(validMaterials, "Materials list cannot be empty");
        this.displayName = displayName;
    }

    /**
     * Apply the special effect, called by various event listeners.
     *
     * @param event the event.
     */
    public abstract void apply(Event event);

    /**
     * Get the list of LootMaterial's that this effect can be applied to.
     *
     * @return the list of LootMaterial's that this effect can be applied to
     */
    public List<LootMaterial> getValidMaterials() {
        return validMaterials;
    }

    /**
     * Get the registered effect map for the given namespace. This maps integer id's to effects.
     *
     * @param ns the namespace to get the effect map for
     * @return the effect map for the given namespace
     */
    public static Map<Integer, LootSpecialEffect> getEffects(final String ns) {
        effects.putIfAbsent(ns, new HashMap<>());
        return effects.get(ns);
    }

    /**
     * Get the integer id for this effect. Note that this
     * may overlap with other effects in different namespaces.
     *
     * @return the integer id for this effect.
     */
    public int id() {
        return id.id;
    }

    /**
     * Get the namespace for this effect.
     *
     * @return the namespace for this effect
     */
    public String ns() {
        return id.ns;
    }

    /**
     * Get the effect id for this effect.
     *
     * @return the effect id for this effect
     */
    public EffectId effectId() {
        return id;
    }

    /**
     * Get the "internal" name for this effect, this will also be
     * used in the /al command.
     *
     * @return the "internal" name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the display name for this effect.
     *
     * @return the display name for this effect.
     */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return name;
    }

    private static final Map<String, Map<Integer, LootSpecialEffect>> effects = new HashMap<>();

    /**
     * The namespace used by AcuteLoot.
     */
    public static final String AL_NS = "AL";

    @Deprecated
    public static LootSpecialEffect get(int id) {
        return get(new EffectId(AL_NS, id));
    }

    /**
     * Get the given registered effect by effect id.
     *
     * @param id the EffectId of the effect to get
     * @return the effect with the given effect id
     */
    public static LootSpecialEffect get(final EffectId id) {
        return effects.get(id.ns).get(id.id);
    }

    /**
     * Register the given effect. If an effect with the same effect id has already been
     * registered an AcuteLootException is thrown.
     *
     * @param effect the effect to register
     */
    public static void registerEffect(final LootSpecialEffect effect) {
        if (!effects.containsKey(effect.ns())) {
            effects.put(effect.ns(), new HashMap<>());
        }
        if (effects.get(effect.ns()).containsKey(effect.id())) {
            throw new AcuteLootException("Effect with id '" + effect + "' already registered.");
        }
        effects.get(effect.ns()).put(effect.id(), effect);
    }

    /**
     * Unregister the given effect. Throws an AcuteLootException if the
     * effect has not been registered.
     *
     * @param effect the effect to unregister, must have been registered before
     * @return the unregistered effect
     */
    public static LootSpecialEffect unregisterEffect(final LootSpecialEffect effect) {
        if (!effects.containsKey(effect.ns()) || !effects.get(effect.ns()).containsKey(effect.id())) {
            throw new AcuteLootException("Effect has not been registered");
        }
        return effects.get(effect.ns()).remove(effect.id());
    }
}
