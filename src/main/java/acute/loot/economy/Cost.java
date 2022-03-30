package acute.loot.economy;

import org.bukkit.entity.Player;

public interface Cost {

    boolean pay(Player player);

    String notEnoughDescription();

}
