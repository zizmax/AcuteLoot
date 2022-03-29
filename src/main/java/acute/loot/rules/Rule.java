package acute.loot.rules;

import lombok.AllArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@AllArgsConstructor
class Rule {

    private final Condition condition;
    private final Generator generator;
    private final Spawner spawner;

    boolean metBy(Event event) {
        return condition.metBy(event);
    }

    List<ItemStack> generate() {
        return generator.generate();
    }

    void spawn(List<ItemStack> items, Event event) {
        spawner.spawn(items, event);
    }
}
