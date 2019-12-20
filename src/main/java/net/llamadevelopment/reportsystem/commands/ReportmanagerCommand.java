//This file was created by Mundschutziii. 
//You can change the code here, but do not sell this file as your own.

package net.llamadevelopment.reportsystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.utils.FormUiUtil;

public class ReportmanagerCommand extends CommandManager {

    private ReportSystem plugin;

    public ReportmanagerCommand(ReportSystem plugin) {
        super(plugin, "reportmanager", "Manage every report in an UI.", "/reportmanager");
        this.plugin = plugin;
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
                sender.sendMessage(plugin.getConfig().getString("Prefix").replace("&", "§") + plugin.getConfig().getString("NoPermission").replace("&", "§"));
            }
        }
        return false;
    }
}
