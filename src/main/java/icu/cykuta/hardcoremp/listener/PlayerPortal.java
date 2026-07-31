package icu.cykuta.hardcoremp.listener;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.utils.SpawnUtils;
import icu.cykuta.hardcoremp.world.GameSession;
import icu.cykuta.hardcoremp.world.WorldManager;
import icu.cykuta.hardcoremp.world.WorldStatus;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerPortal implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        WorldManager wm = HardcoreMP.getWorldManager();
        if (wm.getStatus() != WorldStatus.READY) return;

        GameSession session = wm.getGameSession();
        if (session == null) return;

        World from = event.getFrom().getWorld();
        if (from == null) return;

        Location to = event.getTo();
        if (to == null) return;

        PlayerTeleportEvent.TeleportCause cause = event.getCause();

        if (cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            if (from.equals(session.getOverworld())) {
                event.setTo(new Location(session.getNether(), to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch()));
            } else if (from.equals(session.getNether())) {
                event.setTo(new Location(session.getOverworld(), to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch()));
            }

        } else if (cause == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            if (from.equals(session.getOverworld())) {
                event.setTo(new Location(session.getEnd(), 100.5, 49, 0.5));
            } else if (from.equals(session.getEnd())) {
                event.setTo(SpawnUtils.getSafeSpawn(session.getOverworld()));
            }

        } else if (cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY) {
            if (from.equals(session.getEnd())) {
                event.setTo(new Location(session.getEnd(), to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent event) {
        WorldManager wm = HardcoreMP.getWorldManager();
        if (wm.getStatus() != WorldStatus.READY) return;

        GameSession session = wm.getGameSession();
        if (session == null) return;

        World from = event.getFrom().getWorld();
        if (from == null) return;

        Location to = event.getTo();
        if (to == null || to.getWorld() == null) return;

        World.Environment destEnv = to.getWorld().getEnvironment();

        // Work out the portal type from the environment of the destination world
        if (from.equals(session.getOverworld()) && destEnv == World.Environment.NETHER) {
            event.setTo(new Location(session.getNether(), to.getX(), to.getY(), to.getZ()));

        } else if (from.equals(session.getNether()) && destEnv == World.Environment.NORMAL) {
            event.setTo(new Location(session.getOverworld(), to.getX(), to.getY(), to.getZ()));

        } else if (from.equals(session.getOverworld()) && destEnv == World.Environment.THE_END) {
            event.setTo(new Location(session.getEnd(), 100.5, 49, 0.5));

        } else if (from.equals(session.getEnd()) && destEnv == World.Environment.NORMAL) {
            event.setTo(session.getOverworld().getSpawnLocation());
        }
    }

    // When a player dies in the End, respawn them in our overworld (not the vanilla one)
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        WorldManager wm = HardcoreMP.getWorldManager();
        if (wm.getStatus() != WorldStatus.READY) return;

        GameSession session = wm.getGameSession();
        if (session == null) return;

        Location respawn = event.getRespawnLocation();
        if (respawn.getWorld() == null) return;

        // Redirect when Bukkit wants to respawn the player in our End
        if (respawn.getWorld().equals(session.getEnd())) {
            event.setRespawnLocation(SpawnUtils.getSafeSpawn(session.getOverworld()));
        }
    }
}


