package icu.cykuta.hardcoremp.utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class Massive {
    /**
     * Send a message to all players.
     * @param title
     * @param subtitle
     */
    public static void title(String title, String subtitle) {
        for (Player player: Bukkit.getOnlinePlayers()) {
            Chat.title(player, title, subtitle);
        }
    }

    /**
     * Send a message to all players.
     * @param gameMode
     */
    public static void setGameMode(GameMode gameMode) {
        for (Player player: Bukkit.getOnlinePlayers()) {
            player.setGameMode(gameMode);
        }
    }

    /**
     * Clear the inventory of all players.
     */
    public static void clearInventory() {
        for (Player player: Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();
        }
    }

    /**
     * Kick all players.
     * @param reason
     */
    public static void kick(String reason) {
        for (Player player: Bukkit.getOnlinePlayers()) {
            player.kickPlayer(reason);
        }
    }

    /**
     * Regenerate the stats of all players. <br>
     * Health, food level, saturation, level.
     */
    public static void regenStats() {
        for (Player player: Bukkit.getOnlinePlayers()) {
            regenStats(player);
        }
    }

    public static void regenStats(Player player) {
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setLevel(0);
        player.setExp(0);
        player.getActivePotionEffects().forEach(effect ->
                player.removePotionEffect(effect.getType()));
    }
}
