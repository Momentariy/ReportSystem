package net.llamadevelopment.reportsystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.Sound;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.language.Language;

public class ReportCommand extends PluginCommand<ReportSystem> {

    public ReportCommand(ReportSystem owner) {
        super(owner.getConfig().getString("Commands.Report.Name"), owner);
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false),
                new CommandParameter("reason", CommandParamType.TEXT, false)
        });
        this.setDescription(owner.getConfig().getString("Commands.Report.Description"));
        this.setAliases(owner.getConfig().getStringList("Commands.Report.Aliases").toArray(new String[]{}));
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length >= 2) {
                String target = args[0];
                this.getPlugin().provider.hasReported(player.getName(), target, hasReported -> {
                    if (!hasReported) {
                        String reason = "";
                        for (int i = 1; i < args.length; ++i) reason = reason + args[i] + " ";
                        this.getPlugin().provider.createReport(sender.getName(), target, reason);
                        player.sendMessage(Language.getAndReplace("report-success", target, reason));
                        ReportSystemAPI.playSound(player, Sound.RANDOM_LEVELUP);
                        return;
                    }
                    player.sendMessage(Language.getAndReplace("already-reported", target));
                    ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
                });
            } else {
                player.sendMessage(Language.getAndReplace("report-usage", this.getName()));
                ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
            }
        }
        return false;
    }

}
