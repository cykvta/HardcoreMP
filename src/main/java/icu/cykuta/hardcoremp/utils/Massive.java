package icu.cykuta.hardcoremp.utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Massive {
    public static void message(String message) {
        Chat.broadcast(message);
    }

    public static void title(String title, String subtitle) {
        for (Player player: Bukkit.getOnlinePlayers()) {
            Chat.title(player, title, subtitle);
        }
    }

    public static void setGameMode(GameMode gameMode) {
        for (Player player: Bukkit.getOnlinePlayers()) {
            player.setGameMode(gameMode);
        }
    }

    public static void teleport(Location location) {
        for (Player player: Bukkit.getOnlinePlayers()) {
            player.teleport(location);
        }
    }

    public static void clearInventory() {
        for (Player player: Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();
        }
    }

    public static void kick(String reason) {
        for (Player player: Bukkit.getOnlinePlayers()) {
            player.kickPlayer(reason);
        }
    }

    public static void regenStats() {
        for (Player player: Bukkit.getOnlinePlayers()) {
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.setLevel(0);
        }
    }
}
