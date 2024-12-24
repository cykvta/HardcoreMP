package icu.cykuta.hardcoremp.world;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import icu.cykuta.hardcoremp.Config;
import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldManager {
    private final MVWorldManager mvWorldManager = HardcoreMP.getMultiverseCore();
    private World lobbyWorld;
    private MultiverseWorld gameWorld;
    private static final String lobbyWorldName = "world";
    private String gameWorldName;
    private static final Config cfg = HardcoreMP.getConfigFile();
    private WorldStatus status = WorldStatus.NOT_READY;

    public WorldManager() {
        this.gameWorldName = cfg.getFileConfiguration().getString("world.game");

        try {
            LoadWorlds();
        } catch (IllegalArgumentException e) {
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            Bukkit.getPluginManager().disablePlugin(HardcoreMP.getPlugin());
        }
    }

    /**
     * This method is user to try to get the lobby world.
     * Surround with try-catch block to handle the exception.
     *
     * @exception IllegalArgumentException If something wrong with the world name or world loading.
     **/
    private void LoadWorlds() {
        this.lobbyWorld = Bukkit.getWorld(lobbyWorldName);
        this.gameWorld = mvWorldManager.getMVWorld(gameWorldName);

        // Check if worlds are null
        if (lobbyWorld == null) {
            throw new IllegalArgumentException("Worlds are null.");
        }

        // Check if the game world is null and create it if it is.
        if (gameWorld == null) {
            Chat.broadcast("Game world not found, creating it now.");
            this.gameWorld = this.createGameWorld();
            this.saveGameWorld();
        }
    }

    /**
     * This method is used to create the game world.
     * It will create the world, nether and the end.
     */
    public MultiverseWorld createGameWorld() {
        // Generate random name to use as world name and world seed.
        this.gameWorldName = String.valueOf(System.currentTimeMillis());

        // Create the world, nether and the end.
        this.mvWorldManager.addWorld(gameWorldName, World.Environment.NORMAL, gameWorldName, null, null, null);
        this.mvWorldManager.addWorld(gameWorldName + "_nether", World.Environment.NETHER, gameWorldName, null, null, null);
        this.mvWorldManager.addWorld(gameWorldName + "_the_end", World.Environment.THE_END, gameWorldName, null, null, null);

        return this.mvWorldManager.getMVWorld(gameWorldName);
    }

    /**
     * This method is used to delete the game world.
     * It will delete the world, nether and the end.
     */
    public void deleteGameWorld() {
        this.mvWorldManager.deleteWorld(gameWorldName);
        this.mvWorldManager.deleteWorld(gameWorldName + "_nether");
        this.mvWorldManager.deleteWorld(gameWorldName + "_the_end");
    }

    /**
     * This method is used to regenerate the game world.
     * Is a combination of deleteGameWorld and createGameWorld.
     */
    public void regenGameWorld() {
        this.deleteGameWorld();
        this.gameWorld = this.createGameWorld();

        // Save world name to config file.
        this.saveGameWorld();
    }

    public World getLobbyWorld() {
        return this.lobbyWorld;
    }

    public MultiverseWorld getGameWorld() {
        return this.gameWorld;
    }

    public void saveGameWorld() {
        cfg.getFileConfiguration().set("world.game", gameWorld.getName());
        cfg.save();
    }

    public WorldStatus getStatus() {
        return status;
    }

    public void setStatus(WorldStatus status) {
        this.status = status;
    }
}
