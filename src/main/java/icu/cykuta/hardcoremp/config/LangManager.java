package icu.cykuta.hardcoremp.config;

import icu.cykuta.hardcoremp.utils.Chat;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Access to the messages stored in lang.yml.
 */
public class LangManager {

    private static YamlFile file;

    /**
     * Bind the messages to a file. Called on enable, and by the tests.
     */
    public static void bind(YamlFile yamlFile) {
        file = yamlFile;
    }

    private static FileConfiguration lang() {
        return file.getFileConfiguration();
    }

    /**
     * Get the message from lang.yml.
     *
     * @param path the message key
     * @return String
     */
    public static String getLang(String path) {
        String msg = lang().getString(path);
        return msg == null ? path + " is not found in lang.yml." : Chat.color(msg);
    }

    /**
     * Send message to player.
     *
     * @param sender the receiver of the message
     * @param path   the message key
     */
    public static void sendMessage(CommandSender sender, String path) {
        sender.sendMessage(getLang(path));
    }
}
