package icu.cykuta.hardcoremp.command;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.config.LangManager;
import icu.cykuta.hardcoremp.utils.Chat;
import icu.cykuta.hardcoremp.world.GameSession;
import icu.cykuta.hardcoremp.world.WorldManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HcmpCommand implements CommandExecutor {
    private CommandSender sender;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        this.sender = sender;

        if (args.length == 0) {
            this.sendHelp();
            return true;
        }

        switch (args[0]) {
            case "help":
                this.sendHelp();
                break;
            case "reload":
                this.reload();
                break;
            case "info":
                this.info();
                break;
            default:
                LangManager.sendMessage(sender, "unknown-command");
        }

        return true;
    }

    private void reload() {
        HardcoreMP.getConfigFile().reload();
        LangManager.sendMessage(sender, "reload");
    }

    private void info() {
        if (!(sender instanceof Player)) {
            LangManager.sendMessage(sender, "info.only-players");
            return;
        }

        Player player = (Player) sender;
        World currentWorld = player.getWorld();
        WorldManager worldManager = HardcoreMP.getWorldManager();
        GameSession gameSession = worldManager.getGameSession();

        if (gameSession == null || gameSession.getOverworld() == null) {
            LangManager.sendMessage(sender, "info.no-session");
            return;
        }

        long seed = currentWorld.getSeed();
        long time = currentWorld.getTime();
        boolean hasStorm = currentWorld.hasStorm();
        boolean isThundering = currentWorld.isThundering();
        Location playerLoc = player.getLocation();
        Location spawnLoc = currentWorld.getSpawnLocation();

        // Hora del día
        String timeOfDay;
        if (time < 6000)        timeOfDay = LangManager.getLang("info.time-day");
        else if (time < 12000)  timeOfDay = LangManager.getLang("info.time-sunset");
        else if (time < 18000)  timeOfDay = LangManager.getLang("info.time-night");
        else                    timeOfDay = LangManager.getLang("info.time-dawn");

        // Clima
        String weather;
        if (isThundering)       weather = LangManager.getLang("info.weather-thunder");
        else if (hasStorm)      weather = LangManager.getLang("info.weather-rain");
        else                    weather = LangManager.getLang("info.weather-clear");

        // Tipo de mundo
        String worldType;
        if (currentWorld.equals(gameSession.getOverworld()))                                    worldType = LangManager.getLang("info.type-overworld");
        else if (currentWorld.equals(gameSession.getNether()))                                  worldType = LangManager.getLang("info.type-nether");
        else if (currentWorld.equals(gameSession.getEnd()))                                     worldType = LangManager.getLang("info.type-end");
        else if (currentWorld.getName().equalsIgnoreCase(worldManager.getLobbyWorldName()))     worldType = LangManager.getLang("info.type-lobby");
        else                                                                                    worldType = LangManager.getLang("info.type-unknown");

        sender.sendMessage(LangManager.getLang("info.separator"));
        sender.sendMessage(LangManager.getLang("info.title"));
        sender.sendMessage(LangManager.getLang("info.separator"));
        sender.sendMessage(LangManager.getLang("info.world").replace("{world}", currentWorld.getName()));
        sender.sendMessage(LangManager.getLang("info.seed").replace("{seed}", String.valueOf(seed)));
        sender.sendMessage(LangManager.getLang("info.difficulty").replace("{difficulty}", currentWorld.getDifficulty().toString()));
        sender.sendMessage(LangManager.getLang("info.location")
                .replace("{x}", String.valueOf(playerLoc.getBlockX()))
                .replace("{y}", String.valueOf(playerLoc.getBlockY()))
                .replace("{z}", String.valueOf(playerLoc.getBlockZ())));
        sender.sendMessage(LangManager.getLang("info.spawn")
                .replace("{x}", String.valueOf(spawnLoc.getBlockX()))
                .replace("{y}", String.valueOf(spawnLoc.getBlockY()))
                .replace("{z}", String.valueOf(spawnLoc.getBlockZ())));
        sender.sendMessage(LangManager.getLang("info.time")
                .replace("{time}", timeOfDay)
                .replace("{tick}", String.valueOf(time)));
        sender.sendMessage(LangManager.getLang("info.weather").replace("{weather}", weather));
        sender.sendMessage(LangManager.getLang("info.world-type").replace("{type}", worldType));
        sender.sendMessage(LangManager.getLang("info.lives").replace("{lives}", String.valueOf(gameSession.getLives())));
        sender.sendMessage(LangManager.getLang("info.separator"));
    }

    private void sendHelp() {
        sender.sendMessage(Chat.color("&a=================================================="));
        sender.sendMessage(Chat.color("&6HardcoreMP &7- &fA hardcore plugin."));
        sender.sendMessage(Chat.color("&6/hcmp help &7- &fMostrar este mensaje."));
        sender.sendMessage(Chat.color("&6/hcmp reload &7- &fRecargar el plugin."));
        sender.sendMessage(Chat.color("&6/hcmp info &7- &fMostrar información del mundo."));
        sender.sendMessage(Chat.color("&a=================================================="));
    }
}
