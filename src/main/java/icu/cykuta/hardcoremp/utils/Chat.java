package icu.cykuta.hardcoremp.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Chat {
    /**
     * Color a message with the Minecraft color code.
     * @param message
     * @return
     */
    public static String color(String message) {
        return message.replaceAll("&", "§");
    }

    /**
     * Set the title and subtitle of a player and display it.
     * @param player
     * @param title
     * @param subtitle
     */
    public static void title(Player player, String title, String subtitle) {
        player.sendTitle(color(title), color(subtitle), 10, 70, 20);
    }

    /**
     * Broadcast a message to all players.
     * @param message
     */
    public static void broadcast(String message) {
        Bukkit.getServer().broadcastMessage(color(message));
    }
}
