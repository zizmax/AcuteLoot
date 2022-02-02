package acute.loot.economy;

import com.github.phillip.h.acutelib.util.Checks;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Objects;

class ItemCost implements Cost {

    private final Material material;
    private final int amount;

    ItemCost(Material material, int amount) {
        this.material = Objects.requireNonNull(material, "Material cannot be null");
        this.amount = Checks.requirePositive(amount);
    }

    @Override
    public boolean pay(Player player) {
        final HashMap<Integer, ? extends ItemStack> items = player.getInventory().all(material);
        if (items.values().stream().mapToInt(ItemStack::getAmount).sum() < amount) {
            return false;
        }

        int balance = amount;
        for (ItemStack item : items.values()) {
            final int stackSize = item.getAmount();
            item.setAmount(Math.max(0, stackSize - balance));
            if (balance > stackSize) {
                balance -= stackSize;
            } else {
                break;
            }
        }

        return true;
    }

    @Override
    public String notEnoughDescription() {
        return "You don't have enough items!";
    }
}
