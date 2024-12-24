package icu.cykuta.hardcoremp.events;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.utils.Chat;
import icu.cykuta.hardcoremp.utils.Massive;
import icu.cykuta.hardcoremp.world.WorldManager;
import icu.cykuta.hardcoremp.world.WorldStatus;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerDeath implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Get the player who died
        Player eventPlayer = event.getEntity();
        WorldManager worldManager = HardcoreMP.getWorldManager();

        worldManager.setStatus(WorldStatus.NOT_READY);

        // Send title to all players
        Massive.title("&cMUERTE", eventPlayer.getName() + " ha muerto.");

        // Set all players to spectator mode
        Massive.setGameMode(GameMode.SPECTATOR);

        // Clear inventory
        Massive.clearInventory();

        // Send all players to the lobby world
        Massive.teleport(
                worldManager.getLobbyWorld().getSpawnLocation());

        // Broadcast the waiting message
        Chat.broadcast("&cReiniciando el mundo de juego, espera un momento...");

        // Delay 5 seconds to ensure all players are teleported to the lobby world before deleting the game world
        Bukkit.getScheduler().runTaskLater(HardcoreMP.getPlugin(), () -> {
            // Regenerate the game world
            worldManager.setStatus(WorldStatus.REGENERATING);
            HardcoreMP.getWorldManager().regenGameWorld();

            // Teleport all players to the new game world after 5 seconds and set them to survival mode
            Bukkit.getScheduler().runTaskLater(HardcoreMP.getPlugin(), () -> {
                Chat.broadcast("&aEl mundo de juego ha sido creado con éxito, teletransportando a los jugadores...");
                Massive.teleport(worldManager.getGameWorld().getSpawnLocation());

                // Set the world status to ready
                worldManager.setStatus(WorldStatus.READY);
            }, 5 * 20L);

        }, 5 * 20L);
    }

    @EventHandler
    public void onPlayerDeath(PlayerRespawnEvent event) {
        Player eventPlayer = event.getPlayer();
        WorldManager worldManager = HardcoreMP.getWorldManager();

        if (worldManager.getStatus() == WorldStatus.READY) {
            eventPlayer.sendMessage(Chat.color("&aVas a ser teletransportado al mundo de juego en 5 segundos..."));

            Bukkit.getScheduler().runTaskLater(HardcoreMP.getPlugin(), () -> {
                eventPlayer.teleport(worldManager.getGameWorld().getSpawnLocation());
                eventPlayer.setGameMode(GameMode.SURVIVAL);
            }, 5 * 20L);

        }
    }
}
