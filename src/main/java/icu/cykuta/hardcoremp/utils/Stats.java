package icu.cykuta.hardcoremp.utils;

import org.bukkit.entity.Player;

public class Stats {

    /**
     * Regenerate the stats of player. <br>
     * Health, food level, saturation, level, inventory and echest.
     */
    public static void regenStats(Player player) {
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setLevel(0);
        player.setExp(0);
        player.getInventory().clear();
        player.getEnderChest().clear();
        player.getActivePotionEffects().forEach(effect ->
                player.removePotionEffect(effect.getType()));
    }
}
