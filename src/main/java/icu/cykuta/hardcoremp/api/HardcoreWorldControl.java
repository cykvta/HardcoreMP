package icu.cykuta.hardcoremp.api;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.world.GameSession;
import icu.cykuta.hardcoremp.world.WorldCreationError;
import icu.cykuta.hardcoremp.world.WorldManager;
import icu.cykuta.hardcoremp.world.WorldStatus;

public class HardcoreWorldControl {

    /**
     * Resolved on every call: a static field would capture the manager while the
     * plugin is still enabling and keep a null forever.
     */
    private static WorldManager worldManager() {
        return HardcoreMP.getWorldManager();
    }

    /**
     * This method is used to regenerate the game world and replace the gameWorld object with the new one,
     * also call the GameWorldResetEvent.
     */
    public static void regenGameWorld() throws WorldCreationError {
        worldManager().regenGameWorld();
    }

    /**
     * This method is used to get the game world.
     * @return World
     */
    public static GameSession getGameSession() {
        return worldManager().getGameSession();
    }

    /**
     * Status of the game world.
     */
    public static WorldStatus getStatus() {
        return worldManager().getStatus();
    }

    /**
     * @deprecated misleading name, use {@link #getStatus()}.
     */
    @Deprecated
    public static WorldStatus getLobbyWorldName() {
        return getStatus();
    }
}
