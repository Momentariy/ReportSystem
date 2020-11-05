package net.llamadevelopment.reportsystem.components.provider;

import cn.nukkit.Server;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.data.Report;
import net.llamadevelopment.reportsystem.components.event.ReportCloseEvent;
import net.llamadevelopment.reportsystem.components.event.ReportPlayerEvent;
import net.llamadevelopment.reportsystem.components.event.ReportUpdateStaffEvent;
import net.llamadevelopment.reportsystem.components.event.ReportUpdateStatusEvent;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MongodbProvider extends Provider {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> openReports, closedReports;

    @Override
    public void connect(ReportSystem server) {
        CompletableFuture.runAsync(() -> {
            MongoClientURI uri = new MongoClientURI(server.getConfig().getString("MongoDB.Uri"));
            this.mongoClient = new MongoClient(uri);
            this.mongoDatabase = this.mongoClient.getDatabase(server.getConfig().getString("MongoDB.Database"));
            this.openReports = this.mongoDatabase.getCollection("openedreports");
            this.closedReports = this.mongoDatabase.getCollection("closedreports");
            server.getLogger().info("[MongoClient] Connection opened.");
        });
    }

    @Override
    public void disconnect(ReportSystem server) {
        this.mongoClient.close();
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
            this.openReports.insertOne(document);
            Server.getInstance().getPluginManager().callEvent(new ReportPlayerEvent(player, target, reason, "Pending", "Unknown", repID, date));
        });
    }

    @Override
    public void deleteReport(String id) {
        CompletableFuture.runAsync(() -> {
            MongoCollection<Document> collection = this.openReports;
            collection.deleteOne(new Document("id", id));
        });
    }

    @Override
    public void hasReported(String player, String target, Consumer<Boolean> hasReported) {
        CompletableFuture.runAsync(() -> {
            Document document = this.openReports.find(new Document("player", player).append("target", target).append("status", "Pending")).first();
            hasReported.accept(document != null);
        });
    }

    @Override
    public void reportIDExists(String id, Report.ReportStatus status, Consumer<Boolean> exists) {
        CompletableFuture.runAsync(() -> {
            switch (status) {
                case PENDING:
                case PROGRESS: {
                    Document document = this.openReports.find(new Document("id", id)).first();
                    exists.accept(document != null);
                }
                case CLOSED: {
                    Document document = this.closedReports.find(new Document("id", id)).first();
                    exists.accept(document != null);
                }
            }
        });
    }

    @Override
    public void closeReport(String id) {
        CompletableFuture.runAsync(() -> {
            this.getReport(Report.ReportStatus.PROGRESS, id, report -> {
                Document document = new Document("player", report.getPlayer())
                        .append("target", report.getTarget())
                        .append("reason", report.getReason())
                        .append("status", "Closed")
                        .append("member", report.getMember())
                        .append("id", report.getId())
                        .append("date", report.getDate());
                this.closedReports.insertOne(document);
                this.deleteReport(id);
                Server.getInstance().getPluginManager().callEvent(new ReportCloseEvent(report.getPlayer(), report.getTarget(), report.getReason(), "Closed", report.getMember(), report.getId(), report.getDate()));
            });
        });
    }

    @Override
    public void updateStatus(String id, String status) {
        CompletableFuture.runAsync(() -> {
            Document document = new Document("id", id);
            Document found = this.openReports.find(document).first();
            Bson newEntry = new Document("status", status);
            Bson newEntrySet = new Document("$set", newEntry);
            assert found != null;
            this.openReports.updateOne(found, newEntrySet);
            Server.getInstance().getPluginManager().callEvent(new ReportUpdateStatusEvent(id, status));
        });
    }

    @Override
    public void updateStaffmember(String id, String s) {
        CompletableFuture.runAsync(() -> {
            Document document = new Document("id", id);
            Document found = this.openReports.find(document).first();
            Bson newEntry = new Document("member", s);
            Bson newEntrySet = new Document("$set", newEntry);
            assert found != null;
            this.openReports.updateOne(found, newEntrySet);
            Server.getInstance().getPluginManager().callEvent(new ReportUpdateStaffEvent(id, s));
        });
    }

    @Override
    public void getReport(Report.ReportStatus status, String value, Consumer<Report> report) {
        CompletableFuture.runAsync(() -> {
            switch (status) {
                case PENDING:
                case PROGRESS: {
                    Document document = this.openReports.find(new Document("id", value)).first();
                    if (document != null) {
                        String player = document.getString("player");
                        String target = document.getString("target");
                        String reason = document.getString("reason");
                        String statusSet = document.getString("status");
                        String member = document.getString("member");
                        String date = document.getString("date");
                        report.accept(new Report(player, target, reason, statusSet, member, value, date));
                    }
                }
                break;
                case CLOSED: {
                    Document document = this.closedReports.find(new Document("id", value)).first();
                    if (document != null) {
                        String player = document.getString("player");
                        String target = document.getString("target");
                        String reason = document.getString("reason");
                        String statusSet = document.getString("status");
                        String member = document.getString("member");
                        String date = document.getString("date");
                        report.accept(new Report(player, target, reason, statusSet, member, value, date));
                    }
                }
                break;
            }
        });
    }

    @Override
    public void getReports(Report.ReportStatus status, Report.ReportSearch search, String value, Consumer<Set<Report>> reports) {
        CompletableFuture.runAsync(() -> {
            Set<Report> list = new HashSet<>();
            switch (search) {
                case PLAYER: {
                    switch (status) {
                        case PROGRESS: {
                            this.openReports.find(new Document("player", value).append("status", "Progress")).forEach((Block<? super Document>) document -> {
                                Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                                list.add(report);
                            });
                        }
                        break;
                        case PENDING: {
                            this.openReports.find(new Document("player", value).append("status", "Pending")).forEach((Block<? super Document>) document -> {
                                Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                                list.add(report);
                            });
                        }
                        break;
                        case CLOSED: {
                            this.closedReports.find(new Document("player", value).append("status", "Closed")).forEach((Block<? super Document>) document -> {
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
                            this.openReports.find(new Document("target", value).append("status", "Progress")).forEach((Block<? super Document>) document -> {
                                Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                                list.add(report);
                            });
                        }
                        break;
                        case PENDING: {
                            this.openReports.find(new Document("target", value).append("status", "Pending")).forEach((Block<? super Document>) document -> {
                                Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                                list.add(report);
                            });
                        }
                        break;
                        case CLOSED: {
                            this.closedReports.find(new Document("target", value).append("status", "Closed")).forEach((Block<? super Document>) document -> {
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
                            this.openReports.find(new Document("member", value).append("status", "Progress")).forEach((Block<? super Document>) document -> {
                                Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                                list.add(report);
                            });
                        }
                        break;
                        case CLOSED: {
                            this.closedReports.find(new Document("member", value).append("status", "Closed")).forEach((Block<? super Document>) document -> {
                                Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                                list.add(report);
                            });
                        }
                        break;
                    }
                }
                break;
            }
            reports.accept(list);
        });
    }

    @Override
    public void getReports(Report.ReportStatus status, Consumer<Set<Report>> reports) {
        CompletableFuture.runAsync(() -> {
            Set<Report> list = new HashSet<>();
            switch (status) {
                case PROGRESS: {
                    this.openReports.find(new Document("status", "Progress")).forEach((Block<? super Document>) document -> {
                        Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                        list.add(report);
                    });
                }
                break;
                case PENDING: {
                    this.openReports.find(new Document("status", "Pending")).forEach((Block<? super Document>) document -> {
                        Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                        list.add(report);
                    });
                }
                break;
                case CLOSED: {
                    this.closedReports.find(new Document("status", "Closed")).forEach((Block<? super Document>) document -> {
                        Report report = new Report(document.getString("player"), document.getString("target"), document.getString("reason"), document.getString("status"), document.getString("member"), document.getString("id"), document.getString("date"));
                        list.add(report);
                    });
                }
                break;
            }
            reports.accept(list);
        });
    }

    @Override
    public String getProvider() {
        return "MongoDB";
    }

}
