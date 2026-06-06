package acute.loot;

import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.potion.PotionEffect;

import java.util.List;

/**
 * Moon Boots effect class.
 */
public class MoonBootsEffect extends AcuteLootSpecialEffect {

    public MoonBootsEffect(String name, int id, List<LootMaterial> validLootMaterials, AcuteLoot plugin) {
        super(name, id, validLootMaterials, plugin);
    }

    @Override
    public void applyEffect(Event origEvent) {
        if (origEvent instanceof PlayerStatisticIncrementEvent) {
            // Player jumps are not sent to the server. The jump statistic is not a perfect proxy for jumping.
            PlayerStatisticIncrementEvent event = (PlayerStatisticIncrementEvent) origEvent;
            Player player = event.getPlayer();
            if (event.getStatistic() == Statistic.JUMP) {
                XSound.ENTITY_SLIME_JUMP.play(player.getLocation(), 2.0f, 1.0f);
                player.setVelocity(player.getVelocity().multiply(1.2));
                player.addPotionEffect(new PotionEffect(XPotion.SLOW_FALLING.getPotionEffectType(), 20, 5, true));

            }
        }
    }
}