package net.llamadevelopment.reportsystem;

import cn.nukkit.command.CommandMap;
import cn.nukkit.plugin.PluginBase;
import lombok.Getter;
import net.llamadevelopment.reportsystem.commands.MyreportsCommand;
import net.llamadevelopment.reportsystem.commands.ReportCommand;
import net.llamadevelopment.reportsystem.commands.ReportmanagerCommand;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.forms.FormListener;
import net.llamadevelopment.reportsystem.components.provider.MongodbProvider;
import net.llamadevelopment.reportsystem.components.provider.MySqlProvider;
import net.llamadevelopment.reportsystem.components.provider.YamlProvider;
import net.llamadevelopment.reportsystem.components.provider.Provider;
import net.llamadevelopment.reportsystem.components.language.Language;
import net.llamadevelopment.reportsystem.listeners.EventListener;

import java.util.HashMap;
import java.util.Map;

public class ReportSystem extends PluginBase {

    private final Map<String, Provider> providers = new HashMap<>();
    public Provider provider;

    @Getter
    private static ReportSystem instance;

    @Override
    public void onEnable() {
        instance = this;
        try {
            this.saveDefaultConfig();
            this.providers.put("MongoDB", new MongodbProvider());
            this.providers.put("MySql", new MySqlProvider());
            this.providers.put("Yaml", new YamlProvider());
            if (!this.providers.containsKey(this.getConfig().getString("Provider"))) {
                this.getLogger().error("§4Please specify a valid provider: Yaml, MySql, MongoDB");
                return;
            }
            this.provider = this.providers.get(this.getConfig().getString("Provider"));
            this.provider.connect(this);
            this.getLogger().info("§aSuccessfully loaded " + this.provider.getProvider() + " provider.");
            ReportSystemAPI.setProvider(this.provider);
            Language.init();
            this.loadPlugin();
            this.getLogger().info("§aReportSystem successfully started.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPlugin() {
        this.getServer().getPluginManager().registerEvents(new FormListener(), this);
        this.getServer().getPluginManager().registerEvents(new EventListener(), this);

        CommandMap map = this.getServer().getCommandMap();
        map.register(getConfig().getString("Commands.Report"), new ReportCommand(getConfig().getString("Commands.Report"), getConfig().getString("Commands.ReportDescription")));
        map.register(getConfig().getString("Commands.Reportmanager"), new ReportmanagerCommand(getConfig().getString("Commands.Reportmanager"), getConfig().getString("Commands.ReportmanagerDescription")));
        map.register(getConfig().getString("Commands.Myreports"), new MyreportsCommand(getConfig().getString("Commands.Myreports"), getConfig().getString("Commands.MyreportsDescription")));
    }

    @Override
    public void onDisable() {
        this.provider.disconnect(this);
    }

}
