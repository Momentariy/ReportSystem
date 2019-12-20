//This file was created by Mundschutziii. 
//You can change the code here, but do not sell this file as your own.

package net.llamadevelopment.reportsystem;

import cn.nukkit.command.CommandMap;
import cn.nukkit.plugin.PluginBase;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.llamadevelopment.reportsystem.commands.ReportCommand;
import net.llamadevelopment.reportsystem.commands.ReportmanagerCommand;
import net.llamadevelopment.reportsystem.listeners.EventListener;
import net.llamadevelopment.reportsystem.listeners.FormListener;
import net.llamadevelopment.reportsystem.managers.ReportManager;
import org.bson.Document;

public class ReportSystem extends PluginBase {

    private static ReportSystem instance;
    private boolean error = false;

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> openReportCollection, closeReportCollection;
    public ReportManager reportManager;

    @Override
    public void onEnable() {
        getLogger().info("Starting ReportSystem...");
        instance = this;
        getLogger().info("Loading all components...");
        this.saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new FormListener(), this);
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        registerCommands();
        this.reportManager = new ReportManager();
        if (getConfig().getBoolean("MongoDB")) {
            getLogger().info("Connecting to database...");
            try {
                MongoClientURI uri = new MongoClientURI(getConfig().getString("MongoDBUri"));
                this.mongoClient = new MongoClient(uri);
                this.mongoDatabase = mongoClient.getDatabase(getConfig().getString("Database"));
                this.openReportCollection = mongoDatabase.getCollection(getConfig().getString("OpenReportCollection"));
                this.closeReportCollection = mongoDatabase.getCollection(getConfig().getString("CloseReportCollection"));
                getLogger().info("§aConnected successfully to database!");
            } catch (Exception e) {
                getLogger().error("§4Failed to connect to database.");
                getLogger().error("§4Please check your details in the config.yml or check your mongodb database \"" + getConfig().getString("Database") + "\"");
                error = true;
                onDisable();
            }
        } else {
            getLogger().info("Using config...");
            saveResource("data/openreports.yml");
            saveResource("data/closereports.yml");
            getLogger().info("§aPlugin successfully started.");
        }
    }

    private void registerCommands() {
        CommandMap map = getServer().getCommandMap();
        map.register("report", new ReportCommand(this));
        map.register("reportmanager", new ReportmanagerCommand(this));
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ReportSystem...");
        if (getConfig().getBoolean("MongoDB") && !error) {
            mongoClient.close();
        }
    }

    public static ReportSystem getInstance() {
        return instance;
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public MongoCollection<Document> getCloseReportCollection() {
        return closeReportCollection;
    }

    public MongoCollection<Document> getOpenReportCollection() {
        return openReportCollection;
    }
}
