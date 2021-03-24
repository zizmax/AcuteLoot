package acute.loot;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConfigTest {

    @Test
    @DisplayName("Default config version matches current version")
    public void defaultConfigVersionMatchesCurrentVersion() throws IOException, InvalidConfigurationException {
        try (final BufferedReader config = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/config.yml")))) {
            final YamlConfiguration configuration = new YamlConfiguration();
            configuration.loadFromString(config.lines().collect(Collectors.joining("\n")));
            assertThat(configuration.getInt("config-version"), is(AcuteLoot.configVersion));
        }
    }

}
