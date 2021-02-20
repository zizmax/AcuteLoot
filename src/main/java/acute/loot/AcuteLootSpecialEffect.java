package acute.loot;

import base.util.Checks;
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

    public AcuteLootSpecialEffect(String name, String ns, int id, List<LootMaterial> validMaterials, AcuteLoot plugin) {
        super(name, ns, id, validMaterials, plugin.getConfig().getString("effects." + name.replace("_", ".") + ".name"));
        this.plugin = Objects.requireNonNull(plugin);
        this.effectEnabledConfigKey = Checks.requireNonEmpty(plugin.getConfig().getString("effects." + name.replace("_", ".") + ".enabled"));
    }

    @Override
    public final void apply(Event event) {
        if (plugin.getConfig().getBoolean(effectEnabledConfigKey)) {
            applyEffect(event);
        }
    }

    protected abstract void applyEffect(Event event);
}
