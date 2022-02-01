package acute.loot.commands;

import acute.loot.AcuteLoot;
import acute.loot.LootMaterial;
import acute.loot.economy.Cost;
import acute.loot.economy.CostParser;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static acute.loot.AcuteLoot.CHAT_PREFIX;


/**
 * Handler for /al reroll.
 */
public class RerollCommand extends AcuteLootCommand<Player> {

    private final Cost cost;

    public RerollCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
        cost = new CostParser().parse(plugin.getConfig().getConfigurationSection("reroll"));
    }
    //TODO Add translateable messages

    @Override
    protected void doHandle(Player sender, String[] args) {
        if (plugin().getConfig().getBoolean("reroll.enabled")) {
            //TODO Add config option for item and Vault mode
            ItemStack item = sender.getInventory().getItemInMainHand();
            if (item.getType() != Material.AIR) {
                if (plugin().getLootCode(item) != null) {
                    //TODO Add ignoring this check as a config option?
                    final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(item.getType());
                    if (lootMaterial == LootMaterial.UNKNOWN) {
                        sender.sendMessage(CHAT_PREFIX + item.getType() + " isn't valid AcuteLoot material");
                        return;
                    }
                    if (cost.pay(sender)) {
                        plugin().lootGenerator.createLoot(item, AcuteLoot.random.nextDouble());
                        sender.sendMessage(CHAT_PREFIX + "Reroll successful!");
                        sender.playSound(sender.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    } else {
                        sender.sendMessage(CHAT_PREFIX + cost.notEnoughDescription());
                    }
                } else {
                    sender.sendMessage(CHAT_PREFIX + "Item is not AcuteLoot");
                }
            } else {
                sender.sendMessage(CHAT_PREFIX + "You must be holding something");
            }
        } else {
            sender.sendMessage(CHAT_PREFIX + "Rerolling is not enabled");
        }
    }
}
