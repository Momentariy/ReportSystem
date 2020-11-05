package net.llamadevelopment.reportsystem.components.provider;

import cn.nukkit.utils.Config;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.data.Report;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MongodbProvider extends Provider {

    Config config = ReportSystem.getInstance().getConfig();

    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> openReports, closedReports;

    @Override
    public void connect(ReportSystem server) {
        CompletableFuture.runAsync(() -> {
            MongoClientURI uri = new MongoClientURI(config.getString("MongoDB.Uri"));
            mongoClient = new MongoClient(uri);
            mongoDatabase = mongoClient.getDatabase(config.getString("MongoDB.Database"));
            openReports = mongoDatabase.getCollection("openedreports");
            closedReports = mongoDatabase.getCollection("closedreports");
            server.getLogger().info("[MongoClient] Connection opened.");
        });
    }

    @Override
    public void disconnect(ReportSystem server) {
        mongoClient.close();
        server.getLogger().info("[MongoClient] Connection closed.");
    }

    @Override
    public void createReport(String player, String target, String reason) {
        CompletableFuture.runAsync(() -> {
            String repID = ReportSystemAPI.getRandomIDCode();
            String date = ReportSystemAPI.getDate();
            Document document = new Document("player", player)
                    .append("target", target)
                    .append("reason", reason)
                    .append("status", "Pending")
                    .append("member", "Unknown")
                    .append("id", repID)
                    .append("date", date);
            openReports.insertOne(document);
        });
    }

    @Override
    public void deleteReport(String id) {
        CompletableFuture.runAsync(() -> {
            MongoCollection<Document> collection = openReports;
            collection.deleteOne(new Document("id", id));
        });
    }

    @Override
    public void hasReported(String player, System target, Consumer<Boolean> hasReported) {
        CompletableFuture.runAsync(() -> {
            Document document = openReports.find(new Document("player", player).append("target", target).append("status", "Pending")).first();
            hasReported.accept(document != null);
        });
    }

    @Override
    public boolean hasReported(String player, String target) {
        Document document = openReports.find(new Document("player", player).append("target", target).append("status", "Pending")).first();
        return document != null;
    }

    @Override
    public boolean reportIDExists(String id, Report.ReportStatus status) {
        switch (status) {
            case PENDING:
            case PROGRESS: {
                Document document = openReports.find(new Document("id", id)).first();
                return document != null;
            }
            case CLOSED: {
                Document document = closedReports.find(new Document("id", id)).first();
                return document != null;
            }
        }
        return false;
    }

    @Override
    public void closeReport(String id) {
        CompletableFuture.runAsync(() -> {
            Report report = getReport(Report.ReportStatus.PROGRESS, id);
            Document document = new Document("player", report.getPlayer())
                    .append("target", report.getTarget())
                    .append("reason", report.getReason())
                    .append("status", "Closed")
                    .append("member", report.getMember())
                    .append("id", report.getId())
                    .append("date", report.getDate());
            closedReports.insertOne(document);
            deleteReport(id);
        });
    }

    @Override
    public void updateStatus(String id, String status) {
        CompletableFuture.runAsync(() -> {
            Document document = new Document("id", id);
            Document found = openReports.find(document).first();
            Bson newEntry = new Document("status", status);
            Bson newEntrySet = new Document("$set", newEntry);
            assert found != null;
            openReports.updateOne(found, newEntrySet);
        });
    }

    @Override
    public void updateStaffmember(String id, String s) {
        CompletableFuture.runAsync(() -> {
            Document document = new Document("id", id);
            Document found = openReports.find(document).first();
            Bson newEntry = new Document("member", s);
            Bson newEntrySet = new Document("$set", newEntry);
            assert found != null;
            openReports.updateOne(found, newEntrySet);
        });
    }

    @Override
    public Report getReport(Report.ReportStatus status, String id) {
        switch (status) {
            case PENDING:
            case PROGRESS: {
                Document document = openReports.find(new Document("id", id)).first();
                if (document != null) {
                    String player = document.getString("player");
                    String target = document.getString("target");
                    String reason = document.getString("reason");
                    String statusSet = document.getString("status");
                    String member = document.getString("member");
                    String date = document.getString("date");
                    return new Report(player, target, reason, statusSet, member, id, date);
                }
            }
            break;
            case CLOSED: {
                Document document = closedReports.find(new Document("id", id)).first();
                if (document != null) {
                    String player = document.getString("player");
                    String target = document.getString("target");
                    String reason = document.getString("reason");
                    String statusSet = document.getString("status");
                    String member = document.getString("member");
                    String date = document.getString("date");
                    return new Report(player, target, reason, statusSet, member, id, date);
                }
            }
            break;
        }
        return null;
    }

    @Override
    public List<Report> getReports(Report.ReportStatus status, Report.ReportSearch search, String value) {
        List<Report> list = new ArrayList<>();
        switch (search) {
            case PLAYER: {
                switch (status) {
                    case PROGRESS: {
                        openReports.find(new Document("player", value).append("status", "Progress")).forEach((Block<? super Document>) document -> {
                            Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                            list.add(report);
                        });
                    }
                    break;
                    case PENDING: {
                        openReports.find(new Document("player", value).append("status", "Pending")).forEach((Block<? super Document>) document -> {
                            Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                            list.add(report);
                        });
                    }
                    break;
                    case CLOSED: {
                        closedReports.find(new Document("player", value).append("status", "Closed")).forEach((Block<? super Document>) document -> {
                            Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                            list.add(report);
                        });
                    }
                    break;
                }
            }
            break;
            case TARGET: {
                switch (status) {
                    case PROGRESS: {
                        openReports.find(new Document("target", value).append("status", "Progress")).forEach((Block<? super Document>) document -> {
                            Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                            list.add(report);
                        });
                    }
                    break;
                    case PENDING: {
                        openReports.find(new Document("target", value).append("status", "Pending")).forEach((Block<? super Document>) document -> {
                            Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                            list.add(report);
                        });
                    }
                    break;
                    case CLOSED: {
                        closedReports.find(new Document("target", value).append("status", "Closed")).forEach((Block<? super Document>) document -> {
                            Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                            list.add(report);
                        });
                    }
                    break;
                }
            }
            break;
            case MEMBER: {
                switch (status) {
                    case PROGRESS:
                    case PENDING: {
                        openReports.find(new Document("member", value).append("status", "Progress")).forEach((Block<? super Document>) document -> {
                            Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                            list.add(report);
                        });
                    }
                    break;
                    case CLOSED: {
                        closedReports.find(new Document("member", value).append("status", "Closed")).forEach((Block<? super Document>) document -> {
                            Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                            list.add(report);
                        });
                    }
                    break;
                }
            }
            break;
        }
        return list;
    }

    @Override
    public List<Report> getReports(Report.ReportStatus status) {
        List<Report> list = new ArrayList<>();
        switch (status) {
            case PROGRESS: {
                openReports.find(new Document("status", "Progress")).forEach((Block<? super Document>) document -> {
                    Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                    list.add(report);
                });
            }
            break;
            case PENDING: {
                openReports.find(new Document("status", "Pending")).forEach((Block<? super Document>) document -> {
                    Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                    list.add(report);
                });
            }
            break;
            case CLOSED: {
                closedReports.find(new Document("status", "Closed")).forEach((Block<? super Document>) document -> {
                    Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                    list.add(report);
                });
            }
            break;
        }
        return list;
    }

    @Override
    public String getProvider() {
        return "MongoDB";
    }
}
