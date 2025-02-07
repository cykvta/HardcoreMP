package icu.cykuta.hardcoremp.api;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.world.WorldManager;
import icu.cykuta.hardcoremp.world.WorldStatus;
import org.bukkit.World;

public class HardcoreWorldControl {
    private static final WorldManager worldManager = HardcoreMP.getWorldManager();

    /**
     * This method is used to regenerate the game world and replace the gameWorld object with the new one,
     * also call the GameWorldResetEvent.
     */
    public static void regenGameWorld() {
        worldManager.regenGameWorld();
    }

    /**
     * This method is used to get the game world.
     * @return World
     */
    public static World getGameWorld() {
        return worldManager.getGameWorld().getCBWorld();
    }

    /**
     * This method gets the name of the status of the lobby world.
     */
    public static WorldStatus getLobbyWorldName() {
        return worldManager.getStatus();
    }
}
