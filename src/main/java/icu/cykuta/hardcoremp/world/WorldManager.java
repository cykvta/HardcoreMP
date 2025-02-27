package icu.cykuta.hardcoremp.world;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.api.event.GameWorldResetEvent;
import icu.cykuta.hardcoremp.utils.Chat;
import icu.cykuta.hardcoremp.config.Setting;
import org.bukkit.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

public class WorldManager {
    private final MVWorldManager mvWorldManager = HardcoreMP.getMultiverseCore();
    private final String lobbyWorldName;
    private MultiverseWorld gameWorld;
    private String gameWorldName;
    private long gameWorldCreationTime;
    private WorldStatus status = WorldStatus.READY;

    public WorldManager(String lobbyWorldName) {
        this.lobbyWorldName = lobbyWorldName;
        this.gameWorldName = Setting.getGameWorldName();
    }

    /**
     * This method is user to try to get the lobby world.
     * Surround with try-catch block to handle the exception.
     *
     * @exception IllegalArgumentException If something wrong with the world name or world loading.
     **/
    public void loadWorlds() throws IllegalArgumentException {
        World lobbyWorld = Bukkit.getWorld(lobbyWorldName);
        this.gameWorld = mvWorldManager.getMVWorld(gameWorldName);

        // Check if worlds are null
        if (lobbyWorld == null) {
            throw new IllegalArgumentException("Worlds are null.");
        }

        // Set lobby world to peaceful.
        lobbyWorld.setDifficulty(Difficulty.PEACEFUL);

        // Check if the game world is null and create it if it is.
        if (gameWorld == null) {
            Chat.broadcast("Game world not found, creating it now.");
            this.gameWorld = this.createGameWorld();
            this.saveGameWorld();
        }

        // Set the game world creation time.
        this.setGameWorldCreationTime();
    }

    /**
     * This method is used to set the game world creation time.
     */
    public void setGameWorldCreationTime() {
        try {
            BasicFileAttributes attr = Files.readAttributes(
                    this.gameWorld.getCBWorld().getWorldFolder().toPath(), BasicFileAttributes.class);
            this.gameWorldCreationTime = attr.creationTime().toMillis();

        } catch (IOException e) {
            HardcoreMP.disablePlugin("Error while setting game world creation time.");
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

        // Set the difficulty of all worlds to hard.
        this.mvWorldManager.getMVWorld(gameWorldName).setDifficulty(Difficulty.HARD);
        this.mvWorldManager.getMVWorld(gameWorldName + "_nether").setDifficulty(Difficulty.HARD);
        this.mvWorldManager.getMVWorld(gameWorldName + "_the_end").setDifficulty(Difficulty.HARD);

        return this.mvWorldManager.getMVWorld(gameWorldName);
    }

    /**
     * This method is used to delete the game world, nether and the end. Also remove them from the config file.
     */
    public void deleteGameWorld() {
        this.mvWorldManager.deleteWorld(gameWorldName);
        this.mvWorldManager.deleteWorld(gameWorldName + "_nether");
        this.mvWorldManager.deleteWorld(gameWorldName + "_the_end");

        this.mvWorldManager.removeWorldFromConfig(gameWorldName);
        this.mvWorldManager.removeWorldFromConfig(gameWorldName + "_nether");
        this.mvWorldManager.removeWorldFromConfig(gameWorldName + "_the_end");
    }

    /**
     * This method is used to regenerate the game world and replace the gameWorld object with the new one,
     * also call the GameWorldResetEvent. <br><br>
     * Is a combination of two methods, deleteGameWorld and createGameWorld.
     */
    public void regenGameWorld() {
        this.deleteGameWorld();
        this.gameWorld = this.createGameWorld();

        // Save world name to config file.
        this.saveGameWorld();

        // Call GameWorldResetEvent
        Bukkit.getPluginManager().callEvent(
                new GameWorldResetEvent(this.gameWorld.getCBWorld()));

        // Set the game world creation time.
        this.setGameWorldCreationTime();
    }

    /**
     * This method is used to save the game world name to the config file.
     */
    public void saveGameWorld() {
        Setting.setGameWorldName(this.gameWorldName);
        Setting.saveConfig();
    }

    public long getGameWorldCreationTime() {
        return this.gameWorldCreationTime;
    }

    public MultiverseWorld getGameWorld() {
        return this.gameWorld;
    }

    public String getLobbyWorldName() {
        return lobbyWorldName;
    }

    public WorldStatus getStatus() {
        return status;
    }

    public void setStatus(WorldStatus status) {
        this.status = status;
    }
}
