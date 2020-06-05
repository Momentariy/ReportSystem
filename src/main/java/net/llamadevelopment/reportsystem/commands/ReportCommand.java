package net.llamadevelopment.reportsystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.Sound;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.managers.database.Provider;
import net.llamadevelopment.reportsystem.components.tools.Language;

public class ReportCommand extends Command {

    public ReportCommand(String name, String s) {
        super(name, s);
        commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false),
                new CommandParameter("reason", CommandParamType.TEXT, false)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Provider api = ReportSystemAPI.getProvider();
            if (args.length >= 2) {
                String target = args[0];
                if (!api.hasReported(player.getName(), target)) {
                    String reason = "";
                    for (int i = 1; i < args.length; ++i) reason = reason + args[i] + " ";
                    api.createReport(sender.getName(), target, reason);
                    player.sendMessage(Language.getAndReplace("report-success", target, reason));
                    ReportSystemAPI.playSound(player, Sound.RANDOM_LEVELUP);
                } else {
                    player.sendMessage(Language.getAndReplace("already-reported", target));
                    ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
                }
            } else {
                player.sendMessage(Language.getAndReplace("report-usage", getName()));
                ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
            }
        }
        return false;
    }
}
