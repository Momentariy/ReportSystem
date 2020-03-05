package net.llamadevelopment.reportsystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.managers.ReportManager;
import net.llamadevelopment.reportsystem.components.messaging.Messages;

public class ReportCommand extends CommandManager {


    private ReportSystem plugin;

    public ReportCommand(ReportSystem plugin) {
        super(plugin, plugin.getConfig().getString("Commands.Report"), "Report a player.", "/report");
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
                    sender.sendMessage(Messages.getAndReplace("Messages.ReportCooldown"));
                    return true;
                }
                ReportManager.createReport(player, sender.getName(), reason, ReportManager.getID(), ReportManager.getDate());
                sender.sendMessage(Messages.getAndReplace("Messages.ReportSuccess", player));
                ReportManager.cooldown.add((Player) sender);
                ReportSystem.getInstance().getServer().getScheduler().scheduleDelayedTask(ReportSystem.getInstance(), new Runnable() {
                    public void run() {
                        ReportManager.cooldown.remove(sender);
                    }
                }, plugin.getConfig().getInt("Settings.ReportCooldown") * 20);
            } else {
                sender.sendMessage(Messages.getAndReplace("Usage.ReportCommand", plugin.getConfig().getString("Command.Report")));
            }
        }
        return false;
    }
}
