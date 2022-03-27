package acute.loot;

import acute.loot.generator.LootItemGenerator;
import lombok.AllArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

@AllArgsConstructor
public class AlApi {

    private final AcuteLoot acuteLoot;

    public LootItemGenerator getBaseLootGenerator() {
        return LootItemGenerator.builder(acuteLoot)
                                .namePool(acuteLoot.namePool(), true, true)
                                .build();
    }

    public void addListener(final Listener listener) {
        acuteLoot.getServer().getPluginManager().registerEvents(listener, acuteLoot);
    }

    public void removeListener(final Listener listener) {
        if (listener != null) {
            HandlerList.unregisterAll(listener);
        }
    }

    public ConfigurationSection getBaseConfiguration() {
        return acuteLoot.getConfig();
    }

}
