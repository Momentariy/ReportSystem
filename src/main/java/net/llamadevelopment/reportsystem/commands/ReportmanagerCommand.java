package net.llamadevelopment.reportsystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.level.Sound;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.language.Language;

public class ReportmanagerCommand extends PluginCommand<ReportSystem> {

    public ReportmanagerCommand(ReportSystem owner) {
        super(owner.getConfig().getString("Commands.Reportmanager.Name"), owner);
        this.setDescription(owner.getConfig().getString("Commands.Reportmanager.Description"));
        this.setPermission(owner.getConfig().getString("Commands.Reportmanager.Permission"));
        this.setAliases(owner.getConfig().getStringList("Commands.Reportmanager.Aliases").toArray(new String[]{}));
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission(this.getPermission()))  this.getPlugin().getFormWindows().sendReportManager(player);
            else {
                player.sendMessage(Language.getAndReplace("no-permission"));
                ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
            }
        }
        return false;
    }

}
