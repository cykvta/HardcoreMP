package icu.cykuta.hardcoremp.listener;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.config.LangManager;
import icu.cykuta.hardcoremp.config.Setting;
import icu.cykuta.hardcoremp.world.WorldManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class Motd implements Listener {
    @EventHandler
    public void onPing(ServerListPingEvent event) {
        if (!(Setting.isMotdEnabled())) {
            return;
        }

        WorldManager worldManager = HardcoreMP.getWorldManager();

        switch (worldManager.getStatus()) {
            case READY:
                event.setMotd(LangManager.getLang("motd.ready"));
                break;
            case REGENERATING:
                event.setMotd(LangManager.getLang("motd.not-ready"));
                break;
            default:
                event.setMotd(LangManager.getLang("motd.unknown"));
        }
    }
}
