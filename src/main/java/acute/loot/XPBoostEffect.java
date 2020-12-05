package acute.loot;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerExpChangeEvent;

import java.util.List;

public class XPBoostEffect extends AcuteLootSpecialEffect {

    public XPBoostEffect(String name, String ns, int id, List<LootMaterial> validLootMaterials, AcuteLoot plugin) {
        super(name, ns, id, validLootMaterials, plugin);
    }

    @Override
    public void apply(Event origEvent) {
        if (origEvent instanceof PlayerExpChangeEvent) {
            if (plugin.getConfig().getDouble("effects.xp-boost.chance") > 0) {
                PlayerExpChangeEvent event = (PlayerExpChangeEvent) origEvent;
                final Player player = event.getPlayer();
                final int xpBoostAmount = plugin.getConfig().getInt("effects.xp-boost.boost-amount");
                final int eventXpAmount = event.getAmount();
                if (AcuteLoot.debug) {
                    player.sendMessage("Original XP: " + eventXpAmount);
                    player.sendMessage("Final XP: " + (eventXpAmount + xpBoostAmount));
                    player.sendMessage("XP boosted by " + xpBoostAmount);
                }
                event.setAmount(eventXpAmount + xpBoostAmount);
            }
        }
    }
}
