package acute.loot;

import com.github.phillip.h.acutelib.util.Pair;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ModuleManager {

    private final Map<String, Pair<Module, Boolean>> modules = new HashMap<>();
    private final Logger logger;

    public ModuleManager add(final String name, final Module module, final boolean initialState) {
        if (modules.containsKey(name)) {
            throw new IllegalArgumentException("A module named " + name + " already exists");
        }
        modules.put(name, new Pair<>(module, initialState));

        logger.info("Added module " + name);
        return this;
    }

    public boolean hasModule(final String name) {
        return modules.containsKey(name);
    }

    public List<Pair<String, Module>> enabled() {
        return modules.entrySet().stream()
                      .filter(e -> e.getValue().right())
                      .map(e -> new Pair<>(e.getKey(), e.getValue().left()))
                      .collect(Collectors.toList());
    }

    public List<String> modules() {
        return Collections.unmodifiableList(new ArrayList<>(modules.keySet()));
    }

    public boolean isEnabled(final String name) {
        return Optional.ofNullable(modules.get(name)).map(Pair::right).orElse(false);
    }

    public boolean enable(final String name) {
        if (!hasModule(name) || isEnabled(name)) {
            return false;
        }

        final Module module = modules.get(name).left();
        module.enable();
        modules.put(name, new Pair<>(module, true));

        logger.info("Enabled module " + name);
        return true;
    }

    public boolean disable(final String name) {
        if (!hasModule(name) || !isEnabled(name)) {
            return false;
        }

        final Module module = modules.get(name).left();
        module.disable();
        modules.put(name, new Pair<>(module, false));

        logger.info("Disabled module " + name);
        return true;
    }

    public void start() {
        final List<Pair<String, Module>> enabled = enabled();
        final String modulesMsg = enabled.isEmpty() ? "[NONE]" : enabled.stream().map(Pair::left).collect(Collectors.joining(" "));
        logger.info("Enabling modules " + modulesMsg);
        enabled.forEach(p -> p.right().enable());
    }

    public void stop() {
        final List<Pair<String, Module>> enabled = enabled();
        final String modulesMsg = enabled.isEmpty() ? "[NONE]" : enabled.stream().map(Pair::left).collect(Collectors.joining(" "));
        logger.info("Disabling modules " + modulesMsg);
        enabled.forEach(p -> p.right().disable());
    }

}
