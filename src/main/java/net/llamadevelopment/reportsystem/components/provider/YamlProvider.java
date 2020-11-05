package net.llamadevelopment.reportsystem.components.provider;

import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.data.Report;
import net.llamadevelopment.reportsystem.components.event.ReportCloseEvent;
import net.llamadevelopment.reportsystem.components.event.ReportPlayerEvent;
import net.llamadevelopment.reportsystem.components.event.ReportUpdateStaffEvent;
import net.llamadevelopment.reportsystem.components.event.ReportUpdateStatusEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class YamlProvider extends Provider {

    private Config openedReports, closedReports;

    @Override
    public void connect(ReportSystem server) {
        CompletableFuture.runAsync(() -> {
            server.saveResource("/data/openedreports.yml");
            server.saveResource("/data/closedreports.yml");
            this.openedReports = new Config(server.getDataFolder() + "/data/openedreports.yml", Config.YAML);
            this.closedReports = new Config(server.getDataFolder() + "/data/closedreports.yml", Config.YAML);
            server.getLogger().info("[Configuration] Ready.");
        });
    }

    @Override
    public void createReport(String player, String target, String reason) {
        String id = ReportSystemAPI.getRandomIDCode();
        String date = ReportSystemAPI.getDate();
        this.openedReports.set("Reports." + id + ".Player", player);
        this.openedReports.set("Reports." + id + ".Target", target);
        this.openedReports.set("Reports." + id + ".Reason", reason);
        this.openedReports.set("Reports." + id + ".Status", "Pending");
        this.openedReports.set("Reports." + id + ".Member", "Unknown");
        this.openedReports.set("Reports." + id + ".Date", date);
        this.openedReports.save();
        this.openedReports.reload();
        Server.getInstance().getPluginManager().callEvent(new ReportPlayerEvent(player, target, reason, "Pending", "Unknown", id, date));
    }

    @Override
    public void deleteReport(String id) {
        Map<String, Object> map = this.openedReports.getSection("Reports").getAllMap();
        map.remove(id);
        this.openedReports.set("Reports", map);
        this.openedReports.save();
        this.openedReports.reload();
    }

    @Override
    public void hasReported(String player, String target, Consumer<Boolean> hasReported) {
        for (String id : this.openedReports.getSection("Reports").getKeys(false)) {
            if (this.openedReports.getString("Reports." + id + ".Target").equals(target) && this.openedReports.getString("Reports." + id + ".Player").equals(player) && this.openedReports.getString("Reports." + id + ".Status").equals("Pending")) hasReported.accept(true);
        }
    }

    @Override
    public void reportIDExists(String id, Report.ReportStatus status, Consumer<Boolean> exists) {
        switch (status) {
            case PENDING:
            case PROGRESS: {
                exists.accept(this.openedReports.exists("Reports." + id));
            }
            case CLOSED: {
                exists.accept(this.closedReports.exists("Reports." + id));
            }
        }
    }

    @Override
    public void closeReport(String id) {
        this.getReport(Report.ReportStatus.PROGRESS, id, report -> {
            this.closedReports.set("Reports." + id + ".Player", report.getPlayer());
            this.closedReports.set("Reports." + id + ".Target", report.getTarget());
            this.closedReports.set("Reports." + id + ".Reason", report.getReason());
            this.closedReports.set("Reports." + id + ".Status", "Closed");
            this.closedReports.set("Reports." + id + ".Member", report.getMember());
            this.closedReports.set("Reports." + id + ".Date", report.getDate());
            this.closedReports.save();
            this.closedReports.reload();
            this.deleteReport(id);
            Server.getInstance().getPluginManager().callEvent(new ReportCloseEvent(report.getPlayer(), report.getTarget(), report.getReason(), "Closed", report.getMember(), report.getId(), report.getDate()));
        });
    }

    @Override
    public void updateStatus(String id, String status) {
        this.openedReports.set("Reports." + id + ".Status", status);
        this.openedReports.save();
        this.openedReports.reload();
        Server.getInstance().getPluginManager().callEvent(new ReportUpdateStatusEvent(id, status));
    }

    @Override
    public void updateStaffmember(String id, String s) {
        this.openedReports.set("Reports." + id + ".Member", s);
        this.openedReports.save();
        this.openedReports.reload();
        Server.getInstance().getPluginManager().callEvent(new ReportUpdateStaffEvent(id, s));
    }

    @Override
    public void getReport(Report.ReportStatus status, String value, Consumer<Report> report) {
        switch (status) {
            case PENDING:
            case PROGRESS: {
                String player = this.openedReports.getString("Reports." + value + ".Player");
                String target = this.openedReports.getString("Reports." + value + ".Target");
                String reason = this.openedReports.getString("Reports." + value + ".Reason");
                String statusSet = this.openedReports.getString("Reports." + value + ".Status");
                String member = this.openedReports.getString("Reports." + value + ".Member");
                String date = this.openedReports.getString("Reports." + value + ".Date");
                report.accept(new Report(player, target, reason, statusSet, member, value, date));
            }
            case CLOSED: {
                String player = this.closedReports.getString("Reports." + value + ".Player");
                String target = this.closedReports.getString("Reports." + value + ".Target");
                String reason = this.closedReports.getString("Reports." + value + ".Reason");
                String statusSet = this.closedReports.getString("Reports." + value + ".Status");
                String member = this.closedReports.getString("Reports." + value + ".Member");
                String date = this.closedReports.getString("Reports." + value + ".Date");
                report.accept(new Report(player, target, reason, statusSet, member, value, date));
            }
        }
    }

    @Override
    public void getReports(Report.ReportStatus status, Report.ReportSearch search, String value, Consumer<Set<Report>> reports) {
        Set<Report> list = new HashSet<>();
        switch (search) {
            case PLAYER: {
                switch (status) {
                    case PROGRESS: {
                        for (String s : this.openedReports.getSection("Reports").getAll().getKeys(false)) {
                            String playerSet = this.openedReports.getString("Reports." + s + ".Player");
                            String statusSet = this.openedReports.getString("Reports." + s + ".Status");
                            if (playerSet.equals(value) && statusSet.equals("Progress")) {
                                String target = this.openedReports.getString("Reports." + s + ".Target");
                                String reason = this.openedReports.getString("Reports." + s + ".Reason");
                                String member = this.openedReports.getString("Reports." + s + ".Member");
                                String date = this.openedReports.getString("Reports." + s + ".Date");
                                Report report = new Report(playerSet, target, reason, statusSet, member, s, date);
                                list.add(report);
                            }
                        }
                    }
                    break;
                    case PENDING: {
                        for (String s : this.openedReports.getSection("Reports").getAll().getKeys(false)) {
                            String playerSet = this.openedReports.getString("Reports." + s + ".Player");
                            String statusSet = this.openedReports.getString("Reports." + s + ".Status");
                            if (playerSet.equals(value) && statusSet.equals("Pending")) {
                                String target = this.openedReports.getString("Reports." + s + ".Target");
                                String reason = this.openedReports.getString("Reports." + s + ".Reason");
                                String member = this.openedReports.getString("Reports." + s + ".Member");
                                String date = this.openedReports.getString("Reports." + s + ".Date");
                                Report report = new Report(playerSet, target, reason, statusSet, member, s, date);
                                list.add(report);
                            }
                        }
                    }
                    break;
                    case CLOSED: {
                        for (String s : this.closedReports.getSection("Reports").getAll().getKeys(false)) {
                            String playerSet = this.closedReports.getString("Reports." + s + ".Player");
                            String statusSet = this.closedReports.getString("Reports." + s + ".Status");
                            if (playerSet.equals(value) && statusSet.equals("Closed")) {
                                String target = this.closedReports.getString("Reports." + s + ".Target");
                                String reason = this.closedReports.getString("Reports." + s + ".Reason");
                                String member = this.closedReports.getString("Reports." + s + ".Member");
                                String date = this.closedReports.getString("Reports." + s + ".Date");
                                Report report = new Report(playerSet, target, reason, statusSet, member, s, date);
                                list.add(report);
                            }
                        }
                    }
                    break;
                }
            }
            break;
            case TARGET: {
                switch (status) {
                    case PROGRESS: {
                        for (String s : this.openedReports.getSection("Reports").getAll().getKeys(false)) {
                            String targetSet = this.openedReports.getString("Reports." + s + ".Target");
                            String statusSet = this.openedReports.getString("Reports." + s + ".Status");
                            if (targetSet.equals(value) && statusSet.equals("Progress")) {
                                String player = this.openedReports.getString("Reports." + s + ".Player");
                                String reason = this.openedReports.getString("Reports." + s + ".Reason");
                                String member = this.openedReports.getString("Reports." + s + ".Member");
                                String date = this.openedReports.getString("Reports." + s + ".Date");
                                Report report = new Report(player, targetSet, reason, statusSet, member, s, date);
                                list.add(report);
                            }
                        }
                    }
                    break;
                    case PENDING: {
                        for (String s : this.openedReports.getSection("Reports").getAll().getKeys(false)) {
                            String targetSet = this.openedReports.getString("Reports." + s + ".Target");
                            String statusSet = this.openedReports.getString("Reports." + s + ".Status");
                            if (targetSet.equals(value) && statusSet.equals("Pending")) {
                                String player = this.openedReports.getString("Reports." + s + ".Player");
                                String reason = this.openedReports.getString("Reports." + s + ".Reason");
                                String member = this.openedReports.getString("Reports." + s + ".Member");
                                String date = this.openedReports.getString("Reports." + s + ".Date");
                                Report report = new Report(player, targetSet, reason, statusSet, member, s, date);
                                list.add(report);
                            }
                        }
                    }
                    break;
                    case CLOSED: {
                        for (String s : this.closedReports.getSection("Reports").getAll().getKeys(false)) {
                            String targetSet = this.closedReports.getString("Reports." + s + ".Target");
                            String statusSet = this.closedReports.getString("Reports." + s + ".Status");
                            if (targetSet.equals(value) && statusSet.equals("Closed")) {
                                String player = this.closedReports.getString("Reports." + s + ".Player");
                                String reason = this.closedReports.getString("Reports." + s + ".Reason");
                                String member = this.closedReports.getString("Reports." + s + ".Member");
                                String date = this.closedReports.getString("Reports." + s + ".Date");
                                Report report = new Report(player, targetSet, reason, statusSet, member, s, date);
                                list.add(report);
                            }
                        }
                    }
                    break;
                }
            }
            break;
            case MEMBER: {
                switch (status) {
                    case PENDING:
                    case PROGRESS: {
                        for (String s : this.openedReports.getSection("Reports").getAll().getKeys(false)) {
                            String memberSet = this.openedReports.getString("Reports." + s + ".Member");
                            String statusSet = this.openedReports.getString("Reports." + s + ".Status");
                            if (memberSet.equals(value) && statusSet.equals("Progress")) {
                                String player = this.openedReports.getString("Reports." + s + ".Player");
                                String target = this.openedReports.getString("Reports." + s + ".Target");
                                String reason = this.openedReports.getString("Reports." + s + ".Reason");
                                String date = this.openedReports.getString("Reports." + s + ".Date");
                                Report report = new Report(player, target, reason, statusSet, memberSet, s, date);
                                list.add(report);
                            }
                        }
                    }
                    break;
                    case CLOSED: {
                        for (String s : this.closedReports.getSection("Reports").getAll().getKeys(false)) {
                            String memberSet = this.closedReports.getString("Reports." + s + ".Member");
                            String statusSet = this.closedReports.getString("Reports." + s + ".Status");
                            if (memberSet.equals(value) && statusSet.equals("Closed")) {
                                String player = this.closedReports.getString("Reports." + s + ".Player");
                                String target = this.closedReports.getString("Reports." + s + ".Target");
                                String reason = this.closedReports.getString("Reports." + s + ".Reason");
                                String date = this.closedReports.getString("Reports." + s + ".Date");
                                Report report = new Report(player, target, reason, statusSet, memberSet, s, date);
                                list.add(report);
                            }
                        }
                    }
                    break;
                }
            }
            break;
        }
        reports.accept(list);
    }

    @Override
    public void getReports(Report.ReportStatus status, Consumer<Set<Report>> reports) {
        Set<Report> list = new HashSet<>();
        switch (status) {
            case PROGRESS: {
                for (String s : this.openedReports.getSection("Reports").getAll().getKeys(false)) {
                    String statusSet = this.openedReports.getString("Reports." + s + ".Status");
                    if (statusSet.equals("Progress")) {
                        String playerSet = this.openedReports.getString("Reports." + s + ".Player");
                        String target = this.openedReports.getString("Reports." + s + ".Target");
                        String reason = this.openedReports.getString("Reports." + s + ".Reason");
                        String member = this.openedReports.getString("Reports." + s + ".Member");
                        String date = this.openedReports.getString("Reports." + s + ".Date");
                        Report report = new Report(playerSet, target, reason, statusSet, member, s, date);
                        list.add(report);
                    }
                }
            }
            break;
            case PENDING: {
                for (String s : this.openedReports.getSection("Reports").getAll().getKeys(false)) {
                    String statusSet = this.openedReports.getString("Reports." + s + ".Status");
                    if (statusSet.equals("Pending")) {
                        String playerSet = this.openedReports.getString("Reports." + s + ".Player");
                        String target = this.openedReports.getString("Reports." + s + ".Target");
                        String reason = this.openedReports.getString("Reports." + s + ".Reason");
                        String member = this.openedReports.getString("Reports." + s + ".Member");
                        String date = this.openedReports.getString("Reports." + s + ".Date");
                        Report report = new Report(playerSet, target, reason, statusSet, member, s, date);
                        list.add(report);
                    }
                }
            }
            break;
            case CLOSED: {
                for (String s : this.closedReports.getSection("Reports").getAll().getKeys(false)) {
                    String statusSet = this.closedReports.getString("Reports." + s + ".Status");
                    if (statusSet.equals("Closed")) {
                        String playerSet = this.closedReports.getString("Reports." + s + ".Player");
                        String target = this.closedReports.getString("Reports." + s + ".Target");
                        String reason = this.closedReports.getString("Reports." + s + ".Reason");
                        String member = this.closedReports.getString("Reports." + s + ".Member");
                        String date = this.closedReports.getString("Reports." + s + ".Date");
                        Report report = new Report(playerSet, target, reason, statusSet, member, s, date);
                        list.add(report);
                    }
                }
            }
        }
        reports.accept(list);
    }

    @Override
    public String getProvider() {
        return "Yaml";
    }

}
