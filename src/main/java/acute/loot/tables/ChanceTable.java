package acute.loot.tables;

import com.github.phillip.h.acutelib.collections.IntegerChancePool;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
class ChanceTable implements LootTable {

    private final IntegerChancePool<LootTable> chancePool;

    @Override
    public ItemStack generate() {
        return chancePool.draw().generate();
    }
}
