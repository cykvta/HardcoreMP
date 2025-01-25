package icu.cykuta.hardcoremp.api.event;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Throw when the game world is reset successfully.
 */
public class GameWorldResetEvent extends Event {
    private final World gameWorld;
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public GameWorldResetEvent(World gameWorld) {
        this.gameWorld = gameWorld;
    }

    /**
     * Get the current game world.
     *
     * @return World
     */
    public World getGameWorld() {
        return gameWorld;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
