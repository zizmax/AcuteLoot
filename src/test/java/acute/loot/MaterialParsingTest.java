package acute.loot;

import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

public class MaterialParsingTest {

    @Test
    @DisplayName("Newer material names parse when supported by the active API")
    public void newerMaterialNamesParseWhenSupported() {
        final List<Material> materials = Util.readMaterialsFile(Arrays.asList(
                "COPPER_SWORD, COPPER_CHESTPLATE, MACE, COPPER_SPEAR, WHITE_HARNESS, COPPER_NAUTILUS_ARMOR,"
        ), warning -> {});

        assertThat(materials, hasItem(Material.valueOf("COPPER_SWORD")));
        assertThat(materials, hasItem(Material.valueOf("COPPER_CHESTPLATE")));
        assertThat(materials, hasItem(Material.valueOf("MACE")));
        assertThat(materials, hasItem(Material.valueOf("COPPER_SPEAR")));
        assertThat(materials, hasItem(Material.valueOf("WHITE_HARNESS")));
        assertThat(materials, hasItem(Material.valueOf("COPPER_NAUTILUS_ARMOR")));
    }

    @Test
    @DisplayName("Unsupported material names are skipped without failing parsing")
    public void unsupportedMaterialNamesAreSkipped() {
        final List<String> warnings = new java.util.ArrayList<>();
        final List<Material> materials = Util.readMaterialsFile(Arrays.asList(
                "# ignored",
                "DIAMOND_SWORD, TOTALLY_NOT_A_MATERIAL,"
        ), warnings::add);

        assertThat(materials, contains(Material.DIAMOND_SWORD));
        assertThat(warnings.size(), is(1));
    }

    @Test
    @DisplayName("Default materials file has no unsupported entries on the active API")
    public void defaultMaterialsFileHasNoUnsupportedEntries() throws IOException {
        final List<String> warnings = new java.util.ArrayList<>();
        try (final BufferedReader materials = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/materials.txt")))) {
            Util.readMaterialsFile(materials.lines().collect(Collectors.toList()), warnings::add);
        }

        assertThat(warnings, is(java.util.Collections.emptyList()));
    }
}
