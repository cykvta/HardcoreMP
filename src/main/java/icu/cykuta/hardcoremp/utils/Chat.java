package icu.cykuta.hardcoremp.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Chat {
    public static String color(String message) {
        return message.replaceAll("&", "§");
    }

    public static void title(Player player, String title, String subtitle) {
        player.sendTitle(color(title), color(subtitle), 10, 70, 20);
    }

    public static void broadcast(String message) {
        Bukkit.getServer().broadcastMessage(color(message));
    }
}
