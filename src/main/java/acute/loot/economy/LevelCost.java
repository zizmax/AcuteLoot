package acute.loot.economy;

import com.github.phillip.h.acutelib.util.Checks;
import org.bukkit.entity.Player;

class LevelCost implements Cost {

    private final int amount;

    LevelCost(final int amount) {
        this.amount = Checks.checkNot(amount, x -> x < 0, "Amount must be non-negative");
    }

    @Override
    public boolean pay(Player player) {
        if (player.getLevel() < amount) {
            return false;
        }
        player.giveExpLevels(-amount);
        return true;
    }

    @Override
    public String notEnoughDescription() {
        return "You don't have enough levels!";
    }
}
