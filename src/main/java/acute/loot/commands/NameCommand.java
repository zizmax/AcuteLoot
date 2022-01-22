package acute.loot.commands;

import acute.loot.AcuteLoot;
import acute.loot.LootMaterial;
import acute.loot.Util;
import acute.loot.generator.LootItemGenerator;
import com.github.phillip.h.acutelib.decorators.MetaEditor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.NoSuchElementException;

/**
 * Handler for /al name.
 */
public class NameCommand extends AcuteLootCommand<Player> {

    public NameCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    @Override
    protected void doHandle(Player sender, String[] args) {
        ItemStack item = sender.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR) {
            final LootMaterial lootMaterial = LootMaterial.lootMaterialForMaterial(item.getType());
            if (lootMaterial == LootMaterial.UNKNOWN) {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + item.getType() + " isn't valid AcuteLoot material");
                return;
            }
            final String name;
            LootItemGenerator generator = plugin().lootGenerator;
            if (args.length >= 2) {
                if (plugin().nameGeneratorNames.containsKey(args[1])) {
                    try {
                        name = Util.rollName(item,
                                generator.generate(AcuteLoot.random.nextDouble(), lootMaterial).rarity(),
                                plugin().nameGeneratorNames.get(args[1]));
                    } catch (NoSuchElementException e) {
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + args[1] + " doesn't have any valid names for this item!");
                        return;
                    }
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Name added with " + args[1]);
                } else {
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Name generator " + args[1] + " doesn't exist");
                    return;
                }

            } else {
                // Choose random generator when no other arguments are present
                name = Util.rollName(item,
                        generator.generate(AcuteLoot.random.nextDouble(), lootMaterial).rarity(),
                        plugin().nameGenChancePool,
                        s -> plugin().getLogger().severe(s));
            }
            sender.sendMessage(name);
            final String color = ChatColor.translateAlternateColorCodes('&', plugin().getConfig().getString("loot-name-color"));
            MetaEditor.on(item).setDisplayName(color + name);
        } else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "You must be holding something");
        }
    }
}
