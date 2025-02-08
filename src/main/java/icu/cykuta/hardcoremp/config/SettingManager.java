package icu.cykuta.hardcoremp.config;

import icu.cykuta.hardcoremp.HardcoreMP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SettingManager {
    private static final FileConfiguration config = HardcoreMP.getConfigFile().getFileConfiguration();

    /**
     * Save config
     */
    public static void saveConfig() {
        HardcoreMP.getConfigFile().save();
    }

    /**
     * Return if Player name is in bypass list
     * @param playerName
     * @return boolean
     */
    public static boolean isPlayerInBypassList(String playerName) {
        return config.getList(Setting.USER_BYPASS_LIST.toString()).contains(playerName);
    }

    /**
     * Return if Player is in bypass list
     * @param player
     * @return boolean
     */
    public static boolean isPlayerInBypassList(Player player) {
        return isPlayerInBypassList(player.getName());
    }

    /**
     * Return game world name
     * @return String
     */
    public static String getGameWorldName() {
        return config.getString(Setting.GAME_WORLD.toString());
    }

    /**
     * Set game world name
     * @param worldName
     */
    public static void setGameWorldName(String worldName) {
        config.set(Setting.GAME_WORLD.toString(), worldName);
    }

    /**
     * Return lobby world name
     * @return String
     */
    public static String getLobbyWorldName() {
        return config.getString(Setting.LOBBY_WORLD.toString());
    }

    /**
     * Set lobby world name
     * @param worldName
     */
    public static void setLobbyWorldName(String worldName) {
        config.set(Setting.LOBBY_WORLD.toString(), worldName);
    }
}
