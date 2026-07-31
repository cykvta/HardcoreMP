package icu.cykuta.hardcoremp.utils;

import org.bukkit.entity.Player;

public class Stats {

    /**
     * Regenerate the stats of player. <br>
     * Health, food level, saturation, level, inventory and echest.
     */
    @SuppressWarnings("deprecation") // getMaxHealth() works on every supported API version
    public static void regenStats(Player player) {
        // setHealth(20) throws when the max health attribute is below 20
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setLevel(0);
        player.setExp(0);
        player.setTotalExperience(0);
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.getInventory().clear();
        player.getEnderChest().clear();
        player.getActivePotionEffects().forEach(effect ->
                player.removePotionEffect(effect.getType()));
    }
}
