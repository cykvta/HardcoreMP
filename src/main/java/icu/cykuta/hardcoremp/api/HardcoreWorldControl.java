package icu.cykuta.hardcoremp.api;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.world.GameSession;
import icu.cykuta.hardcoremp.world.WorldCreationError;
import icu.cykuta.hardcoremp.world.WorldManager;
import icu.cykuta.hardcoremp.world.WorldStatus;
import org.bukkit.World;

public class HardcoreWorldControl {
    private static final WorldManager worldManager = HardcoreMP.getWorldManager();

    /**
     * This method is used to regenerate the game world and replace the gameWorld object with the new one,
     * also call the GameWorldResetEvent.
     */
    public static void regenGameWorld() throws WorldCreationError {
        worldManager.regenGameWorld();
    }

    /**
     * This method is used to get the game world.
     * @return World
     */
    public static GameSession getGameSession() {
        return worldManager.getGameSession();
    }

    /**
     * This method gets the name of the status of the lobby world.
     */
    public static WorldStatus getLobbyWorldName() {
        return worldManager.getStatus();
    }
}
