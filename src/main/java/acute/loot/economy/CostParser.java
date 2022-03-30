package acute.loot.economy;

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
                return new ItemCost(Material.valueOf(configuration.getString("material").toUpperCase()), cost);
        }
        throw new IllegalArgumentException("Unknown mode" + mode);
    }

}
