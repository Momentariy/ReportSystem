package net.llamadevelopment.reportsystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.messaging.Messages;
import net.llamadevelopment.reportsystem.components.utils.FormUiUtil;

public class ReportmanagerCommand extends CommandManager {

    public ReportmanagerCommand(ReportSystem plugin) {
        super(plugin, plugin.getConfig().getString("Commands.Reportmanager"), "Manage every report in an UI.", "/reportmanager");
    }

    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("reportsystem.command.reportmanager")) {
                if (args.length == 0) {
                    FormUiUtil.sendReportPanel((Player) sender);
                } else {
                    FormUiUtil.sendReportPanel((Player) sender);
                }
            } else {
                sender.sendMessage(Messages.getAndReplace("Messages.NoPermission"));
            }
        }
        return false;
    }
}
