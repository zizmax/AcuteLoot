package acute.loot.rules;

import acute.loot.tables.LootTable;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

interface Generator {

    List<ItemStack> generate();

    static Generator withChance(final double chance, final LootTable backing) {
        final Random rand = new Random();
        return () -> rand.nextDouble() < chance ? singletonList(backing.generate()) : emptyList();
    }

}
