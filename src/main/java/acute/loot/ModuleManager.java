package acute.loot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ModuleManager {

    private final Plugin plugin;
    private final Map<String, ModuleInfo> modules = new HashMap<>();
    private final Logger logger;

    private File configFile;
    private FileConfiguration configuration;
    private FileConfiguration internalConfig;

    public void reload() {
        configFile = new File(plugin.getDataFolder(), "modules.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource("modules.yml", false);
        }

        configuration = new YamlConfiguration();
        try {
            configuration.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            logger.severe(e.getMessage());
        }

        internalConfig = new YamlConfiguration();
        try (InputStream resource = plugin.getResource("modules.yml");
             InputStreamReader reader = new InputStreamReader(resource)) {
            internalConfig.load(reader);
        } catch (IOException | InvalidConfigurationException e) {
            logger.severe(e.getMessage());
        }

        modules.values().forEach(m -> {
            if (configuration.contains(m.configKey)) {
                // Do NOT use setter here, as the setter writes to the config...
                m.enabled = configuration.getBoolean(m.configKey);
            } else if (internalConfig.contains(m.configKey)) {
                // DO use the setter here, because we want to propagate changes to disk
                m.setEnabled(internalConfig.getBoolean(m.configKey));
            } else {
                logger.severe("Could not find config key for " + m.name + " in either config!");
            }
        });
    }

    public ModuleManager add(final String name, final Module module, final String configKey) {
        if (modules.containsKey(name)) {
            throw new IllegalArgumentException("A module named " + name + " already exists");
        }
        // Modules start disabled, and will have their status loaded
        // from the config on reload()
        modules.put(name, new ModuleInfo(module, name, configKey, false));

        logger.info("Added module " + name);
        return this;
    }

    public boolean hasModule(final String name) {
        return modules.containsKey(name);
    }

    public List<ModuleInfo> enabled() {
        return modules.values().stream()
                      .filter(ModuleInfo::isEnabled)
                      .collect(Collectors.toList());
    }

    public List<String> modules() {
        return Collections.unmodifiableList(new ArrayList<>(modules.keySet()));
    }

    public boolean isEnabled(final String name) {
        return Optional.ofNullable(modules.get(name)).map(ModuleInfo::isEnabled).orElse(false);
    }

    private boolean enableDisable(final boolean enable, final String name) {
        if (!hasModule(name) || isEnabled(name) == enable) {
            return false;
        }

        final ModuleInfo module = modules.get(name);
        if (enable) {
            module.getModule().enable();
        } else {
            module.getModule().disable();
        }
        module.setEnabled(enable);

        logger.info((enable ? "Enabled" : "Disabled") + " module " + name);
        return true;
    }

    public boolean enable(final String name) {
        return enableDisable(true, name);
    }

    public boolean disable(final String name) {
        return enableDisable(false, name);
    }

    private void changeState(final Consumer<ModuleInfo> action, final Function<String, String> msgBuilder) {
        final List<ModuleInfo> enabled = enabled();
        final String modulesMsg = enabled.isEmpty() ? "[NONE]" : enabled.stream().map(ModuleInfo::getName).collect(Collectors.joining(" "));
        logger.info(msgBuilder.apply(modulesMsg));
        enabled.forEach(action);
    }

    public void preStart() {
        changeState(p -> p.getModule().preEnable(), msg -> "Pre-Enabling modules " + msg);
    }

    public void start() {
        changeState(p -> p.getModule().enable(), msg -> "Enabling modules " + msg);
    }

    public void stop() {
        changeState(p -> p.getModule().disable(), msg -> "Disabling modules " + msg);
    }

    @AllArgsConstructor
    @Getter
    private class ModuleInfo {
        private final Module module;
        private final String name;
        private final String configKey;
        private boolean enabled;

        private void setEnabled(boolean enabled) {
            this.enabled = enabled;
            configuration.set(configKey, enabled);
            try {
                configuration.save(configFile);
            } catch (IOException e) {
                logger.severe("Failed to save modules config");
            }
        }
    }

}
