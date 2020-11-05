package net.llamadevelopment.reportsystem.components.provider;

import cn.nukkit.Server;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.data.Report;
import net.llamadevelopment.reportsystem.components.event.ReportCloseEvent;
import net.llamadevelopment.reportsystem.components.event.ReportPlayerEvent;
import net.llamadevelopment.reportsystem.components.event.ReportUpdateStaffEvent;
import net.llamadevelopment.reportsystem.components.event.ReportUpdateStatusEvent;
import net.llamadevelopment.reportsystem.components.simplesqlclient.MySqlClient;
import net.llamadevelopment.reportsystem.components.simplesqlclient.objects.SqlColumn;
import net.llamadevelopment.reportsystem.components.simplesqlclient.objects.SqlDocument;
import net.llamadevelopment.reportsystem.components.simplesqlclient.objects.SqlDocumentSet;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MySqlProvider extends Provider {

    private MySqlClient client;

    @Override
    public void connect(ReportSystem server) {
        CompletableFuture.runAsync(() -> {
            try {
                this.client = new MySqlClient(
                        server.getConfig().getString("MySql.Host"),
                        server.getConfig().getString("MySql.Port"),
                        server.getConfig().getString("MySql.User"),
                        server.getConfig().getString("MySql.Password"),
                        server.getConfig().getString("MySql.Database")
                );

                this.client.createTable("opened_reports", "id",
                        new SqlColumn("player", SqlColumn.Type.VARCHAR, 30)
                                .append("target", SqlColumn.Type.VARCHAR, 30)
                                .append("reason", SqlColumn.Type.VARCHAR, 100)
                                .append("status", SqlColumn.Type.VARCHAR, 30)
                                .append("member", SqlColumn.Type.VARCHAR, 30)
                                .append("id", SqlColumn.Type.VARCHAR, 6)
                                .append("date", SqlColumn.Type.VARCHAR, 30));

                this.client.createTable("closed_reports", "id",
                        new SqlColumn("player", SqlColumn.Type.VARCHAR, 30)
                                .append("target", SqlColumn.Type.VARCHAR, 30)
                                .append("reason", SqlColumn.Type.VARCHAR, 100)
                                .append("status", SqlColumn.Type.VARCHAR, 30)
                                .append("member", SqlColumn.Type.VARCHAR, 30)
                                .append("id", SqlColumn.Type.VARCHAR, 6)
                                .append("date", SqlColumn.Type.VARCHAR, 30));

                server.getLogger().info("[MySqlClient] Connection opened.");
            } catch (Exception e) {
                e.printStackTrace();
                server.getLogger().info("[MySqlClient] Failed to connect to database.");
            }
        });
    }

    @Override
    public void disconnect(ReportSystem server) {
        server.getLogger().info("[MySqlClient] Connection closed.");
    }

    @Override
    public void createReport(String player, String target, String reason) {
        CompletableFuture.runAsync(() -> {
            String id = ReportSystemAPI.getRandomIDCode();
            String date = ReportSystemAPI.getDate();
            this.client.insert("opened_reports", new SqlDocument("player", player)
                    .append("target", target)
                    .append("reason", reason)
                    .append("status", "Pending")
                    .append("member", "Unknown")
                    .append("id", id)
                    .append("date", date));
            Server.getInstance().getPluginManager().callEvent(new ReportPlayerEvent(player, target, reason, "Pending", "Unknown", id, date));
        });
    }

    @Override
    public void deleteReport(String id) {
        CompletableFuture.runAsync(() -> this.client.delete("opened_reports", new SqlDocument("id", id)));
    }

    @Override
    public void hasReported(String player, String target, Consumer<Boolean> hasReported) {
        CompletableFuture.runAsync(() -> {
            SqlDocument document = this.client.find("opened_reports", new SqlDocument("target", target)).first();
            if (document != null) {
                if (document.getString("target").equals(target) && document.getString("player").equals(player) && document.getString("status").equals("Pending")) hasReported.accept(true);
            }
        });
    }

    @Override
    public void reportIDExists(String id, Report.ReportStatus status, Consumer<Boolean> exists) {
        CompletableFuture.runAsync(() -> {
            switch (status) {
                case PENDING:
                case PROGRESS: {
                    SqlDocument document = this.client.find("opened_reports", new SqlDocument("id", id)).first();
                    if (document != null) {
                        exists.accept(document.getString("id") != null);
                    }
                }
                break;
                case CLOSED: {
                    SqlDocument document = this.client.find("closed_reports", new SqlDocument("id", id)).first();
                    if (document != null) {
                        exists.accept(document.getString("id") != null);
                    }
                }
                break;
            }
        });
    }

    @Override
    public void closeReport(String id) {
        CompletableFuture.runAsync(() -> {
            this.getReport(Report.ReportStatus.PROGRESS, id, report -> {
                this.client.insert("closed_reports", new SqlDocument("player", report.getPlayer())
                        .append("target", report.getTarget())
                        .append("reason", report.getReason())
                        .append("status", "Closed")
                        .append("member", report.getMember())
                        .append("id", report.getId())
                        .append("date", report.getDate()));
                this.deleteReport(id);
                Server.getInstance().getPluginManager().callEvent(new ReportCloseEvent(report.getPlayer(), report.getTarget(), report.getReason(), "Closed", report.getMember(), report.getId(), report.getDate()));
            });
        });
    }

    @Override
    public void updateStatus(String id, String status) {
        CompletableFuture.runAsync(() -> {
            this.client.update("opened_reports", new SqlDocument("id", id), new SqlDocument("status", status));
            Server.getInstance().getPluginManager().callEvent(new ReportUpdateStatusEvent(id, status));
        });
    }

    @Override
    public void updateStaffmember(String id, String s) {
        CompletableFuture.runAsync(() -> {
            this.client.update("opened_reports", new SqlDocument("id", id), new SqlDocument("member", s));
            Server.getInstance().getPluginManager().callEvent(new ReportUpdateStaffEvent(id, s));
        });
    }

    @Override
    public void getReport(Report.ReportStatus status, String value, Consumer<Report> report) {
        CompletableFuture.runAsync(() -> {
            switch (status) {
                case PENDING:
                case PROGRESS: {
                    SqlDocument document = this.client.find("opened_reports", new SqlDocument("id", value)).first();
                    if (document != null) {
                        report.accept(new Report(document.getString("PLAYER"), document.getString("TARGET"), document.getString("REASON"), document.getString("STATUS"), document.getString("MEMBER"), document.getString("ID"), document.getString("DATE")));
                    }
                }
                break;
                case CLOSED: {
                    SqlDocument document = this.client.find("closed_reports", new SqlDocument("id", value)).first();
                    if (document != null) {
                        report.accept(new Report(document.getString("PLAYER"), document.getString("TARGET"), document.getString("REASON"), document.getString("STATUS"), document.getString("MEMBER"), document.getString("ID"), document.getString("DATE")));
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
                            SqlDocumentSet documentSet = this.client.find("opened_reports", new SqlDocument("player", value));
                            documentSet.getAll().forEach(document -> {
                                if (document.getString("status").equals("Progress")) {
                                    Report report = new Report(document.getString("PLAYER"), document.getString("TARGET"), document.getString("REASON"), document.getString("STATUS"), document.getString("MEMBER"), document.getString("ID"), document.getString("DATE"));
                                    list.add(report);
                                }
                            });
                        }
                        break;
                        case PENDING: {
                            SqlDocumentSet documentSet = this.client.find("opened_reports", new SqlDocument("player", value));
                            documentSet.getAll().forEach(document -> {
                                if (document.getString("status").equals("Pending")) {
                                    Report report = new Report(document.getString("PLAYER"), document.getString("TARGET"), document.getString("REASON"), document.getString("STATUS"), document.getString("MEMBER"), document.getString("ID"), document.getString("DATE"));
                                    list.add(report);
                                }
                            });
                        }
                        break;
                        case CLOSED: {
                            SqlDocumentSet documentSet = this.client.find("closed_reports", new SqlDocument("player", value));
                            documentSet.getAll().forEach(document -> {
                                if (document.getString("status").equals("Closed")) {
                                    Report report = new Report(document.getString("PLAYER"), document.getString("TARGET"), document.getString("REASON"), document.getString("STATUS"), document.getString("MEMBER"), document.getString("ID"), document.getString("DATE"));
                                    list.add(report);
                                }
                            });
                        }
                        break;
                    }
                }
                break;
                case TARGET: {
                    switch (status) {
                        case PROGRESS: {
                            SqlDocumentSet documentSet = this.client.find("opened_reports", new SqlDocument("target", value));
                            documentSet.getAll().forEach(document -> {
                                if (document.getString("status").equals("Progress")) {
                                    Report report = new Report(document.getString("PLAYER"), document.getString("TARGET"), document.getString("REASON"), document.getString("STATUS"), document.getString("MEMBER"), document.getString("ID"), document.getString("DATE"));
                                    list.add(report);
                                }
                            });
                        }
                        break;
                        case PENDING: {
                            SqlDocumentSet documentSet = this.client.find("opened_reports", new SqlDocument("target", value));
                            documentSet.getAll().forEach(document -> {
                                if (document.getString("status").equals("Pending")) {
                                    Report report = new Report(document.getString("PLAYER"), document.getString("TARGET"), document.getString("REASON"), document.getString("STATUS"), document.getString("MEMBER"), document.getString("ID"), document.getString("DATE"));
                                    list.add(report);
                                }
                            });
                        }
                        break;
                        case CLOSED: {
                            SqlDocumentSet documentSet = this.client.find("closed_reports", new SqlDocument("target", value));
                            documentSet.getAll().forEach(document -> {
                                if (document.getString("status").equals("Closed")) {
                                    Report report = new Report(document.getString("PLAYER"), document.getString("TARGET"), document.getString("REASON"), document.getString("STATUS"), document.getString("MEMBER"), document.getString("ID"), document.getString("DATE"));
                                    list.add(report);
                                }
                            });
                        }
                        break;
                    }
                }
                break;
                case MEMBER: {
                    switch (status) {
                        case PENDING:
                        case PROGRESS: {
                            SqlDocumentSet documentSet = this.client.find("opened_reports", new SqlDocument("member", value));
                            documentSet.getAll().forEach(document -> {
                                if (document.getString("status").equals("Progress")) {
                                    Report report = new Report(document.getString("PLAYER"), document.getString("TARGET"), document.getString("REASON"), document.getString("STATUS"), document.getString("MEMBER"), document.getString("ID"), document.getString("DATE"));
                                    list.add(report);
                                }
                            });
                        }
                        break;
                        case CLOSED: {
                            SqlDocumentSet documentSet = this.client.find("closed_reports", new SqlDocument("member", value));
                            documentSet.getAll().forEach(document -> {
                                if (document.getString("status").equals("Closed")) {
                                    Report report = new Report(document.getString("PLAYER"), document.getString("TARGET"), document.getString("REASON"), document.getString("STATUS"), document.getString("MEMBER"), document.getString("ID"), document.getString("DATE"));
                                    list.add(report);
                                }
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
                    SqlDocumentSet documentSet = this.client.find("opened_reports", new SqlDocument("status", "Progress"));
                    documentSet.getAll().forEach(document -> {
                        Report report = new Report(document.getString("PLAYER"), document.getString("TARGET"), document.getString("REASON"), document.getString("STATUS"), document.getString("MEMBER"), document.getString("ID"), document.getString("DATE"));
                        list.add(report);
                    });
                }
                break;
                case PENDING: {
                    SqlDocumentSet documentSet = this.client.find("opened_reports", new SqlDocument("status", "Pending"));
                    documentSet.getAll().forEach(document -> {
                        Report report = new Report(document.getString("PLAYER"), document.getString("TARGET"), document.getString("REASON"), document.getString("STATUS"), document.getString("MEMBER"), document.getString("ID"), document.getString("DATE"));
                        list.add(report);
                    });
                }
                break;
                case CLOSED: {
                    SqlDocumentSet documentSet = this.client.find("closed_reports", new SqlDocument("status", "Closed"));
                    documentSet.getAll().forEach(document -> {
                        Report report = new Report(document.getString("PLAYER"), document.getString("TARGET"), document.getString("REASON"), document.getString("STATUS"), document.getString("MEMBER"), document.getString("ID"), document.getString("DATE"));
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
        return "MySql";
    }

}
