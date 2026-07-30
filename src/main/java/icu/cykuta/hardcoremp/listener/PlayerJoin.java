package icu.cykuta.hardcoremp.listener;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.config.Setting;
import icu.cykuta.hardcoremp.utils.Chat;
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

        // Patrón Fahare: detectar si el jugador estuvo offline durante un reset
        if (worldManager.playerMissedReset(player)) {
            Stats.regenStats(player);

            if (worldManager.getStatus() == WorldStatus.READY) {
                // Mundo listo → teletransportar directo
                worldManager.markPlayerReset(player);
                handlePlayerReady(worldManager, player);
            } else {
                // Mundo generándose → esperar en limbo
                player.setGameMode(GameMode.SPECTATOR);
                player.teleport(worldManager.getLimboSpawn());
                player.sendMessage(Chat.color("&eEl mundo se está generando... Por favor espera."));
                worldManager.registerWorldReadyCallback(() -> {
                    if (player.isOnline()) {
                        worldManager.markPlayerReset(player);
                        handlePlayerReady(worldManager, player);
                    }
                });
            }
            return;
        }

        // Jugador normal (no se perdió ningún reset)
        if (worldManager.getStatus() == WorldStatus.READY) {
            handlePlayerReady(worldManager, player);
        } else {
            // Mundo generándose → esperar en limbo en espectador
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(worldManager.getLimboSpawn());
            player.sendMessage(Chat.color("&eEl mundo se está generando... Por favor espera."));
            worldManager.registerWorldReadyCallback(() -> {
                if (player.isOnline()) handlePlayerReady(worldManager, player);
            });
        }
    }

    /**
     * Maneja la teletransportación del jugador al mundo de juego
     */
    private void handlePlayerReady(WorldManager worldManager, Player player) {
        GameSession gameSession = worldManager.getGameSession();

        // Limpiar inventario si el jugador tiene items de un mundo anterior
        if (Setting.isOfflinePlayerInventoryClearEnabled()) {
            if (player.getLastPlayed() < gameSession.getCreatedTime()) {
                Stats.regenStats(player);
            }
        }

        // Teletransportar al mundo de juego si está en el lobby o el limbo
        String worldName = player.getWorld().getName();
        boolean isInLobbyOrLimbo = worldName.equalsIgnoreCase(worldManager.getLobbyWorldName())
                || player.getWorld().equals(worldManager.getLimboWorld());

        if (isInLobbyOrLimbo) {
            player.setGameMode(GameMode.SURVIVAL);
            Bukkit.getScheduler().runTaskLater(HardcoreMP.getPlugin(), () -> {
                if (player.isOnline()) {
                    player.teleport(SpawnUtils.getSafeSpawn(gameSession.getOverworld()));
                }
            }, 1);
        }
    }
}
