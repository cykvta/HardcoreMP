package icu.cykuta.hardcoremp.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Read-only access to the options an admin can change in config.yml.
 * Runtime state belongs to {@link GameData}.
 */
public class Setting {
    private static final String SETTING_PATH = "setting.";
    private static final int DEFAULT_MAX_LIVES = 3;
    private static final String DEFAULT_LOBBY_WORLD = "world";

    private static YamlFile file;

    /**
     * Bind the settings to a file. Called on enable, and by the tests.
     */
    public static void bind(YamlFile yamlFile) {
        file = yamlFile;
    }

    private static FileConfiguration config() {
        return file.getFileConfiguration();
    }

    /**
     * Save config
     */
    public static void saveConfig() {
        file.save();
    }

    /**
     * Return if Player name is in bypass list
     *
     * @param playerName the name to look up
     * @return boolean
     */
    public static boolean isPlayerInBypassList(String playerName) {
        List<?> bypassList = config().getList(SETTING_PATH + "user-bypass-list");
        return bypassList != null && bypassList.contains(playerName);
    }

    /**
     * Return if Player is in bypass list
     *
     * @param player the player to look up
     * @return boolean
     */
    public static boolean isPlayerInBypassList(Player player) {
        return isPlayerInBypassList(player.getName());
    }

    /**
     * Return lobby world name
     *
     * @return String
     */
    public static String getLobbyWorldName() {
        String name = config().getString(SETTING_PATH + "lobby-world", DEFAULT_LOBBY_WORLD);
        return name == null || name.trim().isEmpty() ? DEFAULT_LOBBY_WORLD : name;
    }

    /**
     * Set lobby world name
     *
     * @param worldName the new lobby world name
     */
    public static void setLobbyWorldName(String worldName) {
        config().set(SETTING_PATH + "lobby-world", worldName);
    }

    /**
     * Is MOTD enabled
     */
    public static boolean isMotdEnabled() {
        return config().getBoolean(SETTING_PATH + "motd");
    }

    /**
     * Is offline player inventory clear enabled
     */
    public static boolean isOfflinePlayerInventoryClearEnabled() {
        return config().getBoolean(SETTING_PATH + "offline-player-inventory-clear");
    }

    /**
     * Lives a brand new game starts with. A missing key falls back to the
     * default instead of 0, which would reset the world on the first death.
     */
    public static int getMaxLives() {
        return Math.max(0, config().getInt(SETTING_PATH + "max-lives", DEFAULT_MAX_LIVES));
    }

    /**
     * Whether old world folders are deleted instead of backed up on reset
     */
    public static boolean removeOldWorlds() {
        return config().getBoolean(SETTING_PATH + "remove-old-worlds");
    }
}
