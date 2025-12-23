package icu.cykuta.hardcoremp.world;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.api.event.GameWorldResetEvent;
import icu.cykuta.hardcoremp.utils.Chat;
import icu.cykuta.hardcoremp.config.Setting;
import org.bukkit.*;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions;
import org.mvplugins.multiverse.core.world.options.RemoveWorldOptions;

public class WorldManager {
    private final org.mvplugins.multiverse.core.world.WorldManager mvWorldManager = HardcoreMP.getMultiverseCore();
    private final String lobbyWorldName;
    private GameSession gameSession;
    private WorldStatus status = WorldStatus.READY;

    public WorldManager(String lobbyWorldName) {
        this.lobbyWorldName = lobbyWorldName;
        this.gameSession = new GameSession(Setting.getGameWorldName(), Setting.getCreateTime());
    }

    /**
     * This method is user to try to get the lobby world.
     * Surround with try-catch block to handle the exception.
     *
     * @exception IllegalArgumentException If something wrong with the world name or world loading.
     **/
    public void loadWorlds() throws IllegalArgumentException, WorldCreationError {
        World lobbyWorld = Bukkit.getWorld(lobbyWorldName);
        org.mvplugins.multiverse.core.world.MultiverseWorld overworld = gameSession.getOverworld();

        // Check if worlds are null
        if (lobbyWorld == null) {
            throw new IllegalArgumentException("Worlds are null.");
        }

        // Set lobby world to peaceful.
        lobbyWorld.setDifficulty(Difficulty.PEACEFUL);

        // Check if the game world is null and create it if it is.
        if (overworld == null) {
            Chat.broadcast("Game world not found, creating it now.");
            this.gameSession = this.createGameWorld();
            this.saveGameWorld();
        }
    }

    /**
     * This method is used to create the game world.
     * It will create the world, nether and the end.
     */
    public GameSession createGameWorld() throws WorldCreationError {
        // Generate random name to use as world name and world seed.
        long curTime = System.currentTimeMillis();
        String worldName = String.valueOf(curTime);

        // Create the world, nether and the end.
        MultiverseWorld overworld = this.mvWorldManager.createWorld(
                CreateWorldOptions.worldName(worldName).environment(World.Environment.NORMAL).seed(worldName))
                .getOrThrow(WorldCreationError::new);
        MultiverseWorld nether = this.mvWorldManager.createWorld(
                CreateWorldOptions.worldName(worldName + "_nether").environment(World.Environment.NETHER).seed(worldName))
                .getOrThrow(WorldCreationError::new);;
        MultiverseWorld end = this.mvWorldManager.createWorld(
                CreateWorldOptions.worldName(worldName + "_the_end").environment(World.Environment.THE_END).seed(worldName))
                .getOrThrow(WorldCreationError::new);

        // Set the difficulty of all worlds to hard.
        overworld.setDifficulty(Difficulty.HARD);
        nether.setDifficulty(Difficulty.HARD);
        end.setDifficulty(Difficulty.HARD);

        // Set new creation time
        GameSession newGameSession = new GameSession(curTime);
        Setting.setCreateTime(curTime);

        return newGameSession.setOverworld(overworld).setNether(nether).setEnd(end);
    }

    /**
     * This method is used to delete the game world, nether and the end. Also remove them from the config file.
     */
    public void deleteGameWorld() {
        this.mvWorldManager.removeWorld(RemoveWorldOptions.world(this.gameSession.getOverworld()));
        this.mvWorldManager.removeWorld(RemoveWorldOptions.world(this.gameSession.getNether()));
        this.mvWorldManager.removeWorld(RemoveWorldOptions.world(this.gameSession.getEnd()));
    }

    /**
     * This method is used to regenerate the game world and replace the gameWorld object with the new one,
     * also call the GameWorldResetEvent. <br><br>
     * Is a combination of two methods, deleteGameWorld and createGameWorld.
     */
    public void regenGameWorld() {
        try {
            this.deleteGameWorld();
            this.gameSession = this.createGameWorld();

            // Save world name to config file.
            this.saveGameWorld();

            // Call api event = GameWorldResetEvent
            Bukkit.getPluginManager().callEvent(new GameWorldResetEvent(this.gameSession.getOverworld()));

        } catch (WorldCreationError e) {
            HardcoreMP.disablePlugin(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is used to save the game world name to the config file.
     */
    public void saveGameWorld() {
        Setting.setGameWorldName(this.gameSession.getOverworld().getName());
        Setting.saveConfig();
    }

    public GameSession getGameSession() {
        return this.gameSession;
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
