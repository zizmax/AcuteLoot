package acute.loot;

import java.util.Map;
import java.util.Objects;

/**
 * Class representing AcuteLoot configuration. Eventually most
 * config will be in here so it can be applied per-world, but for
 * now it is just effects and loot sources.
 */
public class AlConfig {

    private final boolean effectsEnabled;

    private final boolean enchantingEnabled;
    private final boolean fishingEnabled;
    private final boolean chestsEnabled;
    private final boolean anvilsEnabled;

    /**
     * Construct a new AlConfig.
     *
     * @param effectsEnabled if effects are enabled
     * @param enchantingEnabled if enchanting loot is enabled
     * @param fishingEnabled if fishing for loot is enabled
     * @param chestsEnabled if loot from natural chests is enabled
     * @param anvilsEnabled if loot from anvils is enabled
     */
    public AlConfig(boolean effectsEnabled, boolean enchantingEnabled, boolean fishingEnabled,
                    boolean chestsEnabled, boolean anvilsEnabled) {
        this.effectsEnabled = effectsEnabled;
        this.enchantingEnabled = enchantingEnabled;
        this.fishingEnabled = fishingEnabled;
        this.chestsEnabled = chestsEnabled;
        this.anvilsEnabled = anvilsEnabled;
    }

    /**
     * Return an AlConfig for the given config entry.
     *
     * @param configEntry the config entry, must be non-null
     * @return an AlConfig for the given config entry
     */
    public static AlConfig buildConfig(final Map<?, ?> configEntry) {
        Objects.requireNonNull(configEntry);
        return new AlConfig(getBool(configEntry, "effects.enabled"),
                            getBool(configEntry, "loot-sources.enchanting.enabled"),
                            getBool(configEntry, "loot-sources.fishing.enabled"),
                            getBool(configEntry, "loot-sources.chests.enabled"),
                            getBool(configEntry, "loot-sources.anvils.enabled"));
    }

    private static Boolean getBool(final Map<?, ?> configEntry, final String path) {
        if (configEntry.get(path) != null) {
            return (Boolean) configEntry.get(path);
        }

        // If this is a world-specific config it won't be able to resolve
        // yaml paths (the internal format is different for some reason).
        // So we resolve the path manually.
        final String[] parts = path.split("\\.");
        Map<?, ?> subMap = configEntry;
        for (int i = 0; i < parts.length - 1; i++) {
            subMap = (Map<?, ?>) subMap.get(parts[i]);
        }
        return (Boolean) subMap.get(parts[parts.length - 1]);
    }

    public boolean isEffectsEnabled() {
        return effectsEnabled;
    }

    public boolean isEnchantingEnabled() {
        return enchantingEnabled;
    }

    public boolean isFishingEnabled() {
        return fishingEnabled;
    }

    public boolean isChestsEnabled() {
        return chestsEnabled;
    }

    public boolean isAnvilsEnabled() {
        return anvilsEnabled;
    }

    @Override
    public String toString() {
        return "AlConfig{" +
                "effectsEnabled=" + effectsEnabled +
                ", enchantingEnabled=" + enchantingEnabled +
                ", fishingEnabled=" + fishingEnabled +
                ", chestsEnabled=" + chestsEnabled +
                ", anvilsEnabled=" + anvilsEnabled +
                '}';
    }
}
