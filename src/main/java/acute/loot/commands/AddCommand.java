package acute.loot.commands;

import acute.loot.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

/**
 * Handler for /al add.
 */
public class AddCommand extends AcuteLootCommand<Player> {

    public AddCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    @Override
    protected void doHandle(Player sender, String[] args) {
        if (sender.getInventory().getItemInMainHand().getType() != Material.AIR) {
            ItemStack item = sender.getInventory().getItemInMainHand();
            if (plugin().getLootCode(item) == null) {
                if (args.length == 1) {
                    final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(item.getType());
                    if (lootMaterial == LootMaterial.UNKNOWN) {
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + item.getType() + " isn't valid AcuteLoot material");
                        return;
                    }
                    plugin().lootGenerator.createLootItem(item, AcuteLoot.random.nextDouble());
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + "AcuteLoot added with random rarity");
                } else {
                    if (plugin().rarityNames.containsKey(args[1])) {
                        final int rarity = plugin().rarityNames.get(args[1]);
                        if (args.length > 2) {
                            if (plugin().effectNames.containsKey(args[2])) {
                                final EffectId effectId = new EffectId(plugin().effectNames.get(args[2]));
                                final LootItem lootItem = new LootItem(rarity, Collections.singletonList(effectId));
                                plugin().lootGenerator.createLootItem(item, lootItem);
                                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "AcuteLoot added with " + args[1] + " and " + args[2]);
                                AcuteLoot.sendIncompatibleEffectsWarning(sender, lootItem, item);
                            } else {
                                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Effect " + args[2] + " doesn't exist");
                            }
                        } else {
                            plugin().lootGenerator.createLootItem(item, LootRarity.get(rarity));
                            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "AcuteLoot added with " + args[1]);
                        }

                    } else {
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Rarity " + args[1] + " doesn't exist");
                    }
                }

            } else {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Item is already AcuteLoot");
            }
        } else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "You must be holding something");
        }
    }
}
