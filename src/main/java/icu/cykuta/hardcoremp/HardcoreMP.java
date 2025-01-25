package icu.cykuta.hardcoremp;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import icu.cykuta.hardcoremp.listener.Motd;
import icu.cykuta.hardcoremp.listener.PlayerDeath;
import icu.cykuta.hardcoremp.listener.PlayerJoin;
import icu.cykuta.hardcoremp.utils.Config;
import icu.cykuta.hardcoremp.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public final class HardcoreMP extends JavaPlugin {
    private static HardcoreMP plugin;
    private static WorldManager worldManager;
    private static Config config;

    @Override
    public void onEnable() {
        try {
            plugin = this;
            config = new Config();
            worldManager = new WorldManager();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("Failed to load config file.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerDeath(), this);
        pm.registerEvents(new PlayerJoin(), this);
        pm.registerEvents(new Motd(), this);
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("The plugin has been disabled.");
    }

    public static HardcoreMP getPlugin() {
        return plugin;
    }

    public static WorldManager getWorldManager() {
        return worldManager;
    }

    public static MVWorldManager getMultiverseCore() {
        return JavaPlugin.getPlugin(MultiverseCore.class).getMVWorldManager();
    }

    public static Config getConfigFile() {
        return config;
    }
}
