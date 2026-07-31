package icu.cykuta.hardcoremp.listener;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.config.LangManager;
import icu.cykuta.hardcoremp.config.Setting;
import icu.cykuta.hardcoremp.utils.SpawnUtils;
import icu.cykuta.hardcoremp.utils.Stats;
import icu.cykuta.hardcoremp.world.GameSession;
import icu.cykuta.hardcoremp.world.WorldManager;
import icu.cykuta.hardcoremp.world.WorldStatus;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        WorldManager worldManager = HardcoreMP.getWorldManager();
        Player player = event.getPlayer();

        // Fahare pattern: detect players that were offline during a reset
        if (worldManager.playerMissedReset(player)) {
            // Only wipe them when the admin asked for it in the config
            if (Setting.isOfflinePlayerInventoryClearEnabled()) {
                Stats.regenStats(player);
            }

            if (worldManager.getStatus() == WorldStatus.READY) {
                // World ready, teleport straight away
                worldManager.markPlayerReset(player);
                handlePlayerReady(worldManager, player);
            } else {
                // World being generated, wait in the limbo
                sendToLimbo(worldManager, player);
                worldManager.registerWorldReadyCallback(() -> {
                    if (player.isOnline()) {
                        worldManager.markPlayerReset(player);
                        handlePlayerReady(worldManager, player);
                    }
                });
            }
            return;
        }

        // Regular player (did not miss any reset)
        if (worldManager.getStatus() == WorldStatus.READY) {
            worldManager.markPlayerReset(player);
            handlePlayerReady(worldManager, player);
        } else {
            // World being generated, wait in the limbo as a spectator
            sendToLimbo(worldManager, player);
            worldManager.registerWorldReadyCallback(() -> {
                if (player.isOnline()) {
                    worldManager.markPlayerReset(player);
                    handlePlayerReady(worldManager, player);
                }
            });
        }
    }

    private void sendToLimbo(WorldManager worldManager, Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(worldManager.getLimboSpawn());
        LangManager.sendMessage(player, "world-generating");
    }

    /**
     * Moves the player into the game world.
     */
    private void handlePlayerReady(WorldManager worldManager, Player player) {
        GameSession gameSession = worldManager.getGameSession();

        // NOTE: the inventory is no longer cleared by comparing getLastPlayed() with the
        // creation time of the world. That check emptied the inventory of every player
        // whenever the world was loaded with a fresh creation time (for instance after a
        // server restart), even though nobody had died. The reset id is the only source
        // of truth, and it is handled in onPlayerJoin.

        // Teleport to the game world when the player is in the lobby or the limbo
        String worldName = player.getWorld().getName();
        boolean isInLobbyOrLimbo = worldName.equalsIgnoreCase(worldManager.getLobbyWorldName())
                || player.getWorld().equals(worldManager.getLimboWorld());

        if (isInLobbyOrLimbo) {
            Bukkit.getScheduler().runTaskLater(HardcoreMP.getPlugin(), () -> {
                if (!player.isOnline()) return;
                // Survival only after the teleport: the limbo is a void world and the
                // player would start falling during the tick in between.
                player.teleport(SpawnUtils.getSafeSpawn(gameSession.getOverworld()));
                player.setGameMode(GameMode.SURVIVAL);
            }, 1);
        }
    }
}
