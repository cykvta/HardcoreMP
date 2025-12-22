package icu.cykuta.hardcoremp;

import icu.cykuta.hardcoremp.command.HcmpCommand;
import icu.cykuta.hardcoremp.config.Setting;
import icu.cykuta.hardcoremp.listener.Motd;
import icu.cykuta.hardcoremp.listener.PlayerDeath;
import icu.cykuta.hardcoremp.listener.PlayerJoin;
import icu.cykuta.hardcoremp.config.ConfigLoader;
import icu.cykuta.hardcoremp.world.WorldCreationError;
import icu.cykuta.hardcoremp.world.WorldManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mvplugins.multiverse.core.MultiverseCoreApi;


public final class HardcoreMP extends JavaPlugin {
    private static HardcoreMP plugin;
    private static WorldManager worldManager;
    private static ConfigLoader configLoader;
    private static MultiverseCoreApi mvApi;

    @Override
    public void onEnable() {
        plugin = this;
        configLoader = new ConfigLoader();
        mvApi = MultiverseCoreApi.get();

        new Metrics(this, 25093);

        try {
            configLoader.register();
        } catch (Exception e) {
            disablePlugin("Failed to load config file.");
            return;
        }

        worldManager = new WorldManager(Setting.getLobbyWorldName());

        try {
            worldManager.loadWorlds();
        } catch (IllegalArgumentException e) {
            disablePlugin("Failed to load worlds.");
            return;
        } catch (WorldCreationError e) {
            disablePlugin("Failed to create worlds.");
        }

        this.registerCommands();
        this.registerEvents();
    }

    @Override
    public void onDisable() {
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
    }

    public static HardcoreMP getPlugin() {
        return plugin;
    }

    public static WorldManager getWorldManager() {
        return worldManager;
    }

    public static org.mvplugins.multiverse.core.world.WorldManager getMultiverseCore() {
        return mvApi.getWorldManager();
    }

    public static ConfigLoader getConfigFile() {
        return configLoader;
    }

    public static void disablePlugin(String reason) {
        Bukkit.getLogger().severe(reason);
        Bukkit.getPluginManager().disablePlugin(plugin);
    }
}
