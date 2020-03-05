package net.llamadevelopment.reportsystem.components.managers.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.llamadevelopment.reportsystem.ReportSystem;
import org.bson.Document;

public class MongoDBProvider {

    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;
    private static MongoCollection<Document> openReportCollection, closeReportCollection;

    public static void connect(ReportSystem instance) {
        try {
            MongoClientURI uri = new MongoClientURI(instance.getConfig().getString("MongoDB.Uri"));
            mongoClient = new MongoClient(uri);
            mongoDatabase = mongoClient.getDatabase(instance.getConfig().getString("MongoDB.Database"));
            openReportCollection = mongoDatabase.getCollection("openedreports");
            closeReportCollection = mongoDatabase.getCollection("closedreports");
            instance.getLogger().info("§aConnected successfully to database!");
        } catch (Exception e) {
            instance.getLogger().error("§4Failed to connect to database.");
            instance.getLogger().error("§4Please check your details in the config.yml.");
            e.printStackTrace();
        }
    }

    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    public static MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public static MongoCollection<Document> getOpenReportCollection() {
        return openReportCollection;
    }

    public static MongoCollection<Document> getCloseReportCollection() {
        return closeReportCollection;
    }
}
