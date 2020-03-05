package net.llamadevelopment.reportsystem;

import cn.nukkit.command.CommandMap;
import cn.nukkit.plugin.PluginBase;
import net.llamadevelopment.reportsystem.commands.ReportCommand;
import net.llamadevelopment.reportsystem.commands.ReportmanagerCommand;
import net.llamadevelopment.reportsystem.components.managers.database.MongoDBProvider;
import net.llamadevelopment.reportsystem.components.managers.database.MySqlProvider;
import net.llamadevelopment.reportsystem.listener.EventListener;
import net.llamadevelopment.reportsystem.listener.FormListener;

public class ReportSystem extends PluginBase {

    private static ReportSystem instance;
    private boolean mysql, mongodb, yaml = false;
    private MySqlProvider mySql;

    @Override
    public void onEnable() {
        instance = this;
        System.out.println("");
        System.out.println("  _____                       _    _____           _                 ");
        System.out.println(" |  __ \\                     | |  / ____|         | |                ");
        System.out.println(" | |__) |___ _ __   ___  _ __| |_| (___  _   _ ___| |_ ___ _ __ ___  ");
        System.out.println(" |  _  // _ \\ '_ \\ / _ \\| '__| __|\\___ \\| | | / __| __/ _ \\ '_ ` _ \\ ");
        System.out.println(" | | \\ \\  __/ |_) | (_) | |  | |_ ____) | |_| \\__ \\ ||  __/ | | | | |");
        System.out.println(" |_|  \\_\\___| .__/ \\___/|_|   \\__|_____/ \\__, |___/\\__\\___|_| |_| |_|");
        System.out.println("            | |                           __/ |                      ");
        System.out.println("            |_|                          |___/                       ");
        System.out.println("");
        getLogger().info("§aStarting and loading all components...");
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new FormListener(), this);
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getLogger().info("Components successfully loaded!");
        if (getConfig().getString("Provider").equalsIgnoreCase("MongoDB")) {
            mongodb = true;
            getLogger().info("Connecting to database...");
            MongoDBProvider.connect(this);
        } else if (getConfig().getString("Provider").equalsIgnoreCase("MySql")) {
            mysql = true;
            getLogger().info("Connecting to database...");
            this.mySql = new MySqlProvider();
            this.mySql.createTables();
        } else if (getConfig().getString("Provider").equalsIgnoreCase("Yaml")) {
            yaml = true;
            getLogger().info("Using YAML as provider...");
            saveResource("data/openreports.yml");
            saveResource("data/closereports.yml");
            getLogger().info("§aPlugin successfully started.");
        } else {
            getLogger().warning("§4§lFailed to load! Please specify a valid provider: MySql, MongoDB, Yaml");
        }
        registerCommands();
    }

    private void registerCommands() {
        CommandMap map = getServer().getCommandMap();
        map.register(getConfig().getString("Commands.Report"), new ReportCommand(this));
        map.register(getConfig().getString("Commands.Reportmanager"), new ReportmanagerCommand(this));
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ReportSystem...");
    }

    public static ReportSystem getInstance() {
        return instance;
    }

    public boolean isYaml() {
        return yaml;
    }

    public boolean isMysql() {
        return mysql;
    }

    public boolean isMongodb() {
        return mongodb;
    }
}
