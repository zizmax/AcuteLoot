package acute.loot;

import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LootSpecialEffect {

    private static final String PREFIX = "plugins/AcuteLoot/";
    private List<LootMaterial> validMaterials;
    private final String name;
    private final EffectId id;
    private List<String> matchNames = new ArrayList<>();

    public LootSpecialEffect(String name, String ns, int id, List<LootMaterial> validMaterials) {
        if (name.contains(" ")) throw new IllegalArgumentException("Name must not contain spaces");
        if (name.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be empty");
        if (validMaterials == null || validMaterials.isEmpty()) throw new IllegalArgumentException("Materials list cannot be null or empty");
        this.name = name;
        this.id = new EffectId(ns, id);
        this.validMaterials = validMaterials;
    }

    public abstract void apply(Event event);

    public List<LootMaterial> getValidMaterials() {
        return validMaterials;
    }

    private static final Map<String, Map<Integer,LootSpecialEffect>> effects = new HashMap<>();
    public static final String AL_NS = "AL";

    @Deprecated
    public static LootSpecialEffect get(int id) {
        return get(new EffectId(AL_NS, id));
    }

    public static LootSpecialEffect get(final EffectId id) {
        return effects.get(id.ns).get(id.id);
    }

    public static void registerEffect(final LootSpecialEffect effect) {
        if (!effects.containsKey(effect.ns())) {
            effects.put(effect.ns(), new HashMap<>());
        }
        if (effects.get(effect.ns()).containsKey(effect.id())) {
            throw new IllegalArgumentException("Effect with id '" + effect + "' already registered.");
        }
        effects.get(effect.ns()).put(effect.id(), effect);
    }

    @Deprecated
    public static LootSpecialEffect unregisterEffect(final LootSpecialEffect effect) {
        return unregisterEffect(AL_NS, effect);
    }

    public static LootSpecialEffect unregisterEffect(final String ns, final LootSpecialEffect effect) {
        return effects.get(ns).remove(effect.id());
    }

    public static Map<Integer, LootSpecialEffect> getEffects(final String ns) {
        return effects.get(ns);
    }

    public int id() {
        return id.id;
    }

    public String ns() {
        return id.ns;
    }

    public EffectId effectId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
