package acute.loot;

import org.bukkit.event.Event;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class LootSpecialEffect {

    private static final String PREFIX = "plugins/AcuteLoot/";
    protected final AcuteLoot plugin;
    private List<LootMaterial> validMaterials;
    private final String name;
    private final int id;
    private List<String> matchNames = new ArrayList<>();

    public LootSpecialEffect(String name, int id, List<LootMaterial> validMaterials, AcuteLoot plugin) {
        this.name = name;
        this.id = id;
        this.validMaterials = validMaterials;
        this.plugin = plugin;
    }

    public LootSpecialEffect(String name, int id, List<LootMaterial> validMaterials, String matchNames, AcuteLoot plugin) {
        this(name, id, validMaterials, plugin);

        try (Stream<String> stream = Files.lines(Paths.get(PREFIX + matchNames))) {
            this.matchNames = stream.collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.validMaterials = validMaterials;
    }

    public abstract void apply(Event event);

    public List<LootMaterial> getValidMaterials() {
        return validMaterials;
    }

    private static final Map<Integer, LootSpecialEffect> effects = new HashMap<>();

    public static LootSpecialEffect get(int id) {
        return effects.get(id);
    }

    public static void registerEffect(final LootSpecialEffect effect) {
        if (effects.containsKey(effect.getId())) {
            throw new IllegalArgumentException("Effect with id '" + effect.getId() + "' already registered.");
        }
        effects.put(effect.getId(), effect);
    }

    public static LootSpecialEffect unregisterEffect(final LootSpecialEffect effect) {
        return effects.remove(effect.getId());
    }

    public static Map<Integer, LootSpecialEffect> getEffects() {
        return effects;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getMatchNames() {
        return matchNames;
    }

    @Override
    public String toString() {
        return name;
    }
}
