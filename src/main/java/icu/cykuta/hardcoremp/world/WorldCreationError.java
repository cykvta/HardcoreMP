package icu.cykuta.hardcoremp.world;

public class WorldCreationError extends Exception {
    public WorldCreationError(String message) {
        super(message);
    }

    public WorldCreationError(String message, Throwable cause) {
        super(message, cause);
    }
}
