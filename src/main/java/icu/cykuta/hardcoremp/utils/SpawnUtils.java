package icu.cykuta.hardcoremp.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class SpawnUtils {

    private static final int SEARCH_RADIUS = 16;
    private static final int MAX_Y_SEARCH = 10;

    /**
     * Returns a safe location close to the spawn of the world.
     * The player ends up centered on the block (not on the corner of four blocks),
     * standing on a solid block with air at head height.
     *
     * @param world the world to search in
     * @return a safe location, or the original spawn if nothing better is found
     */
    public static Location getSafeSpawn(World world) {
        Location spawn = world.getSpawnLocation();
        return findSafeLocation(spawn);
    }

    /**
     * Looks for a safe location starting from a base one.
     */
    public static Location findSafeLocation(Location origin) {
        World world = origin.getWorld();
        if (world == null) return origin;

        // First try to adjust the original spawn vertically
        Location adjusted = adjustVertically(origin);
        if (adjusted != null) return center(adjusted);

        // Otherwise search in a square spiral around the spawn
        for (int r = 1; r <= SEARCH_RADIUS; r++) {
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    // Only the edge of the square
                    if (Math.abs(x) != r && Math.abs(z) != r) continue;

                    Location candidate = origin.clone().add(x, 0, z);
                    Location safe = adjustVertically(candidate);
                    if (safe != null) return center(safe);
                }
            }
        }

        // Fallback: return the centered spawn even if it is not ideal
        return center(origin);
    }

    /**
     * Scans the given column for a position where:
     * - the block below is solid
     * - the feet and head blocks are passable
     * - it is neither lava nor fire
     */
    private static Location adjustVertically(Location base) {
        World world = base.getWorld();
        if (world == null) return null;

        int x = base.getBlockX();
        int z = base.getBlockZ();
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 2;

        // Search from the current Y upwards and then downwards
        int startY = Math.max(minY + 1, Math.min(base.getBlockY(), maxY));

        // Upwards first
        for (int y = startY; y <= maxY; y++) {
            Location candidate = new Location(world, x, y, z);
            if (isSafe(candidate)) return candidate;
        }

        // Then downwards
        for (int y = startY - 1; y > minY; y--) {
            Location candidate = new Location(world, x, y, z);
            if (isSafe(candidate)) return candidate;
        }

        return null;
    }

    /**
     * Checks whether a location is safe:
     * - block below: solid, no lava, no fire
     * - feet block: passable (not solid)
     * - head block: passable (not solid)
     */
    private static boolean isSafe(Location loc) {
        World world = loc.getWorld();
        if (world == null) return false;

        Block feet  = world.getBlockAt(loc);
        Block head  = world.getBlockAt(loc.clone().add(0, 1, 0));
        Block floor = world.getBlockAt(loc.clone().add(0, -1, 0));

        // The floor has to be solid and safe
        if (!floor.getType().isSolid()) return false;
        if (isDangerous(floor.getType())) return false;

        // Feet and head have to be passable (not solid)
        if (isSolid(feet.getType())) return false;
        if (isSolid(head.getType())) return false;

        return true;
    }

    /**
     * Materials that cannot be stood on even though they are "solid"
     */
    private static boolean isDangerous(Material mat) {
        switch (mat) {
            case LAVA:
            case FIRE:
            case MAGMA_BLOCK:
            case CAMPFIRE:
            case SOUL_CAMPFIRE:
            case SOUL_FIRE:
            case SWEET_BERRY_BUSH:
            case CACTUS:
                return true;
            default:
                return false;
        }
    }

    /**
     * Materials the player can occupy without suffocating
     */
    private static boolean isSolid(Material mat) {
        if (mat == Material.AIR || mat == Material.CAVE_AIR || mat == Material.VOID_AIR) return false;
        if (mat == Material.LAVA || mat == Material.FIRE || mat == Material.SOUL_FIRE) return true;
        return mat.isSolid();
    }

    /**
     * Centers the location in the middle of the block (0.5, 0.5)
     * and sets the yaw to 0 so the player looks straight ahead.
     */
    private static Location center(Location loc) {
        return new Location(
                loc.getWorld(),
                loc.getBlockX() + 0.5,
                loc.getBlockY(),
                loc.getBlockZ() + 0.5,
                0f, 0f
        );
    }
}



