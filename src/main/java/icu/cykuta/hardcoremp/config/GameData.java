package icu.cykuta.hardcoremp.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Runtime state of the current game: when the world was created, how many
 * resets happened and how many lives are left.
 * <p>
 * It lives in data.yml instead of config.yml so that reinstalling or replacing
 * the configuration never wipes the state of a running game.
 */
public class GameData {
    private static final String DATA_PATH = "data.";

    private static YamlFile file;

    /**
     * Bind the game data to a file. Called on enable, and by the tests.
     */
    public static void bind(YamlFile yamlFile) {
        file = yamlFile;
    }

    private static FileConfiguration data() {
        return file.getFileConfiguration();
    }

    /**
     * Write the data file to disk.
     */
    public static void save() {
        file.save();
    }

    /**
     * Timestamp (ms) of the creation of the current game world.
     */
    public static long getCreateTime() {
        return data().getLong(DATA_PATH + "create-time", 0L);
    }

    public static void setCreateTime(long time) {
        data().set(DATA_PATH + "create-time", time);
    }

    /**
     * Number of world resets performed so far. Used to detect players that were
     * offline while the world was reset.
     */
    public static int getResetId() {
        return data().getInt(DATA_PATH + "reset-id", 0);
    }

    public static void setResetId(int id) {
        data().set(DATA_PATH + "reset-id", id);
    }

    /**
     * Lives left in the current session. Falls back to the configured maximum so
     * that a missing key never means "no lives left".
     */
    public static int getLives() {
        return data().getInt(DATA_PATH + "current-lives", Setting.getMaxLives());
    }

    public static void setLives(int lives) {
        data().set(DATA_PATH + "current-lives", lives);
    }

    /**
     * Move the state that older versions kept in config.yml into data.yml.
     * Values already present in data.yml win, and the legacy keys are dropped
     * from config.yml so there is a single source of truth.
     *
     * @param configFile the config.yml holding the legacy keys
     * @return true when something was migrated
     */
    public static boolean migrateFromConfig(YamlFile configFile) {
        FileConfiguration config = configFile.getFileConfiguration();
        boolean migrated = false;

        migrated |= migrateKey(config, "setting.create-time", "create-time");
        migrated |= migrateKey(config, "setting.reset-id", "reset-id");
        migrated |= migrateKey(config, "setting.current-lives", "current-lives");

        if (migrated) {
            save();
            configFile.save();
        }
        return migrated;
    }

    private static boolean migrateKey(FileConfiguration config, String legacyPath, String key) {
        if (!config.contains(legacyPath)) return false;

        if (!data().contains(DATA_PATH + key)) {
            data().set(DATA_PATH + key, config.get(legacyPath));
        }
        config.set(legacyPath, null);
        return true;
    }
}
