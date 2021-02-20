package acute.loot;

import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class MoonBootsEffect extends AcuteLootSpecialEffect {

    public MoonBootsEffect(String name, String ns, int id, List<LootMaterial> validLootMaterials, AcuteLoot plugin) {
        super(name, ns, id, validLootMaterials, plugin);
    }

    @Override
    public void applyEffect(Event origEvent) {
        if (origEvent instanceof PlayerStatisticIncrementEvent) {
            // Player jumps are not sent to the server. The jump statistic is not a perfect proxy for jumping.
            PlayerStatisticIncrementEvent event = (PlayerStatisticIncrementEvent) origEvent;
            Player player = event.getPlayer();
            if (event.getStatistic() == Statistic.JUMP) {
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 2, 1);
                player.setVelocity(player.getVelocity().multiply(1.2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20, 5, true));

            }
        }
    }
}