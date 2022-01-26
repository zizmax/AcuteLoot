package acute.loot.generator;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ColorNamerTest {

    @Test
    void colorName() {
        final Namer namer = (itemStack, lootItem) -> {};
        final ColorNamer overwrite = new ColorNamer(namer, (i,r) -> "[color]", true);
        final ColorNamer noOverwrite = new ColorNamer(namer, (i,r) -> "[color]", false);

        final String coloredName = ChatColor.translateAlternateColorCodes('&', "&aColored &2Name&r");
        final String noColorName = "No Color Name";

        assertThat(overwrite.colorName(noColorName, "[color]"), is("[color]No Color Name"));
        assertThat(overwrite.colorName(coloredName, "[color]"), is("[color]Colored Name"));
        assertThat(noOverwrite.colorName(noColorName, "[color]"), is("[color]No Color Name"));
        assertThat(noOverwrite.colorName(coloredName, "[color]"), is(coloredName));
    }
}