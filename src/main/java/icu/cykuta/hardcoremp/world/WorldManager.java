package icu.cykuta.hardcoremp.world;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.api.event.GameWorldResetEvent;
import icu.cykuta.hardcoremp.config.GameData;
import icu.cykuta.hardcoremp.config.Setting;
import icu.cykuta.hardcoremp.utils.Chat;
import icu.cykuta.hardcoremp.utils.SpawnUtils;
import icu.cykuta.hardcoremp.utils.Stats;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class WorldManager {

    private static final String LIMBO_WORLD = "hcmp_limbo";
    private static final String BACKUP_FOLDER = "old_worlds";

    private final String lobbyWorldName;
    private GameSession gameSession;
    private WorldStatus status = WorldStatus.INITIALIZING;
    private final List<Runnable> worldReadyCallbacks = new ArrayList<>();

    private World limboWorld;
    private int currentResetId;
    private final NamespacedKey lastResetIdKey;

    public WorldManager(String lobbyWorldName) {
        this.lobbyWorldName = lobbyWorldName;
        this.lastResetIdKey = new NamespacedKey(HardcoreMP.getPlugin(), "last_reset_id");
        this.currentResetId = GameData.getResetId();
    }

    public void loadWorlds() throws IllegalArgumentException {
        World lobbyWorld = Bukkit.getWorld(lobbyWorldName);
        if (lobbyWorld == null) throw new IllegalArgumentException("Lobby world '" + lobbyWorldName + "' does not exist.");
        lobbyWorld.setDifficulty(Difficulty.PEACEFUL);

        // The limbo world always exists
        limboWorld = loadOrCreateLimbo();
        if (limboWorld == null) throw new IllegalArgumentException("The limbo world could not be created.");

        // A world only counts as existing when its folder holds a level.dat
        boolean overworldExists = worldExistsOnDisk(AsyncWorldGenerator.OVERWORLD_NAME);
        boolean netherExists    = worldExistsOnDisk(AsyncWorldGenerator.NETHER_NAME);
        boolean endExists       = worldExistsOnDisk(AsyncWorldGenerator.END_NAME);

        if (!overworldExists && !netherExists && !endExists) {
            // First run, or the worlds were deleted outside the plugin: nothing to keep.
            logger().info("Game worlds not found, creating them now.");
            beginNewWorldGeneration();
            return;
        }

        if (!(overworldExists && netherExists && endExists)) {
            // Inconsistent state. Generating here would destroy the running game,
            // so stop and let the admin decide.
            this.status = WorldStatus.GENERATION_FAILED;
            HardcoreMP.disablePlugin("Some game world folders are missing ("
                    + (overworldExists ? "" : AsyncWorldGenerator.OVERWORLD_NAME + " ")
                    + (netherExists ? "" : AsyncWorldGenerator.NETHER_NAME + " ")
                    + (endExists ? "" : AsyncWorldGenerator.END_NAME)
                    + "). The plugin will not regenerate the world automatically to avoid data loss. "
                    + "Restore the backup or delete the remaining folders to start a new game.");
            return;
        }

        // Load the existing worlds from disk (on the main thread)
        try {
            World overworld = new WorldCreator(AsyncWorldGenerator.OVERWORLD_NAME)
                    .environment(World.Environment.NORMAL).createWorld();
            World nether    = new WorldCreator(AsyncWorldGenerator.NETHER_NAME)
                    .environment(World.Environment.NETHER).createWorld();
            World end       = new WorldCreator(AsyncWorldGenerator.END_NAME)
                    .environment(World.Environment.THE_END).createWorld();

            if (overworld == null || nether == null || end == null) {
                throw new IllegalStateException("Bukkit returned null while loading the game worlds");
            }

            this.gameSession = GameSession.restore(GameData.getCreateTime())
                    .setOverworld(overworld)
                    .setNether(nether)
                    .setEnd(end);
            this.status = WorldStatus.READY;
            logger().info("Game world loaded successfully.");
        } catch (Exception e) {
            // Never regenerate when the worlds exist but fail to load: doing so used to
            // create a brand new world, which wiped the game and emptied the inventory
            // of every player on their next join.
            this.status = WorldStatus.GENERATION_FAILED;
            HardcoreMP.disablePlugin("Error loading the game worlds: " + e
                    + ". The plugin will not regenerate them automatically to avoid data loss.");
        }
    }

    /** Whether the world exists on disk and is not an empty or corrupted folder. */
    private boolean worldExistsOnDisk(String worldName) {
        java.io.File folder = new java.io.File(Bukkit.getWorldContainer(), worldName);
        return folder.isDirectory() && new java.io.File(folder, "level.dat").isFile();
    }

    /**
     * Generate a world from scratch (there is no previous game to keep).
     * The reset id is bumped so that offline players are treated as having
     * missed a reset.
     */
    private void beginNewWorldGeneration() {
        bumpResetId();
        this.status = WorldStatus.GENERATING;
        this.createGameSessionAsync();
    }

    private void bumpResetId() {
        currentResetId++;
        GameData.setResetId(currentResetId);
        GameData.save();
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
     * Reset the world: send everyone to the limbo, delete the worlds one by one
     * (Fahare pattern) and generate new ones.
     */
    public void regenGameWorld() {
        if (this.status == WorldStatus.REGENERATING) return;
        if (this.gameSession == null) {
            logger().warning("Ignoring a world reset: there is no game session yet.");
            return;
        }
        this.status = WorldStatus.REGENERATING;

        bumpResetId();

        // Move every player to the limbo as a spectator
        Location limboSpawn = getLimboSpawn();
        Bukkit.getOnlinePlayers().forEach(p -> {
            Stats.regenStats(p);
            p.setGameMode(GameMode.SPECTATOR);
            p.teleport(limboSpawn);
        });

        // Delete the worlds one by one, one tick apart (Fahare pattern)
        List<World> worlds = new ArrayList<>();
        worlds.add(gameSession.getOverworld());
        worlds.add(gameSession.getNether());
        worlds.add(gameSession.getEnd());
        worlds.removeIf(java.util.Objects::isNull);

        deleteNextWorld(worlds);
    }

    /**
     * Fahare pattern: delete the worlds one by one with a tick of delay between them.
     */
    private void deleteNextWorld(List<World> worlds) {
        if (worlds.isEmpty()) {
            // Everything deleted, generate the new worlds
            createGameSessionAsync();
            return;
        }

        Bukkit.getScheduler().runTask(HardcoreMP.getPlugin(), () -> {
            World world = worlds.remove(0);
            String worldName = world.getName();
            java.io.File worldFolder = world.getWorldFolder();

            // A player left behind blocks the unload and leaves the reset half done
            world.getPlayers().forEach(p -> {
                p.setGameMode(GameMode.SPECTATOR);
                p.teleport(getLimboSpawn());
            });

            if (Bukkit.unloadWorld(world, false)) {
                archiveWorldFolder(worldName, worldFolder);
            } else {
                // The world stays loaded, so it will be reused and the reset is not real
                logger().severe("Failed to unload world: " + worldName + ". It will not be regenerated.");
                Chat.broadcast("Failed to unload world: " + worldName);
            }

            // Next world on the following tick
            Bukkit.getScheduler().runTask(HardcoreMP.getPlugin(), () -> deleteNextWorld(worlds));
        });
    }

    /**
     * Take the folder of an unloaded world out of the way, and back it up when the
     * config asks for it.
     * <p>
     * The folder is renamed first because the rename is atomic: whatever happens
     * with the backup afterwards, the new world can never be generated on top of
     * the old one. Keeping the folder around used to make a reset silently reload
     * the previous world while every inventory had already been cleared.
     */
    private void archiveWorldFolder(String worldName, java.io.File worldFolder) {
        if (worldFolder == null || !worldFolder.exists()) return;

        boolean keepBackup = !Setting.removeOldWorlds();
        java.io.File archived = new java.io.File(worldFolder.getParentFile(),
                worldFolder.getName() + "_old_" + System.currentTimeMillis());

        if (!worldFolder.renameTo(archived)) {
            logger().warning("Could not move " + worldName + " aside, deleting it in place.");
            try {
                deleteDirectory(worldFolder.toPath());
            } catch (IOException e) {
                logger().severe("The folder of " + worldName + " could not be removed (" + e.getMessage()
                        + "). The new world may reuse it.");
            }
            return;
        }

        java.io.File backupFolder = new java.io.File(HardcoreMP.getPlugin().getDataFolder(), BACKUP_FOLDER);
        Bukkit.getScheduler().runTaskAsynchronously(HardcoreMP.getPlugin(), () -> {
            if (keepBackup) {
                try {
                    icu.cykuta.hardcoremp.utils.File.createWorldZip(backupFolder, worldName, archived);
                } catch (IOException e) {
                    logger().severe("Error creating the backup of " + worldName + ": " + e.getMessage());
                    return; // keep the folder so the admin can rescue it by hand
                }
            }

            try {
                deleteDirectory(archived.toPath());
            } catch (IOException e) {
                logger().severe("Error deleting " + archived.getName() + ": " + e.getMessage());
            }
        });
    }

    private void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;

        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.delete(p); } catch (IOException ignored) {}
            });
        }

        // Files.delete() failures are swallowed above, so confirm the result
        if (Files.exists(path)) throw new IOException("Could not fully delete " + path);
    }

    public void registerWorldReadyCallback(Runnable callback) {
        if (this.status == WorldStatus.READY && this.gameSession != null) {
            callback.run();
        } else {
            this.worldReadyCallbacks.add(callback);
        }
    }

    private void notifyWorldReady() {
        // Every online player went through this reset, so mark them all even if they
        // are not in the limbo. Otherwise they would lose their inventory after a restart.
        Bukkit.getOnlinePlayers().forEach(this::markPlayerReset);

        // Send the players waiting in the limbo to the new world
        if (limboWorld != null) {
            Location spawn = SpawnUtils.getSafeSpawn(gameSession.getOverworld());
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.getWorld().equals(limboWorld))
                    .forEach(p -> {
                        // Survival only after the teleport: the limbo is a void world
                        p.teleport(spawn);
                        p.setGameMode(GameMode.SURVIVAL);
                    });
        }

        // Run the individual callbacks (players that joined while generating)
        for (Runnable callback : worldReadyCallbacks) {
            try { callback.run(); } catch (Exception e) {
                logger().warning("Error in world ready callback: " + e.getMessage());
            }
        }
        worldReadyCallbacks.clear();

        Bukkit.getPluginManager().callEvent(new GameWorldResetEvent(gameSession.getOverworld()));
    }

    /** Mark the player as processed with the current reset id. */
    public void markPlayerReset(Player player) {
        player.getPersistentDataContainer().set(lastResetIdKey, PersistentDataType.INTEGER, currentResetId);
    }

    /**
     * Whether the player was offline while the world was reset.
     * A player without a mark is never considered as having missed one: it may be
     * their first join or an upgrade from an older version of the plugin, and
     * clearing their inventory there would be a false positive.
     */
    public boolean playerMissedReset(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (!pdc.has(lastResetIdKey, PersistentDataType.INTEGER)) return false;

        return missedReset(pdc.get(lastResetIdKey, PersistentDataType.INTEGER), currentResetId);
    }

    /**
     * Decision behind {@link #playerMissedReset(Player)}, without the Bukkit types.
     *
     * @param playerResetId  the reset id stored on the player, null when absent
     * @param currentResetId the reset id of the server
     */
    public static boolean missedReset(Integer playerResetId, int currentResetId) {
        return playerResetId != null && playerResetId < currentResetId;
    }

    /** Persist the state that has to survive a server restart. */
    public void saveState() {
        // Only while a game is running: if the server stops in the middle of a reset,
        // the players do have to be treated as having missed it.
        if (status == WorldStatus.READY) {
            Bukkit.getOnlinePlayers().forEach(this::markPlayerReset);
        }
        GameData.setResetId(currentResetId);
        if (gameSession != null) GameData.setLives(gameSession.getLives());
        GameData.save();
    }

    private Logger logger() {
        return HardcoreMP.getPlugin().getLogger();
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
