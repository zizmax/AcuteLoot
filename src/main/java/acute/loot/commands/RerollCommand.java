package acute.loot.commands;

import acute.loot.AcuteLoot;
import acute.loot.LootMaterial;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


/**
 * Handler for /al reroll.
 */
public class RerollCommand extends AcuteLootCommand<Player> {

    public RerollCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }
    //TODO Add translateable messages

    @Override
    protected void doHandle(Player sender, String[] args) {
        if (plugin().getConfig().getBoolean("reroll.enabled")) {
            String currency = "experience";
            //TODO Add config option for item and Vault mode
            ItemStack item = sender.getInventory().getItemInMainHand();
            if (item.getType() != Material.AIR) {
                if (plugin().getLootCode(item) != null) {
                    //TODO Add ignoring this check as a config option?
                    final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(item.getType());
                    if (lootMaterial == LootMaterial.UNKNOWN) {
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + item.getType() + " isn't valid AcuteLoot material");
                        return;
                    }
                    int cost = plugin().getConfig().getInt("reroll.cost");
                    if (sender.getLevel() >= cost) {
                        plugin().lootGenerator.createLootItem(item, AcuteLoot.random.nextDouble());
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Reroll successful!");
                        sender.giveExpLevels(-cost);
                        sender.playSound(sender.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    } else {
                        sender.sendMessage(String.format(AcuteLoot.CHAT_PREFIX + "You don't have enough %s!", currency));
                    }
                } else {
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Item is not AcuteLoot");
                }
            } else {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "You must be holding something");
            }
        } else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Rerolling is not enabled");
        }
    }
}
