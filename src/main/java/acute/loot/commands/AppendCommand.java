package acute.loot.commands;

import acute.loot.AcuteLoot;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handler for /al append.
 */

public abstract class AppendCommand<T extends CommandSender> extends AcuteLootCommand<T>  {

    public AppendCommand(String permission, AcuteLoot plugin) {
        super(permission, plugin);
    }

    protected void appendName(CommandSender sender, String[] args) {

        if (args.length >= 3) {
            String fileName = args[1];
            String textToAdd = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            // File path for your .txt file
            String filePath = "plugins/AcuteLoot/names/" + fileName + ".txt"; // Assuming the file name is specified as an argument

            // Create the file object
            File file = new File(filePath);

            try {
                // Check if the file doesn't exist, create it
                if (!file.exists()) {
                    sender.sendMessage(AcuteLoot.CHAT_PREFIX + "File " + fileName + " does not exist!");
                    return;
                }

                // Append text to the file
                BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                writer.write( "\n" + textToAdd);
                writer.close();

                sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Appended successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                sender.sendMessage("An error occurred while appending text to the file.");
            }
        } else {
            sender.sendMessage(AcuteLoot.CHAT_PREFIX + "Must specify a file and input!");
        }
    }

    public static class ConsoleAppendCommand extends AppendCommand<ConsoleCommandSender> {

        public ConsoleAppendCommand(String permission, AcuteLoot plugin) {
            super(permission, plugin);
        }

        @Override
        protected void doHandle(ConsoleCommandSender sender, String[] args) {
            appendName(sender, args);
        }
    }
}





