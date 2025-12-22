package icu.cykuta.hardcoremp.world;

import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.reasons.CreateFailureReason;

public class WorldCreationError extends Throwable {
    public WorldCreationError(Attempt.Failure<LoadedMultiverseWorld, CreateFailureReason> worldName) {
        super("Cannot create " + worldName + ".");
    }
}
