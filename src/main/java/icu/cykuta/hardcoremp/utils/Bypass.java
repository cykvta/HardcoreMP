package icu.cykuta.hardcoremp.utils;

import icu.cykuta.hardcoremp.HardcoreMP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Bypass {
    public static boolean isPlayerInBypassList(String playerName) {
        FileConfiguration config = HardcoreMP.getConfigFile().getFileConfiguration();
        return config.getList("bypass").contains(playerName);
    }

    public static boolean isPlayerInBypassList(Player player) {
        return isPlayerInBypassList(player.getName());
    }
}
