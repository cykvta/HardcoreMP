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
}
