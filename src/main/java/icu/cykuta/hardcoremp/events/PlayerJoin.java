package icu.cykuta.hardcoremp.events;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.world.WorldManager;
import icu.cykuta.hardcoremp.world.WorldStatus;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Check if player is in the lobby world
        WorldManager worldManager = HardcoreMP.getWorldManager();

        if (worldManager.getStatus() != WorldStatus.READY) {
            event.getPlayer().kickPlayer("El mundo de juego aún no está listo, por favor espera...");
            return;
        }

        Player player = event.getPlayer();
        player.setGameMode(GameMode.SURVIVAL);

        if (player.getWorld().getName().equalsIgnoreCase(WorldManager.getLobbyWorldName())) {
            player.teleport(HardcoreMP.getWorldManager().getGameWorld().getSpawnLocation());
        }
    }
}
