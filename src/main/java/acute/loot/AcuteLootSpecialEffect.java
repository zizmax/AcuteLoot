package acute.loot;

import java.util.List;

/**
 * Abstract base class for AcuteLoot effects. Does nothing over LootSpecialEffect
 * except provide an AcuteLoot plugin instance and read the DisplayName from config.
 */
public abstract class AcuteLootSpecialEffect extends LootSpecialEffect {

    protected final AcuteLoot plugin;

    public AcuteLootSpecialEffect(String name, String ns, int id, List<LootMaterial> validMaterials, AcuteLoot plugin) {
        super(name, ns, id, validMaterials, plugin.getConfig().getString("effects." + name.replace("_", ".") + ".name"));
        this.plugin = plugin;
    }
}
