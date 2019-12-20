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

    public CommandManager(ReportSystem plugin, String name, String desc, String usage, String[] aliases) {
        super(name, desc, usage, aliases);

        this.plugin = plugin;
    }

    public CommandManager(ReportSystem plugin, Boolean override, String name, String desc, String usage, String[] aliases) {
        super(name, desc, usage, aliases);

        this.plugin = plugin;

        CommandMap map = plugin.getServer().getCommandMap();
        Command command = map.getCommand(name);
        command.setLabel(name + "_disabled");
        command.unregister(map);
    }

    public ReportSystem getPlugin() {
        return plugin;
    }
}