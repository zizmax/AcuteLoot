package acute.loot.listener;

import acute.loot.AcuteLoot;
import acute.loot.LootMaterial;
import acute.loot.LootSource;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Listener for loot creation via enchanting.
 */
@AllArgsConstructor
public class EnchantingLootListener implements Listener {

    private final @NonNull AcuteLoot plugin;

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        final LootSource lootSource = plugin.getEnchantingLootSource();
        final Player player = event.getEnchanter();
        if (!lootSource.enabledFor(player)) {
            return;
        }

        double roll = AcuteLoot.random.nextDouble();
        double chance = plugin.getConfig().getDouble("loot-sources.enchanting.chance") / 100.0;
        if (plugin.debug) {
            player.sendMessage("Roll: " + roll);
            player.sendMessage("Raw chance: " + chance);
        }

        ItemStack item = event.getItem();
        if (roll <= chance && plugin.getLootCode(item) == null) {
            final int enchantRarity = getEnchantRarity(event.getEnchantsToAdd());
            final int maxScore = getMaxEnchantScore(item);
            final double normalizedLuck = Math.min(1.0, (double) enchantRarity / maxScore);

            double seed = AcuteLoot.random.nextDouble();
            chance = (seed + normalizedLuck) / 2.0;
            item = lootSource.getGenerator().createLoot(item, chance);

            sendDebugStats(player, chance, enchantRarity, item, seed, maxScore);
        }
    }

    private void sendDebugStats(Player player, double chance, int enchantRarity, ItemStack item, double seed, int maxScore) {
        if (plugin.debug) {
            player.sendMessage(ChatColor.GOLD + "You enchanted a " + ChatColor.AQUA + item.getType());
            player.sendMessage(ChatColor.GOLD + "It is called " + item.getItemMeta().getDisplayName());
            player.sendMessage(ChatColor.GOLD + "Enchant Score: " + ChatColor.AQUA + enchantRarity + "/" + maxScore);
            player.sendMessage(ChatColor.GOLD + "Enchant Score Percentage: " + ChatColor.AQUA +
                    String.format("%.2f%%", (( (double) enchantRarity / maxScore) * 100.0)));
            player.sendMessage(ChatColor.GOLD + "Seed: " + ChatColor.AQUA + String.format("%.2f%%", seed * 100.0));
            player.sendMessage(ChatColor.GOLD + "Final Rarity Score: " + ChatColor.AQUA +
                    String.format("%.2f%%", chance * 100.0));
            player.sendMessage(ChatColor.GOLD + "Rarity: " + ChatColor.AQUA + item.getItemMeta().getLore().get(0));
        }
    }

    private int getMaxEnchantScore(ItemStack item) {
        switch (LootMaterial.lootMaterialForMaterial(item.getType())) {
            case CHEST_PLATE:
            case PANTS:
                return 200;
            case HELMET:
            case BOOTS:
            case PICK:
            case SHOVEL:
            case HOE:
            case CROSSBOW:
            case FISHING_ROD:
                return 250;
            default:
                return 300;
        }
    }


    private int getEnchantRarity(Map<Enchantment, Integer> enchantments) {
        double totalLevels = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            totalLevels = totalLevels + (float) entry.getValue() / entry.getKey().getMaxLevel() * 100.0;
        }
        return Math.min((int) totalLevels, 300);
    }

}
