package acute.loot.commands;

import acute.loot.AcuteLoot;
import acute.loot.UpdateChecker;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public abstract class ReloadCommand<T extends CommandSender> extends AcuteLootCommand<T> {

    public ReloadCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    @Override
    protected void doHandle(T sender, String[] args) {
        // Reload names/rarities, copy and save config.yml
        plugin().reloadConfiguration();

        // Check for updates
        UpdateChecker.init(plugin(), AcuteLoot.spigotID).requestUpdateCheck().whenComplete((result, exception) -> {
            if (result.requiresUpdate()) {
                sendUpdateMessage(sender, result);
                return;
            }

            UpdateChecker.UpdateReason reason = result.getReason();
            if (reason == UpdateChecker.UpdateReason.UP_TO_DATE) {
                sender.sendMessage(String.format(AcuteLoot.CHAT_PREFIX + AcuteLoot.UP_TO_DATE, result.getNewestVersion()));
            } else if (reason == UpdateChecker.UpdateReason.UNRELEASED_VERSION) {
                sender.sendMessage(String.format(AcuteLoot.CHAT_PREFIX + AcuteLoot.UNRELEASED_VERSION, result.getNewestVersion()));
            } else {
                sender.sendMessage(AcuteLoot.CHAT_PREFIX + ChatColor.RED + AcuteLoot.UPDATE_CHECK_FAILED + ChatColor.WHITE + reason);
            }
        });
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Config reloaded successfully");
        sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Checking for updates...");
    }

    protected abstract void sendUpdateMessage(T sender, UpdateChecker.UpdateResult result);

    public static class PlayerReloadCommand extends ReloadCommand<Player> {

        public PlayerReloadCommand(String permission, AcuteLoot plugin) {
            super(permission, plugin);
        }

        @Override
        protected void sendUpdateMessage(Player sender, UpdateChecker.UpdateResult result) {
            BaseComponent message = new TextComponent(TextComponent.fromLegacyText(String.format(AcuteLoot.CHAT_PREFIX
                    + ChatColor.RED + AcuteLoot.UPDATE_AVAILABLE, result.getNewestVersion())));
            TextComponent link = new TextComponent( "here" );
            link.setUnderlined(true);
            link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                    AcuteLoot.SPIGOT_URL));
            link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GOLD + "SpigotMC.org")));
            message.addExtra(link);
            sender.spigot().sendMessage(message);
        }
    }

    public static class ConsoleReloadCommand extends ReloadCommand<ConsoleCommandSender> {

        public ConsoleReloadCommand(String permission, AcuteLoot plugin) {
            super(permission, plugin);
        }

        @Override
        protected void sendUpdateMessage(ConsoleCommandSender sender, UpdateChecker.UpdateResult result) {
            sender.sendMessage(String.format(AcuteLoot.CHAT_PREFIX + ChatColor.RED + AcuteLoot.UPDATE_AVAILABLE, result
                    .getNewestVersion()) + AcuteLoot.SPIGOT_URL);
        }
    }
}
