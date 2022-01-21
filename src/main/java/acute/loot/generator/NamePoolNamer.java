package acute.loot.generator;

import acute.loot.AcuteLoot;
import acute.loot.LootItem;
import acute.loot.Util;
import acute.loot.namegen.NameGenerator;
import com.github.phillip.h.acutelib.collections.IntegerChancePool;
import com.github.phillip.h.acutelib.decorators.MetaEditor;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
class NamePoolNamer implements Namer {

    private final @NonNull IntegerChancePool<NameGenerator> namePool;
    private final @NonNull AcuteLoot plugin;

    @Override
    public void nameLoot(ItemStack itemStack, LootItem lootItem) {
        final String name = Util.rollName(itemStack, lootItem.rarity(), namePool, s -> plugin.getLogger().severe(s));
        MetaEditor.on(itemStack).setDisplayName(name);
    }

}
