package acute.loot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ModuleManager {

    private final Plugin plugin;
    private final Map<String, ModuleInfo> modules = new HashMap<>();
    private final Logger logger;

    public ModuleManager add(final String name, final Module module, final String configKey) {
        if (modules.containsKey(name)) {
            throw new IllegalArgumentException("A module named " + name + " already exists");
        }
        modules.put(name, new ModuleInfo(module, name, configKey, plugin.getConfig().getBoolean(configKey, false)));

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
        plugin.getConfig().set(module.getConfigKey(), true);
        plugin.saveConfig();

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
        plugin.getConfig().set(module.getConfigKey(), false);
        plugin.saveConfig();

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
    private static class ModuleInfo {
        private final Module module;
        private final String name;
        private final String configKey;
        private @Setter boolean enabled;
    }

}
