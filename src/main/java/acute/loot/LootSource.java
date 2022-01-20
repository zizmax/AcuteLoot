package acute.loot;

import acute.loot.generator.LootItemGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.Map;

@AllArgsConstructor
public class LootSource {

    private final boolean enabledGlobal;
    private final @NonNull Map<String, Boolean> enabledPerWorld;
    private final boolean usePermissions;
    private final @NonNull String permission;
    private final @NonNull @Getter LootItemGenerator generator;

    public boolean enabledFor(final Player player) {
        return (!usePermissions || player.hasPermission(permission)) &&
               enabledPerWorld.getOrDefault(player.getWorld().getName(), enabledGlobal);
    }

}
