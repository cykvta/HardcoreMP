package icu.cykuta.hardcoremp.command;

import icu.cykuta.hardcoremp.HardcoreMP;
import icu.cykuta.hardcoremp.config.LangManager;
import icu.cykuta.hardcoremp.utils.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
            default:
                LangManager.sendMessage(sender, "unknown-command");
        }

        return true;
    }

    private void reload() {
        HardcoreMP.getConfigFile().reload();
        LangManager.sendMessage(sender, "reload");
    }

    private void sendHelp() {
        sender.sendMessage(Chat.color("&a--------------------"));
        sender.sendMessage(Chat.color("&6HardcoreMP &7- &fA hardcore plugin."));
        sender.sendMessage(Chat.color("&6/hcmp help &7- &fShow help message."));
        sender.sendMessage(Chat.color("&6/hcmp reload &7- &fReload the plugin."));
        sender.sendMessage(Chat.color("&a--------------------"));
    }
}
