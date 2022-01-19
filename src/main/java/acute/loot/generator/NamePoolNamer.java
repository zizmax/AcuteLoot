package acute.loot.generator;

import acute.loot.AcuteLoot;
import acute.loot.LootRarity;
import acute.loot.Util;
import acute.loot.namegen.NameGenerator;
import com.github.phillip.h.acutelib.collections.IntegerChancePool;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
class NamePoolNamer implements Namer {

    private final @NonNull IntegerChancePool<NameGenerator> namePool;
    private final @NonNull AcuteLoot plugin;

    @Override
    public void nameLoot(ItemStack lootItem, LootRarity rarity) {
        final String name = Util.rollName(lootItem, rarity, namePool, s -> plugin.getLogger().severe(s));

        final String nameColor;
        if (plugin.getConfig().getBoolean("global-loot-name-color")) {
            nameColor = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("loot-name-color"));
        } else {
            nameColor = rarity.getRarityColor();
        }

        lootItem.getItemMeta().setDisplayName(nameColor + name);
    }


}
