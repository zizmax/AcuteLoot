package acute.loot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class NameResourceFormatTest {

    private static final List<String> MACE_AND_SPEAR_NAME_FILES = Arrays.asList(
            "/names/maces.txt",
            "/names/spears.txt",
            "/names/fixed/maces.txt",
            "/names/fixed/spears.txt"
    );

    @Test
    @DisplayName("Mace and spear name resources are one name per line")
    public void maceAndSpearNameResourcesAreOneNamePerLine() throws IOException {
        for (final String resourceName : MACE_AND_SPEAR_NAME_FILES) {
            final List<String> names = readResourceLines(resourceName);

            assertThat(resourceName + " should contain multiple names", names.size(), is(greaterThan(1)));
            assertThat(resourceName + " should not be a single comma-joined list",
                    names.stream().anyMatch(line -> line.contains(",")),
                    is(false));
        }
    }

    private List<String> readResourceLines(final String resourceName) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(resourceName)))) {
            return reader.lines().collect(Collectors.toList());
        }
    }
}
