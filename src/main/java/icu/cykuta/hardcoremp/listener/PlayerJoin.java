package icu.cykuta.hardcoremp.listener;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.config.LangManager;
import icu.cykuta.hardcoremp.config.Setting;
import icu.cykuta.hardcoremp.utils.Massive;
import icu.cykuta.hardcoremp.world.WorldManager;
import icu.cykuta.hardcoremp.world.WorldStatus;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
    private Player player;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        WorldManager worldManager = HardcoreMP.getWorldManager();

        if (worldManager.getStatus() != WorldStatus.READY) {
            event.getPlayer().kickPlayer(LangManager.getLang("world-not-ready"));
            return;
        }

        player = event.getPlayer();
        player.setGameMode(GameMode.SURVIVAL);

        MultiverseWorld gameWorld = worldManager.getGameWorld();

        // Clear player inventory if they have items from old world
        if (Setting.isOfflinePlayerInventoryClearEnabled()) {
            long worldCreationTime = worldManager.getGameWorldCreationTime();
            long playerLastJoin = player.getLastPlayed();

            if (playerLastJoin < worldCreationTime) {
                Massive.regenStats(player);
            }
        }

        // Teleport player to game world if they are in the lobby world
        if (player.getWorld().getName().equalsIgnoreCase(worldManager.getLobbyWorldName())) {
            Bukkit.getScheduler().runTaskLater(HardcoreMP.getPlugin(), () -> {
                player.teleport(gameWorld.getSpawnLocation());
            }, 1);
        }
    }
}
