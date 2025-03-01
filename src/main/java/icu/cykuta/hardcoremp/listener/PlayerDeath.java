package icu.cykuta.hardcoremp.listener;

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

        // Send title to all players
        Massive.title(
                LangManager.getLang("death-title"),
                LangManager.getLang("death-subtitle").replace("{player}", eventPlayer.getName())
        );

        // Clear inventory
        Massive.clearInventory();

        // Regen statistics
        Massive.regenStats();

        // Set all players gamemode to spectator
        Massive.setGameMode(GameMode.SPECTATOR);

        // If world is not ready, return
        if (worldManager.getStatus() == WorldStatus.REGENERATING) {
            return;
        }

        // Set the world status to regenerating
        worldManager.setStatus(WorldStatus.REGENERATING);

        Bukkit.getScheduler().runTaskLater(HardcoreMP.getPlugin(), () -> {
            // Kick players
            Massive.kick(LangManager.getLang("kick"));

            // Regenerate the game world
            HardcoreMP.getWorldManager().regenGameWorld();

            // Set the world status to ready
            worldManager.setStatus(WorldStatus.READY);
        }, 5 * 20L);
    }
}
