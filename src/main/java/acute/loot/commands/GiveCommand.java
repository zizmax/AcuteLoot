package acute.loot.commands;

import acute.loot.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GiveCommand extends AcuteLootCommand<CommandSender> {

    public GiveCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    @Override
    protected void doHandle(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            if (sender.getServer().getPlayerExact(args[1]) != null) {
                Player target = sender.getServer().getPlayerExact(args[1]);
                ItemStack item = plugin().lootGenerator.getNewRandomLootItemStack();
                LootItem lootItem = null;
                if (args.length == 2) plugin().lootGenerator.createLootItem(item, AcuteLoot.random.nextDouble());
                else {
                    if (plugin().rarityNames.containsKey(args[2])) {
                        final int rarityID = plugin().rarityNames.get(args[2]);
                        if (args.length == 3) {
                            plugin().lootGenerator.createLootItem(item, LootRarity.get(rarityID));
                        }
                        final List<EffectId> effects = new ArrayList<>();
                        if (args.length > 3) {
                            if (plugin().effectNames.containsKey(args[3])) {
                                effects.add(new EffectId(plugin().effectNames.get(args[3])));
                                lootItem = new LootItem(rarityID, effects);
                                plugin().lootGenerator.createLootItem(item, lootItem);
                            } else {
                                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Effect " + args[3] + " doesn't exist");
                                return; // Do not apply the rarity if the effect is invalid
                            }
                        }

                    } else {
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Rarity " + args[2] + " doesn't exist");
                        return;
                    }
                }
                if (target.getInventory().firstEmpty() != -1) {
                    target.getInventory().addItem(item);
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Gave " + ChatColor.GOLD + item.getType() + ChatColor.GRAY + " to " + ChatColor.GOLD + target.getDisplayName());
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Name: " + item.getItemMeta().getDisplayName());
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Rarity: " + item.getItemMeta().getLore().get(0));
                    AcuteLoot.sendIncompatibleEffectsWarning(sender, lootItem, item);
                } else {
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + target.getDisplayName() + "'s inventory is full!");
                }

            } else {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Player " + args[1] + " is not online!");
            }
        }
        else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Must specify a player!");
        }
    }
}
