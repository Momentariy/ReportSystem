package net.llamadevelopment.reportsystem.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandMap;
import cn.nukkit.command.PluginIdentifiableCommand;
import net.llamadevelopment.reportsystem.ReportSystem;

public abstract class CommandManager extends Command implements PluginIdentifiableCommand {
    private ReportSystem plugin;

    public CommandManager(ReportSystem plugin, String name, String desc, String usage) {
        super(name, desc, usage);

        this.plugin = plugin;
    }

    public ReportSystem getPlugin() {
        return plugin;
    }
}