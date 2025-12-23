package icu.cykuta.hardcoremp.listener;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.config.LangManager;
import icu.cykuta.hardcoremp.config.Setting;
import icu.cykuta.hardcoremp.utils.Chat;
import icu.cykuta.hardcoremp.utils.Stats;
import icu.cykuta.hardcoremp.world.GameSession;
import icu.cykuta.hardcoremp.world.WorldManager;
import icu.cykuta.hardcoremp.world.WorldStatus;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeath implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Get the player who died
        Player eventPlayer = event.getEntity();

        // Check if player is in the bypass list
        if (Setting.isPlayerInBypassList(eventPlayer)) {
            LangManager.sendMessage(eventPlayer, "bypass");
            return;
        }

        WorldManager worldManager = HardcoreMP.getWorldManager();
        GameSession gameSession = HardcoreMP.getWorldManager().getGameSession();

        // check if players has lives left
        if (gameSession.getLives() > 0) {
            gameSession.removeLife();
            Chat.massTitle(
                    LangManager.getLang("lives-left")
                            .replace("{lives}", String.valueOf(gameSession.getLives()))
                            .replace("{max-lives}", String.valueOf(Setting.getMaxLives())),
                    LangManager.getLang("death-subtitle").replace("{player}", eventPlayer.getName())
            );

            return;
        }

        // Send title to all players
        Chat.massTitle(
                LangManager.getLang("death-title"),
                LangManager.getLang("death-subtitle").replace("{player}", eventPlayer.getName())
        );

        // If world is not ready, return
        if (worldManager.getStatus() == WorldStatus.REGENERATING) {
            return;
        }

        // Set the world status to regenerating
        worldManager.setStatus(WorldStatus.REGENERATING);

        // For every player
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.setGameMode(GameMode.SPECTATOR);
            Stats.regenStats(player);
        });

        // Task run after 5 seconds
        Bukkit.getScheduler().runTaskLater(HardcoreMP.getPlugin(), () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.kickPlayer(LangManager.getLang("kick"));
            });

            // Regenerate the game world
            HardcoreMP.getWorldManager().regenGameWorld();

            // Set the world status to ready
            worldManager.setStatus(WorldStatus.READY);
            }, 5 * 20L);
        }
}
