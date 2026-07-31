package icu.cykuta.hardcoremp;

import icu.cykuta.hardcoremp.command.HcmpCommand;
import icu.cykuta.hardcoremp.config.GameData;
import icu.cykuta.hardcoremp.config.LangManager;
import icu.cykuta.hardcoremp.config.Setting;
import icu.cykuta.hardcoremp.config.YamlFile;
import icu.cykuta.hardcoremp.listener.Motd;
import icu.cykuta.hardcoremp.listener.PlayerDeath;
import icu.cykuta.hardcoremp.listener.PlayerJoin;
import icu.cykuta.hardcoremp.listener.PlayerPortal;
import icu.cykuta.hardcoremp.world.WorldManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public final class HardcoreMP extends JavaPlugin {
    private static HardcoreMP plugin;
    private static WorldManager worldManager;
    private static YamlFile configFile;
    private static YamlFile langFile;
    private static YamlFile dataFile;

    @Override
    public void onEnable() {
        plugin = this;
        configFile = new YamlFile("config.yml");
        langFile = new YamlFile("lang.yml");
        dataFile = new YamlFile("data.yml");

        try {
            new Metrics(this, 25093);
        } catch (Throwable t) {
            getLogger().warning("Failed to start bStats metrics: " + t.getMessage());
        }

        try {
            configFile.register();
            langFile.register();
            dataFile.register();
        } catch (Exception e) {
            disablePlugin("Failed to load the plugin files: " + e.getMessage());
            return;
        }

        Setting.bind(configFile);
        LangManager.bind(langFile);
        GameData.bind(dataFile);

        // Older versions kept the game state inside config.yml
        if (GameData.migrateFromConfig(configFile)) {
            getLogger().info("Game state migrated from config.yml to data.yml.");
        }

        worldManager = new WorldManager(Setting.getLobbyWorldName());

        try {
            worldManager.loadWorlds();
        } catch (RuntimeException e) {
            // Must return: once the plugin is disabled its classloader is closed,
            // and loading any further class of ours throws a "zip file closed" error.
            disablePlugin("Failed to load worlds: " + e.getMessage());
            return;
        }

        this.registerCommands();
        this.registerEvents();
    }

    @Override
    public void onDisable() {
        // Persist lives and reset id before the server saves the players, so a
        // restart is not mistaken for a missed world reset.
        if (worldManager != null) {
            try {
                worldManager.saveState();
            } catch (Exception e) {
                getLogger().warning("Failed to save plugin state: " + e.getMessage());
            }
        }
        Bukkit.getConsoleSender().sendMessage("The plugin has been disabled.");
    }

    public void registerCommands() {
        getCommand("hcmp").setExecutor(new HcmpCommand());
    }

    public void registerEvents() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerDeath(), this);
        pm.registerEvents(new PlayerJoin(), this);
        pm.registerEvents(new Motd(), this);
        pm.registerEvents(new PlayerPortal(), this);
    }

    public static HardcoreMP getPlugin() {
        return plugin;
    }

    public static WorldManager getWorldManager() {
        return worldManager;
    }

    /** Admin options (config.yml). */
    public static YamlFile getConfigFile() {
        return configFile;
    }

    /** Messages (lang.yml). */
    public static YamlFile getLangFile() {
        return langFile;
    }

    /** Runtime game state (data.yml). */
    public static YamlFile getDataFile() {
        return dataFile;
    }

    /**
     * Log a line through the plugin logger, falling back to the server one when
     * the plugin is not enabled yet.
     */
    public static void log(String message) {
        if (plugin != null) plugin.getLogger().info(message);
        else Bukkit.getLogger().info(message);
    }

    /**
     * Disable the plugin with a reason.
     * <p>
     * Never call this from {@link #onEnable()} and keep going: disabling closes the
     * plugin classloader, so the very next class we touch fails to load.
     */
    public static void disablePlugin(String reason) {
        Bukkit.getLogger().severe(reason);
        if (plugin != null) Bukkit.getPluginManager().disablePlugin(plugin);
    }
}
