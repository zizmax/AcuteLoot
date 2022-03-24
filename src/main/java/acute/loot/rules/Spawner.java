package acute.loot.rules;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

interface Spawner {

    void spawn(List<ItemStack> items, final Event trigger);

    static Spawner intoMobDrop() {
        return (items, trigger) -> {
            if (trigger instanceof EntityDeathEvent) {
                ((EntityDeathEvent) trigger).getDrops().addAll(items);
            }
        };
    }

    static Spawner intoMobHand() {
        return (items, trigger) -> {
            if (items.isEmpty()) {
                return;
            }
            if (trigger instanceof EntitySpawnEvent) {
                // TODO: What if not EntityLiving, what if more than one item (or none)?
                final ItemStack item = items.get(0);
                ((LivingEntity) ((EntitySpawnEvent) trigger).getEntity()).getEquipment().setItemInMainHand(item);
                ((LivingEntity) ((EntitySpawnEvent) trigger).getEntity()).getEquipment().setItemInMainHandDropChance(1);
            }
        };
    }

}
