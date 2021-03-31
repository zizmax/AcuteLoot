package acute.loot;

import org.bukkit.event.Event;

import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for AcuteLoot effects. Does nothing over LootSpecialEffect
 * except provide an AcuteLoot plugin instance and read the DisplayName from config.
 */
public abstract class AcuteLootSpecialEffect extends LootSpecialEffect {

    protected final AcuteLoot plugin;
    private final String effectEnabledConfigKey;

    /**
     * Construct a new AcuteLootSpecialEffect. This will use the LootSpecialEffect.AL_NS namespace.
     *
     * @param name the config name
     * @param id the effect id
     * @param validMaterials the list of valid materials for the effect
     * @param plugin the AcuteLoot instance
     */
    public AcuteLootSpecialEffect(String name, int id, List<LootMaterial> validMaterials, AcuteLoot plugin) {
        super(name, LootSpecialEffect.AL_NS, id, validMaterials, plugin.getConfig()
                                                                       .getString("effects." + name.replace("_", ".") + ".name"));
        this.plugin = Objects.requireNonNull(plugin);
        this.effectEnabledConfigKey = "effects." + name.replace("_", ".") + ".enabled";
    }

    @Override
    public final void apply(Event event) {
        if (plugin.getConfig().getBoolean(effectEnabledConfigKey)) {
            applyEffect(event);
        }
    }

    protected abstract void applyEffect(Event event);
}
