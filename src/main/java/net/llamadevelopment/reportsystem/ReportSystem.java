package net.llamadevelopment.reportsystem;

import cn.nukkit.command.CommandMap;
import cn.nukkit.plugin.PluginBase;
import net.llamadevelopment.reportsystem.commands.MyreportsCommand;
import net.llamadevelopment.reportsystem.commands.ReportCommand;
import net.llamadevelopment.reportsystem.commands.ReportmanagerCommand;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.forms.FormListener;
import net.llamadevelopment.reportsystem.components.managers.MongoDBProvider;
import net.llamadevelopment.reportsystem.components.managers.MySqlProvider;
import net.llamadevelopment.reportsystem.components.managers.YamlProvider;
import net.llamadevelopment.reportsystem.components.managers.database.Provider;
import net.llamadevelopment.reportsystem.components.tools.Language;
import net.llamadevelopment.reportsystem.listeners.EventListener;

import java.util.HashMap;
import java.util.Map;

public class ReportSystem extends PluginBase {

    private static ReportSystem instance;
    public static Provider provider;
    private static final Map<String, Provider> providers = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        registerProvider(new MongoDBProvider());
        registerProvider(new MySqlProvider());
        registerProvider(new YamlProvider());
        if (!providers.containsKey(getConfig().getString("Provider"))) {
            getLogger().error("§4Please specify a valid provider: Yaml, MySql, MongoDB");
            return;
        }
        provider = providers.get(getConfig().getString("Provider"));
        provider.connect(this);
        getLogger().info("§aSuccessfully loaded " + provider.getProvider() + " provider.");
        ReportSystemAPI.setProvider(provider);
        Language.initConfiguration();
        loadPlugin();
        getLogger().info("§aPlugin successfully started.");
    }

    private void loadPlugin() {
        getServer().getPluginManager().registerEvents(new FormListener(), this);
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        CommandMap map = getServer().getCommandMap();
        map.register(getConfig().getString("Commands.Report"), new ReportCommand(getConfig().getString("Commands.Report"), getConfig().getString("Commands.ReportDescription")));
        map.register(getConfig().getString("Commands.Reportmanager"), new ReportmanagerCommand(getConfig().getString("Commands.Reportmanager"), getConfig().getString("Commands.ReportmanagerDescription")));
        map.register(getConfig().getString("Commands.Myreports"), new MyreportsCommand(getConfig().getString("Commands.Myreports"), getConfig().getString("Commands.MyreportsDescription")));
    }

    @Override
    public void onDisable() {
        provider.disconnect(this);
    }

    private void registerProvider(Provider provider) {
        providers.put(provider.getProvider(), provider);
    }

    public static ReportSystem getInstance() {
        return instance;
    }

}
