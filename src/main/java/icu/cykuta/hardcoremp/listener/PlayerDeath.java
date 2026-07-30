package icu.cykuta.hardcoremp.listener;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.config.LangManager;
import icu.cykuta.hardcoremp.config.Setting;
import icu.cykuta.hardcoremp.utils.Chat;
import icu.cykuta.hardcoremp.world.GameSession;
import icu.cykuta.hardcoremp.world.WorldManager;
import icu.cykuta.hardcoremp.world.WorldStatus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeath implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player eventPlayer = event.getEntity();

        if (Setting.isPlayerInBypassList(eventPlayer)) {
            LangManager.sendMessage(eventPlayer, "bypass");
            return;
        }

        WorldManager worldManager = HardcoreMP.getWorldManager();
        GameSession gameSession = worldManager.getGameSession();

        // Hay vidas restantes — quitar una vida y avisar
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

        // Sin vidas — avisar y resetear
        if (worldManager.getStatus() == WorldStatus.REGENERATING) return;

        Chat.massTitle(
                LangManager.getLang("death-title"),
                LangManager.getLang("death-subtitle").replace("{player}", eventPlayer.getName())
        );

        // Auto-respawn después de 1 tick para salir de la pantalla de muerte
        // WorldManager se encarga de: stats reset, modo espectador, limbo, crear nuevo mundo y teletransportar
        Bukkit.getScheduler().runTaskLater(HardcoreMP.getPlugin(), () -> {
            eventPlayer.spigot().respawn();
            worldManager.regenGameWorld();
        }, 1L);
    }
}
