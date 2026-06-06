package acute.loot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class VersionCompatibilityTest {

    @Test
    @DisplayName("Version comparison supports legacy and modern Minecraft versions")
    public void versionComparisonSupportsLegacyAndModernVersions() {
        assertThat(AcuteLoot.isVersionAtLeast("1.17.1-R0.1-SNAPSHOT", "1.17"), is(true));
        assertThat(AcuteLoot.isVersionAtLeast("1.16.5-R0.1-SNAPSHOT", "1.17"), is(false));
        assertThat(AcuteLoot.isVersionAtLeast("1.20.6-R0.1-SNAPSHOT", "1.17"), is(true));
        assertThat(AcuteLoot.isVersionAtLeast("26.1.2-R0.1-SNAPSHOT", "1.17"), is(true));
        assertThat(AcuteLoot.isVersionAtLeast("26.1.2-R0.1-SNAPSHOT", "26.1.2"), is(true));
        assertThat(AcuteLoot.isVersionAtLeast("26.1.1-R0.1-SNAPSHOT", "26.1.2"), is(false));
    }
}
