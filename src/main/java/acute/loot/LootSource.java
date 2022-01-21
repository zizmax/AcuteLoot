package acute.loot;

import acute.loot.generator.LootItemGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * A source of AcuteLoot.
 */
@AllArgsConstructor
public class LootSource {

    private final boolean enabledGlobal;
    private final @NonNull Map<String, Boolean> enabledPerWorld;
    private final boolean usePermissions;
    private final @NonNull String permission;
    private final @NonNull @Getter LootItemGenerator generator;

    /**
     * Return if this LootSource is enabled for the given player.
     *
     * @param player the player
     * @return if this LootSource is enabled for the given player
     */
    public boolean enabledFor(final @NonNull Player player) {
        return (!usePermissions || player.hasPermission(permission)) &&
               enabledPerWorld.getOrDefault(player.getWorld().getName(), enabledGlobal);
    }

}
