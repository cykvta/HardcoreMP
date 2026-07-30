package icu.cykuta.hardcoremp.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class SpawnUtils {

    private static final int SEARCH_RADIUS = 16;
    private static final int MAX_Y_SEARCH = 10;

    /**
     * Devuelve una ubicación segura cerca del spawn del mundo.
     * El jugador quedará centrado en el bloque (no en la esquina de 4 bloques),
     * con los pies sobre un bloque sólido y la cabeza en aire.
     *
     * @param world El mundo donde buscar
     * @return Location segura, o el spawn original si no se encuentra nada mejor
     */
    public static Location getSafeSpawn(World world) {
        Location spawn = world.getSpawnLocation();
        return findSafeLocation(spawn);
    }

    /**
     * Busca una ubicación segura partiendo de una location base.
     */
    public static Location findSafeLocation(Location origin) {
        World world = origin.getWorld();
        if (world == null) return origin;

        // Primero intentar ajustar verticalmente el spawn original
        Location adjusted = adjustVertically(origin);
        if (adjusted != null) return center(adjusted);

        // Si no, buscar en espiral alrededor del spawn
        for (int r = 1; r <= SEARCH_RADIUS; r++) {
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    // Solo el borde del cuadrado (espiral cuadrada)
                    if (Math.abs(x) != r && Math.abs(z) != r) continue;

                    Location candidate = origin.clone().add(x, 0, z);
                    Location safe = adjustVertically(candidate);
                    if (safe != null) return center(safe);
                }
            }
        }

        // Fallback: devolver spawn centrado aunque no sea ideal
        return center(origin);
    }

    /**
     * Busca verticalmente desde la columna dada una posición donde:
     * - El bloque de los pies sea sólido
     * - Los dos bloques de encima (pies y cabeza) sean transpasables
     * - No sea lava ni fuego
     */
    private static Location adjustVertically(Location base) {
        World world = base.getWorld();
        if (world == null) return null;

        int x = base.getBlockX();
        int z = base.getBlockZ();
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 2;

        // Buscar desde la Y actual hacia arriba y abajo
        int startY = Math.max(minY + 1, Math.min(base.getBlockY(), maxY));

        // Buscar hacia arriba primero
        for (int y = startY; y <= maxY; y++) {
            Location candidate = new Location(world, x, y, z);
            if (isSafe(candidate)) return candidate;
        }

        // Luego hacia abajo
        for (int y = startY - 1; y > minY; y--) {
            Location candidate = new Location(world, x, y, z);
            if (isSafe(candidate)) return candidate;
        }

        return null;
    }

    /**
     * Verifica si una ubicación es segura:
     * - Bloque de abajo: sólido, no lava, no fuego
     * - Bloque de los pies: transitable (no sólido)
     * - Bloque de la cabeza: transitable (no sólido)
     */
    private static boolean isSafe(Location loc) {
        World world = loc.getWorld();
        if (world == null) return false;

        Block feet  = world.getBlockAt(loc);
        Block head  = world.getBlockAt(loc.clone().add(0, 1, 0));
        Block floor = world.getBlockAt(loc.clone().add(0, -1, 0));

        // El suelo debe ser sólido y seguro
        if (!floor.getType().isSolid()) return false;
        if (isDangerous(floor.getType())) return false;

        // Los pies y la cabeza deben ser pasables (no sólidos)
        if (isSolid(feet.getType())) return false;
        if (isSolid(head.getType())) return false;

        return true;
    }

    /**
     * Materiales que no se pueden pisar aunque sean "sólidos"
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
     * Materiales que el jugador puede ocupar sin sofocarse
     */
    private static boolean isSolid(Material mat) {
        if (mat == Material.AIR || mat == Material.CAVE_AIR || mat == Material.VOID_AIR) return false;
        if (mat == Material.LAVA || mat == Material.FIRE || mat == Material.SOUL_FIRE) return true;
        return mat.isSolid();
    }

    /**
     * Centra la ubicación en el medio del bloque (0.5, 0.5)
     * y ajusta el yaw a 0 para que mire al frente.
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



