package acute.loot.tables;

import acute.loot.AlApi;
import acute.loot.Util;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class LootTableParser {

    private final @NonNull AlApi alApi;

    public LootTable parse(final ConfigurationSection config) throws IOException {
        final List<Material> materials = new ArrayList<>();

        if (config.contains("materials")) {
            config.getStringList("materials").stream().map(Material::matchMaterial).forEach(materials::add);
        }
        if (config.contains("materials-file")) {
            materials.addAll(Util.readMaterialsFile(
                    Files.lines(alApi.getFilePath(config.getString("materials-file"))).collect(Collectors.toList()),
                    alApi::warn));
        }

        return alApi.getGeneratorWithRandomMaterials(materials);
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

}
