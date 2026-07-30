package icu.cykuta.hardcoremp.world;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.config.Setting;
import icu.cykuta.hardcoremp.utils.Chat;
import icu.cykuta.hardcoremp.utils.SpawnUtils;
import icu.cykuta.hardcoremp.utils.Stats;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class WorldManager {

    private static final String LIMBO_WORLD = "hcmp_limbo";

    private final String lobbyWorldName;
    private GameSession gameSession;
    private WorldStatus status = WorldStatus.READY;
    private final List<Runnable> worldReadyCallbacks = new ArrayList<>();

    private World limboWorld;
    private int currentResetId = 0;
    private final NamespacedKey lastResetIdKey;

    public WorldManager(String lobbyWorldName) {
        this.lobbyWorldName = lobbyWorldName;
        this.lastResetIdKey = new NamespacedKey(HardcoreMP.getPlugin(), "last_reset_id");
        this.currentResetId = Setting.getResetId();
    }

    public void loadWorlds() throws IllegalArgumentException {
        World lobbyWorld = Bukkit.getWorld(lobbyWorldName);
        if (lobbyWorld == null) throw new IllegalArgumentException("Lobby world does not exist.");
        lobbyWorld.setDifficulty(Difficulty.PEACEFUL);

        // Crear/cargar limbo (siempre existe)
        limboWorld = loadOrCreateLimbo();

        // Verificar si las carpetas de los mundos existen en disco
        java.io.File serverRoot = Bukkit.getWorldContainer();
        boolean overworldExists = new java.io.File(serverRoot, AsyncWorldGenerator.OVERWORLD_NAME).exists();
        boolean netherExists    = new java.io.File(serverRoot, AsyncWorldGenerator.NETHER_NAME).exists();
        boolean endExists       = new java.io.File(serverRoot, AsyncWorldGenerator.END_NAME).exists();

        if (overworldExists && netherExists && endExists) {
            // Cargar mundos existentes desde disco (en hilo principal)
            try {
                World overworld = new WorldCreator(AsyncWorldGenerator.OVERWORLD_NAME)
                        .environment(World.Environment.NORMAL).createWorld();
                World nether    = new WorldCreator(AsyncWorldGenerator.NETHER_NAME)
                        .environment(World.Environment.NETHER).createWorld();
                World end       = new WorldCreator(AsyncWorldGenerator.END_NAME)
                        .environment(World.Environment.THE_END).createWorld();

                if (overworld != null && nether != null && end != null) {
                    this.gameSession = new GameSession()
                            .setOverworld(overworld)
                            .setNether(nether)
                            .setEnd(end)
                            .setCreatedTime(Setting.getCreateTime());
                    this.status = WorldStatus.READY;
                    Chat.broadcast("Game world loaded successfully.");
                } else {
                    Chat.broadcast("Failed to load worlds, generating new ones.");
                    this.status = WorldStatus.GENERATING;
                    this.createGameSessionAsync();
                }
            } catch (Exception e) {
                Chat.broadcast("Error loading worlds: " + e.getMessage() + ". Generating new ones.");
                this.status = WorldStatus.GENERATING;
                this.createGameSessionAsync();
            }
        } else {
            // Primera vez o mundos borrados externamente
            Chat.broadcast("Game worlds not found, creating them now.");
            this.status = WorldStatus.GENERATING;
            this.createGameSessionAsync();
        }
    }

    private World loadOrCreateLimbo() {
        World existing = Bukkit.getWorld(LIMBO_WORLD);
        if (existing != null) return existing;

        return new WorldCreator(LIMBO_WORLD)
                .type(WorldType.FLAT)
                .generateStructures(false)
                .generatorSettings("{\"biome\":\"minecraft:the_end\",\"layers\":[{\"block\":\"minecraft:air\",\"height\":1}]}")
                .createWorld();
    }

    private void createGameSessionAsync() {
        this.status = WorldStatus.GENERATING;
        AsyncWorldGenerator generator = new AsyncWorldGenerator();
        CompletableFuture<GameSession> future = generator.generateWorldsAsync();

        future.whenComplete((session, throwable) -> {
            if (throwable != null) {
                Chat.broadcast("Error creating game world: " + throwable.getMessage());
                this.status = WorldStatus.GENERATION_FAILED;
                HardcoreMP.disablePlugin("Failed to create game world");
            } else {
                this.gameSession = session;
                this.status = WorldStatus.READY;
                Chat.broadcast("Game world created successfully!");
                notifyWorldReady();
            }
        });
    }

    /**
     * Resetea el mundo: teletransporta a todos al limbo,
     * elimina los mundos uno a uno (patrón Fahare) y crea nuevos.
     */
    public void regenGameWorld() {
        if (this.status == WorldStatus.REGENERATING) return;
        this.status = WorldStatus.REGENERATING;

        // Incrementar reset ID y guardarlo
        currentResetId++;
        Setting.setResetId(currentResetId);
        Setting.saveConfig();

        // Teletransportar todos los jugadores al limbo en espectador
        Location limboSpawn = getLimboSpawn();
        Bukkit.getOnlinePlayers().forEach(p -> {
            Stats.regenStats(p);
            p.setGameMode(GameMode.SPECTATOR);
            p.teleport(limboSpawn);
        });

        // Eliminar mundos uno por uno con delay entre cada uno (patrón Fahare)
        List<World> worlds = new ArrayList<>();
        worlds.add(gameSession.getOverworld());
        worlds.add(gameSession.getNether());
        worlds.add(gameSession.getEnd());

        deleteNextWorld(worlds);
    }

    /**
     * Patrón de Fahare: elimina mundos uno por uno con un tick de delay entre cada uno.
     */
    private void deleteNextWorld(List<World> worlds) {
        if (worlds.isEmpty()) {
            // Todos eliminados → crear nuevos
            createGameSessionAsync();
            return;
        }

        Bukkit.getScheduler().runTask(HardcoreMP.getPlugin(), () -> {
            World world = worlds.remove(0);
            java.io.File worldFolder = world.getWorldFolder();

            if (Bukkit.unloadWorld(world, false)) {
                if (Setting.removeOldWorlds()) {
                    try {
                        deleteDirectory(worldFolder.toPath());
                    } catch (IOException e) {
                        Chat.broadcast("Error deleting world " + world.getName() + ": " + e.getMessage());
                    }
                } else {
                    // Backup asíncrono
                    Bukkit.getScheduler().runTaskAsynchronously(HardcoreMP.getPlugin(), () -> {
                        try {
                            icu.cykuta.hardcoremp.utils.File.createWorldsZip(new World[]{ world });
                        } catch (IOException e) {
                            Chat.broadcast("Error creating backup: " + e.getMessage());
                        }
                    });
                }
            } else {
                Chat.broadcast("Failed to unload world: " + world.getName());
            }

            // Siguiente mundo en el próximo tick
            Bukkit.getScheduler().runTask(HardcoreMP.getPlugin(), () -> deleteNextWorld(worlds));
        });
    }

    private void deleteDirectory(Path path) throws IOException {
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.delete(p); } catch (IOException ignored) {}
            });
        }
    }

    public void registerWorldReadyCallback(Runnable callback) {
        if (this.status == WorldStatus.READY && this.gameSession != null) {
            callback.run();
        } else {
            this.worldReadyCallbacks.add(callback);
        }
    }

    private void notifyWorldReady() {
        // Teletransportar a todos los jugadores que están en el limbo al nuevo mundo
        if (limboWorld != null) {
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.getWorld().equals(limboWorld))
                    .forEach(p -> {
                        p.setGameMode(GameMode.SURVIVAL);
                        p.teleport(SpawnUtils.getSafeSpawn(gameSession.getOverworld()));
                        markPlayerReset(p);
                    });
        }

        // Ejecutar callbacks individuales (jugadores que se unieron durante la generación)
        for (Runnable callback : worldReadyCallbacks) {
            try { callback.run(); } catch (Exception e) {
                Chat.broadcast("Error in world ready callback: " + e.getMessage());
            }
        }
        worldReadyCallbacks.clear();
    }

    /** Marca que el jugador ha sido procesado con el reset ID actual. */
    public void markPlayerReset(Player player) {
        player.getPersistentDataContainer().set(lastResetIdKey, PersistentDataType.INTEGER, currentResetId);
    }

    /** Devuelve si el jugador se perdió un reset (estaba offline). */
    public boolean playerMissedReset(Player player) {
        int playerLastReset = player.getPersistentDataContainer()
                .getOrDefault(lastResetIdKey, PersistentDataType.INTEGER, 0);
        return playerLastReset < currentResetId;
    }

    public Location getLimboSpawn() {
        return new Location(limboWorld, 0.5, 100, 0.5);
    }

    public GameSession getGameSession() { return this.gameSession; }
    public String getLobbyWorldName()   { return lobbyWorldName; }
    public WorldStatus getStatus()      { return status; }
    public void setStatus(WorldStatus status) { this.status = status; }
    public int getCurrentResetId()      { return currentResetId; }
    public World getLimboWorld()        { return limboWorld; }
}
