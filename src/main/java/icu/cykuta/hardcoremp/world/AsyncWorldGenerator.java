package icu.cykuta.hardcoremp.world;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.config.GameData;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates the game worlds without blocking the main server thread.
 */
public class AsyncWorldGenerator {

    // Fixed names, they never depend on a timestamp
    public static final String OVERWORLD_NAME = "hcmp_game";
    public static final String NETHER_NAME    = "hcmp_game_nether";
    public static final String END_NAME       = "hcmp_game_the_end";

    private GameSession gameSession;
    private CompletableFuture<GameSession> completionFuture;
    private final AtomicInteger successCount = new AtomicInteger(0);

    /**
     * Starts the generation of the three worlds.
     * They are created on the main thread (required for WorldInitEvent) but the
     * caller is not blocked: the returned CompletableFuture completes once every
     * world is ready.
     */
    public CompletableFuture<GameSession> generateWorldsAsync() {
        this.completionFuture = new CompletableFuture<>();
        this.gameSession = GameSession.createNew(System.currentTimeMillis());

        long seed = new Random().nextLong();

        // Create the worlds on the main thread (required for WorldInitEvent)
        Bukkit.getScheduler().runTask(HardcoreMP.getPlugin(), () -> {
            try {
                createWorld(OVERWORLD_NAME, World.Environment.NORMAL, seed, "overworld");
                createWorld(NETHER_NAME,    World.Environment.NETHER,   seed, "nether");
                createWorld(END_NAME,       World.Environment.THE_END,  seed, "end");
            } catch (Exception e) {
                completionFuture.completeExceptionally(
                        new WorldCreationError("Error generating worlds: " + e.getMessage()));
            }
        });

        return completionFuture;
    }

    /**
     * Generates a single world on the main thread.
     */
    private void createWorld(String name, World.Environment env, long seed, String type) {
        try {
            World world = new WorldCreator(name).environment(env).seed(seed).createWorld();
            if (world == null) {
                completionFuture.completeExceptionally(
                        new WorldCreationError("Failed to create world: " + name));
                return;
            }
            world.setDifficulty(Difficulty.HARD);

            switch (type) {
                case "overworld": gameSession.setOverworld(world); break;
                case "nether":    gameSession.setNether(world);    break;
                case "end":       gameSession.setEnd(world);       break;
            }

            HardcoreMP.getPlugin().getLogger().info("World " + name + " generated at " + world.getWorldFolder());

            if (successCount.incrementAndGet() == 3) {
                GameData.setCreateTime(gameSession.getCreatedTime());
                // The seed is what lets the next startup recognise this same world
                GameData.setWorldSeed(seed);
                GameData.save();
                completionFuture.complete(gameSession);
            }
        } catch (Exception e) {
            completionFuture.completeExceptionally(
                    new WorldCreationError("Error creating world " + name + ": " + e.getMessage()));
        }
    }
}
