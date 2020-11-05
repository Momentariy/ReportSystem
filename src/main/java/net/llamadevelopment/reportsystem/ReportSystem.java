package net.llamadevelopment.reportsystem;

import cn.nukkit.command.CommandMap;
import cn.nukkit.plugin.PluginBase;
import lombok.Getter;
import net.llamadevelopment.reportsystem.commands.MyreportsCommand;
import net.llamadevelopment.reportsystem.commands.ReportCommand;
import net.llamadevelopment.reportsystem.commands.ReportmanagerCommand;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.forms.FormListener;
import net.llamadevelopment.reportsystem.components.forms.FormWindows;
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

    @Getter
    private FormWindows formWindows;

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
            this.formWindows = new FormWindows(this);
            Language.init();
            this.loadPlugin();
            this.getLogger().info("§aReportSystem successfully started.");
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().error("§4Failed to load ReportSystem.");
        }
    }

    private void loadPlugin() {
        this.getServer().getPluginManager().registerEvents(new FormListener(), this);
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);

        CommandMap map = this.getServer().getCommandMap();
        map.register("reportsystem", new ReportCommand(this));
        map.register("reportsystem", new ReportmanagerCommand(this));
        map.register("reportsystem", new MyreportsCommand(this));
    }

    @Override
    public void onDisable() {
        this.provider.disconnect(this);
    }

}
