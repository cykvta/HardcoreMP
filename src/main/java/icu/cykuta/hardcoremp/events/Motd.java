package icu.cykuta.hardcoremp.events;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.world.WorldManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class Motd implements Listener {
    @EventHandler
    public void onPing(ServerListPingEvent event) {
        WorldManager worldManager = HardcoreMP.getWorldManager();

        switch (worldManager.getStatus()) {
            case READY:
                event.setMotd("El mundo de juego está listo.");
                break;
            case REGENERATING:
                event.setMotd("El mundo de juego se está regenerando.");
                break;
        }
    }
}
