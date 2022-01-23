package acute.loot.commands;

import acute.loot.AcuteLoot;
import acute.loot.LootMaterial;
import acute.loot.generator.LootItemGenerator;
import acute.loot.namegen.NameGenerator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
            ItemMeta meta = item.getItemMeta();
            String name = null;
            LootItemGenerator generator = plugin().lootGenerator;
            if (args.length >= 2) {
                if (plugin().nameGeneratorNames.containsKey(args[1])) {
                    try {
                        name = plugin()
                                .nameGeneratorNames
                                .get(args[1])
                                .generate(lootMaterial, generator.generate(AcuteLoot.random.nextDouble(), lootMaterial).rarity());
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
                int attempts = 100;
                NameGenerator nameGenerator = null;
                do {
                    try {
                        nameGenerator = plugin().nameGenChancePool.draw();
                        name = nameGenerator.generate(lootMaterial, generator.generate(AcuteLoot.random.nextDouble(), lootMaterial)
                                                                             .rarity());
                        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Name added with random generator");
                    } catch (NoSuchElementException e) {
                        // Couldn't draw a name for some reason, try again
                        attempts--;
                    }
                } while (name == null && attempts > 0);
                if (attempts == 0) {
                    plugin().getLogger()
                            .severe("Could not generate a name in 100 attempts! Are name files empty or corrupted?");
                    plugin().getLogger().severe("Name Generator: " + nameGenerator.toString());
                }
            }
            sender.sendMessage(name);
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin().getConfig()
                                                                                    .getString("loot-name-color")) + name);
            item.setItemMeta(meta);


        } else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "You must be holding something");
        }
    }
}
