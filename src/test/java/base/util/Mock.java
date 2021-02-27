package base.util;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Proxy;

public final class Mock {

    private Mock() {}

    public static Player mockPlayer() {
        return (Player) Proxy.newProxyInstance(Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> null);
    }

    public static ConsoleCommandSender mockConsoleCommandSender() {
        return (ConsoleCommandSender) Proxy.newProxyInstance(ConsoleCommandSender.class.getClassLoader(),
                new Class<?>[]{ConsoleCommandSender.class},
                (proxy, method, args) -> null);
    }

}
