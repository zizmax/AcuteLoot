package acute.loot.economy;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public class CostParser {

    public Cost parse(final ConfigurationSection configuration) {
        final String mode = configuration.getString("mode");
        final int cost = configuration.getInt("cost");

        if (Objects.requireNonNull(mode, "No mode present in configuration section").equals("xp")) {
            return new LevelCost(cost);
        }
        throw new IllegalArgumentException("Unknown mode" + mode);
    }

}
