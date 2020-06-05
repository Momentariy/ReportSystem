package net.llamadevelopment.reportsystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Sound;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.data.ReportSearch;
import net.llamadevelopment.reportsystem.components.data.ReportStatus;
import net.llamadevelopment.reportsystem.components.managers.database.Provider;
import net.llamadevelopment.reportsystem.components.tools.FormWindows;
import net.llamadevelopment.reportsystem.components.tools.Language;

public class MyreportsCommand extends Command {

    public MyreportsCommand(String name, String s) {
        super(name, s);
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Provider api = ReportSystemAPI.getProvider();
            int amount = api.getReports(ReportStatus.PENDING, ReportSearch.PLAYER, player.getName()).size() + api.getReports(ReportStatus.PROGRESS, ReportSearch.PLAYER, player.getName()).size() + api.getReports(ReportStatus.CLOSED, ReportSearch.PLAYER, player.getName()).size();
            if (amount == 0) {
                player.sendMessage(Language.getAndReplace("no-myreports"));
                ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
                return true;
            }
            FormWindows.sendMyreports(player);
        }
        return false;
    }
}
