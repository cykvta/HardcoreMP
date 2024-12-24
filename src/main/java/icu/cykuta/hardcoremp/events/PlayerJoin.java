package icu.cykuta.hardcoremp.events;

import icu.cykuta.hardcoremp.HardcoreMP;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Check if player is in the lobby world
        Player player = event.getPlayer();
        player.setRespawnLocation(HardcoreMP.getWorldManager().getLobbyWorld().getSpawnLocation());

        if (player.getWorld().getName().equals("world")) {
            player.teleport(HardcoreMP.getWorldManager().getGameWorld().getSpawnLocation());
        }
    }
}
