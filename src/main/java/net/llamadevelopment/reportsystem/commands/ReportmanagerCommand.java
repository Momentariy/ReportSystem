package net.llamadevelopment.reportsystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Sound;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.tools.FormWindows;
import net.llamadevelopment.reportsystem.components.tools.Language;

public class ReportmanagerCommand extends Command {

    public ReportmanagerCommand(String name, String s) {
        super(name, s);
        setPermission("reportsystem.command.reportmanager");
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission(getPermission())) FormWindows.sendReportManager(player);
            else {
                player.sendMessage(Language.getAndReplace("no-permission"));
                ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
            }
        }
        return false;
    }
}
