package acute.loot;

import acute.loot.generator.LootItemGenerator;
import acute.loot.tables.LootTable;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@AllArgsConstructor
public class AlApi {

    protected static final String DEFAULT_LOOT_TABLE = "DEFAULT";

    private final AcuteLoot acuteLoot;
    private final Map<String, LootTable> lootTables = new HashMap<>();

    public Logger log() {
        return acuteLoot.getLogger();
    }

    public void warn(final String message) {
        acuteLoot.getLogger().warning(message);
    }

    public void info(String message) {
        acuteLoot.getLogger().info(message);
    }

    public Path getFilePath(final String path) {
        return Paths.get(acuteLoot.getDataFolder().getPath() + "/" + path);
    }

    public LootItemGenerator getBaseLootGenerator() {
        return LootItemGenerator.builder(acuteLoot)
                                .namePool(acuteLoot.namePool(), true, true)
                                .build();
    }

    public LootItemGenerator getGeneratorWithRandomMaterials(final List<Material> materials) {
        return LootItemGenerator.builder(acuteLoot)
                .namePool(acuteLoot.namePool(), true, true)
                .randomLootMaterials(materials)
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

    public LootTable getDefaultLootTable() {
        return lootTables.computeIfAbsent(DEFAULT_LOOT_TABLE, k -> getBaseLootGenerator());
    }

    public Optional<LootTable> getLootTable(final String name) {
        return Optional.ofNullable(lootTables.get(name));
    }

    public void setLootTable(final @NonNull String name, final @NonNull LootTable table) {
        lootTables.put(name, table);
    }

    public void clearLootTables() {
        lootTables.clear();
    }
}
