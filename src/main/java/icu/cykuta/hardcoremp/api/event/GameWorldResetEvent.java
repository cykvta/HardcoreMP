package icu.cykuta.hardcoremp.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

/**
 * Throw when the game world is reset successfully.
 */
public class GameWorldResetEvent extends Event {
    private final MultiverseWorld gameWorld;
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public GameWorldResetEvent(MultiverseWorld gameWorld) {
        this.gameWorld = gameWorld;
    }

    /**
     * Get the current game world.
     *
     * @return World
     */
    public MultiverseWorld getGameWorld() {
        return gameWorld;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
