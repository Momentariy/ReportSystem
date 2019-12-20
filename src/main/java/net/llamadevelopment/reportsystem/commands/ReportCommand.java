//This file was created by Mundschutziii. 
//You can change the code here, but do not sell this file as your own.

package net.llamadevelopment.reportsystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.managers.ReportManager;

public class ReportCommand extends CommandManager {


    private ReportSystem plugin;

    public ReportCommand(ReportSystem plugin) {
        super(plugin, "report", "Report a player.", "/report");
        this.plugin = plugin;
    }

    public boolean execute(final CommandSender sender, String s, String[] args) {
        if (sender instanceof Player) {
            if (args.length >= 2) {
                String player = args[0];
                String reason = "";
                for (int i = 1; i < args.length; ++i) {
                    reason = reason + args[i] + " ";
                }
                if (ReportManager.cooldown.contains(sender)) {
                    sender.sendMessage(plugin.getConfig().getString("Prefix").replace("&", "§") + plugin.getConfig().getString("ReportCooldown").replace("&", "§"));
                    return true;
                }
                ReportManager.createReport(player, sender.getName(), reason, ReportManager.getID(), ReportManager.getDate());
                sender.sendMessage(plugin.getConfig().getString("Prefix").replace("&", "§") + plugin.getConfig().getString("ReportSuccess").replace("&", "§").replace("%target%", player));
                ReportManager.cooldown.add((Player) sender);
                ReportSystem.getInstance().getServer().getScheduler().scheduleDelayedTask(ReportSystem.getInstance(), new Runnable() {
                    public void run() {
                        ReportManager.cooldown.remove(sender);
                    }
                }, plugin.getConfig().getInt("Cooldown") * 20);
            } else {
                sender.sendMessage(plugin.getConfig().getString("Prefix").replace("&", "§") + plugin.getConfig().getString("Usage.ReportCommand").replace("&", "§"));
            }
        }
        return false;
    }
}
