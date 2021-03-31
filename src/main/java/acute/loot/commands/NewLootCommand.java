package acute.loot.commands;

import acute.loot.AcuteLoot;
import acute.loot.EffectId;
import acute.loot.LootItem;
import acute.loot.LootRarity;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * New loot command class.
 */
public class NewLootCommand extends AcuteLootCommand<Player> {

    public NewLootCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    @Override
    protected void doHandle(Player sender, String[] args) {
        ItemStack item = plugin().lootGenerator.getNewRandomLootItemStack();
        LootItem lootItem = null;
        if (args.length == 1) {
            plugin().lootGenerator.createLootItem(item, AcuteLoot.random.nextDouble());
        } else {
            if (plugin().rarityNames.containsKey(args[1])) {
                final int rarityId = plugin().rarityNames.get(args[1]);
                if (args.length == 2) {
                    plugin().lootGenerator.createLootItem(item, LootRarity.get(rarityId));
                }
                final List<EffectId> effects = new ArrayList<>();
                if (args.length > 2) {
                    if (plugin().effectNames.containsKey(args[2])) {
                        effects.add(new EffectId(plugin().effectNames.get(args[2])));
                        lootItem = new LootItem(rarityId, effects);
                        plugin().lootGenerator.createLootItem(item, lootItem);
                    } else {
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Effect " + args[2] + " doesn't exist");
                        return; // Do not apply the rarity if the effect is invalid
                    }
                }

            } else {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Rarity " + args[1] + " doesn't exist");
                return;
            }
        }
        if (sender.getInventory().firstEmpty() != -1) {
            sender.getInventory().addItem(item);
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Created " + ChatColor.GOLD + item.getType());
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Name: " + item.getItemMeta().getDisplayName());
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Rarity: " + item.getItemMeta().getLore().get(0));
            AcuteLoot.sendIncompatibleEffectsWarning(sender, lootItem, item);
        } else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Inventory cannot be full");
        }
    }
}
