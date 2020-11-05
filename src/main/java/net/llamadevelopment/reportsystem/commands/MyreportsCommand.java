package net.llamadevelopment.reportsystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Sound;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.data.Report;
import net.llamadevelopment.reportsystem.components.provider.Provider;
import net.llamadevelopment.reportsystem.components.forms.FormWindows;
import net.llamadevelopment.reportsystem.components.language.Language;

public class MyreportsCommand extends Command {

    public MyreportsCommand(String name, String s) {
        super(name, s);
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Provider api = ReportSystemAPI.getProvider();
            int amount = api.getReports(Report.ReportStatus.PENDING, Report.ReportSearch.PLAYER, player.getName()).size() + api.getReports(Report.ReportStatus.PROGRESS, Report.ReportSearch.PLAYER, player.getName()).size() + api.getReports(Report.ReportStatus.CLOSED, Report.ReportSearch.PLAYER, player.getName()).size();
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
