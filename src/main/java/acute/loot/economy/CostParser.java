package acute.loot.economy;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public class CostParser {

    public Cost parse(final ConfigurationSection configuration) {
        final String mode = configuration.getString("mode");
        final int cost = configuration.getInt("cost");

        switch (Objects.requireNonNull(mode, "No mode present in configuration section")) {
            case "xp":
                return new LevelCost(cost);
            case "item":
                Material material = XMaterial.matchXMaterial(configuration.getString("material"))
                                             .map(XMaterial::parseMaterial)
                                             .orElse(Material.EMERALD);
                return new ItemCost(material, cost);
        }
        throw new IllegalArgumentException("Unknown mode" + mode);
    }

}
