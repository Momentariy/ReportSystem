package net.llamadevelopment.reportsystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.level.Sound;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.data.Report;
import net.llamadevelopment.reportsystem.components.language.Language;

public class MyreportsCommand extends PluginCommand<ReportSystem> {

    public MyreportsCommand(ReportSystem owner) {
        super(owner.getConfig().getString("Commands.Myreports.Name"), owner);
        this.setDescription(owner.getConfig().getString("Commands.Myreports.Description"));
        this.setAliases(owner.getConfig().getStringList("Commands.Myreports.Aliases").toArray(new String[]{}));
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            this.getPlugin().provider.getReports(Report.ReportStatus.PENDING, Report.ReportSearch.PLAYER, player.getName(), reports1 -> {
                this.getPlugin().provider.getReports(Report.ReportStatus.PROGRESS, Report.ReportSearch.PLAYER, player.getName(), reports2 -> {
                    this.getPlugin().provider.getReports(Report.ReportStatus.CLOSED, Report.ReportSearch.PLAYER, player.getName(), reports3 -> {
                        int amount = reports1.size() + reports2.size() + reports3.size();
                        if (amount == 0) {
                            player.sendMessage(Language.getAndReplace("no-myreports"));
                            ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
                            return;
                        }
                        this.getPlugin().getFormWindows().sendMyreports(player, reports1, reports2, reports3);
                    });
                });
            });
        }
        return false;
    }

}
