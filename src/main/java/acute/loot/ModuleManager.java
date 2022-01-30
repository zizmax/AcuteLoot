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
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ModuleManager {

    private final Plugin plugin;
    private final Map<String, ModuleInfo> modules = new HashMap<>();
    private final Logger logger;

    private File configFile;
    private FileConfiguration configuration;

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

        // Do NOT use setter here, as the setter writes to the config...
        modules.values().forEach(m -> m.enabled = configuration.getBoolean(m.configKey, false));
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

    public boolean enable(final String name) {
        if (!hasModule(name) || isEnabled(name)) {
            return false;
        }

        final ModuleInfo module = modules.get(name);
        module.getModule().enable();
        module.setEnabled(true);

        logger.info("Enabled module " + name);
        return true;
    }

    public boolean disable(final String name) {
        if (!hasModule(name) || !isEnabled(name)) {
            return false;
        }

        final ModuleInfo module = modules.get(name);
        module.getModule().disable();
        module.setEnabled(false);

        logger.info("Disabled module " + name);
        return true;
    }

    public void preStart() {
        final List<ModuleInfo> enabled = enabled();
        enabled.forEach(p -> p.getModule().preEnable());
    }

    public void start() {
        final List<ModuleInfo> enabled = enabled();
        final String modulesMsg = enabled.isEmpty() ? "[NONE]" : enabled.stream().map(ModuleInfo::getName).collect(Collectors.joining(" "));
        logger.info("Enabling modules " + modulesMsg);
        enabled.forEach(p -> p.getModule().enable());
    }

    public void stop() {
        final List<ModuleInfo> enabled = enabled();
        final String modulesMsg = enabled.isEmpty() ? "[NONE]" : enabled.stream().map(ModuleInfo::getName).collect(Collectors.joining(" "));
        logger.info("Disabling modules " + modulesMsg);
        enabled.forEach(p -> p.getModule().disable());
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
