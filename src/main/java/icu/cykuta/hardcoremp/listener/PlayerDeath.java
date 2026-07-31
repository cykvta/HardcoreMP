package icu.cykuta.hardcoremp.listener;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.config.LangManager;
import icu.cykuta.hardcoremp.config.Setting;
import icu.cykuta.hardcoremp.utils.Chat;
import icu.cykuta.hardcoremp.world.GameSession;
import icu.cykuta.hardcoremp.world.WorldManager;
import icu.cykuta.hardcoremp.world.WorldStatus;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeath implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player eventPlayer = event.getEntity();

        WorldManager worldManager = HardcoreMP.getWorldManager();
        GameSession gameSession = worldManager.getGameSession();

        // No session yet (the world is still being generated) or a reset is already running
        if (gameSession == null) return;
        if (worldManager.getStatus() != WorldStatus.READY) return;

        // Only deaths inside the game worlds count. The lobby is a waiting area kept
        // on peaceful difficulty, so dying there must not cost a life.
        if (!isGameWorld(gameSession, eventPlayer.getWorld())) return;

        if (Setting.isPlayerInBypassList(eventPlayer)) {
            LangManager.sendMessage(eventPlayer, "bypass");
            return;
        }

        gameSession.removeLife();

        // Lives left: announce how many remain
        if (gameSession.getLives() > 0) {
            Chat.massTitle(
                    LangManager.getLang("lives-left")
                            .replace("{lives}", String.valueOf(gameSession.getLives()))
                            .replace("{max-lives}", String.valueOf(Setting.getMaxLives())),
                    LangManager.getLang("death-subtitle").replace("{player}", eventPlayer.getName())
            );
            return;
        }

        // No lives left: announce it and reset the world
        Chat.massTitle(
                LangManager.getLang("death-title"),
                LangManager.getLang("death-subtitle").replace("{player}", eventPlayer.getName())
        );

        // Auto-respawn one tick later so the player leaves the death screen.
        // WorldManager takes care of the stats reset, spectator mode, limbo,
        // world generation and the final teleport.
        Bukkit.getScheduler().runTaskLater(HardcoreMP.getPlugin(), () -> {
            try {
                if (eventPlayer.isOnline() && eventPlayer.isDead()) eventPlayer.spigot().respawn();
            } catch (Throwable t) {
                // Auto-respawn is a convenience; the reset is not. A server where this
                // call is gone used to swallow the reset along with it.
                HardcoreMP.getPlugin().getLogger().warning(
                        "Could not auto-respawn " + eventPlayer.getName() + ": " + t);
            }
            worldManager.regenGameWorld();
        }, 1L);
    }

    private boolean isGameWorld(GameSession session, World world) {
        return world.equals(session.getOverworld())
                || world.equals(session.getNether())
                || world.equals(session.getEnd());
    }
}
