package acute.loot.tables;

import acute.loot.AlApi;
import acute.loot.Util;
import com.github.phillip.h.acutelib.collections.IntegerChancePool;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@AllArgsConstructor
public class LootTableParser {

    private final @NonNull AlApi alApi;

    public LootTable parse(final ConfigurationSection config) throws IOException {

        if (config.contains("subtables")) {
            return parseWithSubtables(config);
        }

        final List<Material> materials = new ArrayList<>();
        if (config.contains("materials")) {
            config.getStringList("materials").stream().map(Material::matchMaterial).forEach(materials::add);
        }
        if (config.contains("materials-file")) {
            materials.addAll(Util.readMaterialsFile(
                    Files.lines(alApi.getFilePath(config.getString("materials-file"))).collect(Collectors.toList()),
                    alApi::warn));
        }

        if (materials.isEmpty()) {
            throw new IllegalArgumentException("Table does not contain any items!");
        }

        return alApi.getGeneratorWithRandomMaterials(materials);
    }

    private LootTable parseWithSubtables(final ConfigurationSection config) throws IOException {
        if (hasTopLevelConfig(config)) {
            alApi.warn("Loot table is using subtables, but also contains top level settings. These will be ignored.");
        }

        final IntegerChancePool<LootTable> chancePool = new IntegerChancePool<>();

        final ConfigurationSection subtables = config.getConfigurationSection("subtables");
        for (String subtable : subtables.getKeys(false)) {
            alApi.info(" - Parsing subtable " + subtable);
            final ConfigurationSection subtableSection = subtables.getConfigurationSection(subtable);

            final LootTable parsedSubtable;
            if (subtableSection.contains("ref")) {
                if (hasTopLevelConfig(subtableSection)) {
                    alApi.warn("Subtable is using ref, but also contains top level settings. These will be ignored.");
                }
                parsedSubtable = Objects.requireNonNull(alApi.getLootTable(subtableSection.getString("ref")),
                                      "Ref does not point to existing table");
            } else {
                parsedSubtable = parse(subtableSection);
            }

            chancePool.add(parsedSubtable, subtableSection.getInt("chance"));
        }

        return new ChanceTable(chancePool);
    }

    public void parseAndAddLootTables(final ConfigurationSection config) {
        int count = 0;
        for (String table : config.getKeys(false)) {
            alApi.log().info("Parsing table " + table);
            try {
                alApi.setLootTable(table, parse(config.getConfigurationSection(table)));
                count++;
            } catch (Exception e) {
                e.printStackTrace();
                alApi.log().warning("Failed to parse loot table: " + e.getMessage());
            }
        }
        alApi.log().info("Parsed " + count + " loot tables");
    }

    private boolean hasTopLevelConfig(final ConfigurationSection config) {
        return config.contains("materials") || config.contains("materials-file");
    }
}