package icu.cykuta.hardcoremp.world;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.config.Setting;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Genera mundos de forma asincrónica sin bloquear el hilo principal del servidor.
 */
public class AsyncWorldGenerator {

    // Nombres fijos - siempre los mismos, no dependen de timestamp
    public static final String OVERWORLD_NAME = "hcmp_game";
    public static final String NETHER_NAME    = "hcmp_game_nether";
    public static final String END_NAME       = "hcmp_game_the_end";

    private GameSession gameSession;
    private CompletableFuture<GameSession> completionFuture;
    private final AtomicInteger successCount = new AtomicInteger(0);

    /**
     * Inicia la generación asincrónica de los tres mundos.
     * Los mundos se crean en el hilo principal (necesario para WorldInitEvent),
     * pero se configura de forma no bloqueante.
     * Retorna un CompletableFuture que se completa cuando todos los mundos están listos.
     */
    public CompletableFuture<GameSession> generateWorldsAsync() {
        this.completionFuture = new CompletableFuture<>();
        this.gameSession = new GameSession(System.currentTimeMillis());

        long seed = new Random().nextLong();

        // Crear los mundos en el hilo principal (necesario para WorldInitEvent)
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
     * Genera un mundo individual en el hilo principal.
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

            if (successCount.incrementAndGet() == 3) {
                Setting.setCreateTime(gameSession.getCreatedTime());
                Setting.saveConfig();
                completionFuture.complete(gameSession);
            }
        } catch (Exception e) {
            completionFuture.completeExceptionally(
                    new WorldCreationError("Error creating world " + name + ": " + e.getMessage()));
        }
    }
}
