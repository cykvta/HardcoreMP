package icu.cykuta.hardcoremp.config;

import icu.cykuta.hardcoremp.HardcoreMP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Setting {
    private static final FileConfiguration config = HardcoreMP.getConfigFile().getFileConfiguration();
    private static final String settingPath = "setting.";

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
        return config.getList(settingPath + "user-bypass-list").contains(playerName);
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
        return config.getString(settingPath + "game-world");
    }

    /**
     * Set game world name
     * @param worldName
     */
    public static void setGameWorldName(String worldName) {
        config.set(settingPath + "game-world", worldName);
    }

    /**
     * Return lobby world name
     * @return String
     */
    public static String getLobbyWorldName() {
        return config.getString(settingPath + "lobby-world");
    }

    /**
     * Set lobby world name
     * @param worldName
     */
    public static void setLobbyWorldName(String worldName) {
        config.set(settingPath + "lobby-world", worldName);
    }

    /**
     * Is MOTD enabled
     */
    public static boolean isMotdEnabled() {
        return config.getBoolean(settingPath + "motd");
    }

    /**
     * Is offline player inventory clear enabled
     */
    public static boolean isOfflinePlayerInventoryClearEnabled() {
        return config.getBoolean(settingPath + "offline-player-inventory-clear");
    }

    /**
     * Get default lives count for new world
     */
    public static int getMaxLives() {
        return config.getInt(settingPath + "max-lives");
    }

    /**
     * Get default lives count for new world
     */
    public static int getLives() {
        return config.getInt(settingPath + "current-lives");
    }

    /**
     * Set current lives count
     */
    public static void setLives(int lives) {
        config.set(settingPath + "current-lives", lives);
    }

    /**
     * Set world creation time
     */
    public static void setCreateTime(long time) {
        config.set(settingPath + "create-time", time);
    }

    /**
     * Get world creation time
     */
    public static long getCreateTime() {
        return config.getLong(settingPath + "create-time");
    }
}
