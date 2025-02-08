package icu.cykuta.hardcoremp.config;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.utils.Chat;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;


public class LangManager {
    private static final FileConfiguration config = HardcoreMP.getConfigFile().getFileConfiguration();

    /**
     * Get the message from the config file.
     * @param path
     * @return String
     */
    public static String getLang(String path) {
        String msg = config.getString("lang." + path);
        return msg == null ? path + " is not found in the config file." : Chat.color(msg);
    }

    /**
     * Send message to player.
     * @param sender
     * @param path
     */
    public static void sendMessage(CommandSender sender, String path) {
        sender.sendMessage(getLang(path));
    }
}
