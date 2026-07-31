package icu.cykuta.hardcoremp.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Chat {
    /**
     * Translate the alternate colour codes of a message.
     * Only {@code &} followed by a valid colour or format character is replaced,
     * so an ampersand in a player or world name survives untouched.
     *
     * @param message the raw message
     * @return the coloured message
     */
    public static String color(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Set the title and subtitle of a player and display it.
     *
     * @param player   the receiver
     * @param title    the title
     * @param subtitle the subtitle
     */
    public static void title(Player player, String title, String subtitle) {
        player.sendTitle(color(title), color(subtitle), 10, 70, 20);
    }

    public static void massTitle(String title, String subtitle) {
        Bukkit.getOnlinePlayers().forEach(player -> Chat.title(player, title, subtitle));
    }

    /**
     * Broadcast a message to all players.
     *
     * @param message the message to broadcast
     */
    public static void broadcast(String message) {
        Bukkit.getServer().broadcastMessage(color(message));
    }
}
