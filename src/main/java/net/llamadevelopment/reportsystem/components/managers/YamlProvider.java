package net.llamadevelopment.reportsystem.components.managers;

import cn.nukkit.utils.Config;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.data.Report;
import net.llamadevelopment.reportsystem.components.data.ReportSearch;
import net.llamadevelopment.reportsystem.components.data.ReportStatus;
import net.llamadevelopment.reportsystem.components.managers.database.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class YamlProvider extends Provider {

    Config openedReports, closedReports;

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
        openedReports.set("Reports." + id + ".Player", player);
        openedReports.set("Reports." + id + ".Target", target);
        openedReports.set("Reports." + id + ".Reason", reason);
        openedReports.set("Reports." + id + ".Status", "Pending");
        openedReports.set("Reports." + id + ".Member", "Unknown");
        openedReports.set("Reports." + id + ".Date", date);
        openedReports.save();
        openedReports.reload();
    }

    @Override
    public void deleteReport(String id) {
        Map<String, Object> map = openedReports.getSection("Reports").getAllMap();
        map.remove(id);
        openedReports.set("Reports", map);
        openedReports.save();
        openedReports.reload();
    }

    @Override
    public boolean hasReported(String player, String target) {
        for (String id : openedReports.getSection("Reports").getKeys(false)) {
            if (openedReports.getString("Reports." + id + ".Target").equals(target) && openedReports.getString("Reports." + id + ".Player").equals(player) && openedReports.getString("Reports." + id + ".Status").equals("Pending")) return true;
        }
        return false;
    }

    @Override
    public boolean reportIDExists(String id, ReportStatus status) {
        switch (status) {
            case PENDING:
            case PROGRESS: {
                return openedReports.exists("Reports." + id);
            }
            case CLOSED: {
                return closedReports.exists("Reports." + id);
            }
        }
        return false;
    }

    @Override
    public void closeReport(String id) {
        Report report = getReport(ReportStatus.PROGRESS, id);
        closedReports.set("Reports." + id + ".Player", report.getPlayer());
        closedReports.set("Reports." + id + ".Target", report.getTarget());
        closedReports.set("Reports." + id + ".Reason", report.getReason());
        closedReports.set("Reports." + id + ".Status", "Closed");
        closedReports.set("Reports." + id + ".Member", report.getMember());
        closedReports.set("Reports." + id + ".Date", report.getDate());
        closedReports.save();
        closedReports.reload();
        deleteReport(id);
    }

    @Override
    public void updateStatus(String id, String status) {
        openedReports.set("Reports." + id + ".Status", status);
        openedReports.save();
        openedReports.reload();
    }

    @Override
    public void updateStaffmember(String id, String s) {
        openedReports.set("Reports." + id + ".Member", s);
        openedReports.save();
        openedReports.reload();
    }

    @Override
    public Report getReport(ReportStatus status, String value) {
        switch (status) {
            case PENDING:
            case PROGRESS: {
                String player = openedReports.getString("Reports." + value + ".Player");
                String target = openedReports.getString("Reports." + value + ".Target");
                String reason = openedReports.getString("Reports." + value + ".Reason");
                String statusSet = openedReports.getString("Reports." + value + ".Status");
                String member = openedReports.getString("Reports." + value + ".Member");
                String date = openedReports.getString("Reports." + value + ".Date");
                return new Report(player, target, reason, statusSet, member, value, date);
            }
            case CLOSED: {
                String player = closedReports.getString("Reports." + value + ".Player");
                String target = closedReports.getString("Reports." + value + ".Target");
                String reason = closedReports.getString("Reports." + value + ".Reason");
                String statusSet = closedReports.getString("Reports." + value + ".Status");
                String member = closedReports.getString("Reports." + value + ".Member");
                String date = closedReports.getString("Reports." + value + ".Date");
                return new Report(player, target, reason, statusSet, member, value, date);
            }
        }
        return null;
    }

    @Override
    public List<Report> getReports(ReportStatus status, ReportSearch search, String value) {
        List<Report> list = new ArrayList<>();
        switch (search) {
            case PLAYER: {
                switch (status) {
                    case PROGRESS: {
                        for (String s : openedReports.getSection("Reports").getAll().getKeys(false)) {
                            String playerSet = openedReports.getString("Reports." + s + ".Player");
                            String statusSet = openedReports.getString("Reports." + s + ".Status");
                            if (playerSet.equals(value) && statusSet.equals("Progress")) {
                                String target = openedReports.getString("Reports." + s + ".Target");
                                String reason = openedReports.getString("Reports." + s + ".Reason");
                                String member = openedReports.getString("Reports." + s + ".Member");
                                String date = openedReports.getString("Reports." + s + ".Date");
                                Report report = new Report(playerSet, target, reason, statusSet, member, s, date);
                                list.add(report);
                            }
                        }
                    }
                    break;
                    case PENDING: {
                        for (String s : openedReports.getSection("Reports").getAll().getKeys(false)) {
                            String playerSet = openedReports.getString("Reports." + s + ".Player");
                            String statusSet = openedReports.getString("Reports." + s + ".Status");
                            if (playerSet.equals(value) && statusSet.equals("Pending")) {
                                String target = openedReports.getString("Reports." + s + ".Target");
                                String reason = openedReports.getString("Reports." + s + ".Reason");
                                String member = openedReports.getString("Reports." + s + ".Member");
                                String date = openedReports.getString("Reports." + s + ".Date");
                                Report report = new Report(playerSet, target, reason, statusSet, member, s, date);
                                list.add(report);
                            }
                        }
                    }
                    break;
                    case CLOSED: {
                        for (String s : closedReports.getSection("Reports").getAll().getKeys(false)) {
                            String playerSet = closedReports.getString("Reports." + s + ".Player");
                            String statusSet = closedReports.getString("Reports." + s + ".Status");
                            if (playerSet.equals(value) && statusSet.equals("Closed")) {
                                String target = closedReports.getString("Reports." + s + ".Target");
                                String reason = closedReports.getString("Reports." + s + ".Reason");
                                String member = closedReports.getString("Reports." + s + ".Member");
                                String date = closedReports.getString("Reports." + s + ".Date");
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
                        for (String s : openedReports.getSection("Reports").getAll().getKeys(false)) {
                            String targetSet = openedReports.getString("Reports." + s + ".Target");
                            String statusSet = openedReports.getString("Reports." + s + ".Status");
                            if (targetSet.equals(value) && statusSet.equals("Progress")) {
                                String player = openedReports.getString("Reports." + s + ".Player");
                                String reason = openedReports.getString("Reports." + s + ".Reason");
                                String member = openedReports.getString("Reports." + s + ".Member");
                                String date = openedReports.getString("Reports." + s + ".Date");
                                Report report = new Report(player, targetSet, reason, statusSet, member, s, date);
                                list.add(report);
                            }
                        }
                    }
                    break;
                    case PENDING: {
                        for (String s : openedReports.getSection("Reports").getAll().getKeys(false)) {
                            String targetSet = openedReports.getString("Reports." + s + ".Target");
                            String statusSet = openedReports.getString("Reports." + s + ".Status");
                            if (targetSet.equals(value) && statusSet.equals("Pending")) {
                                String player = openedReports.getString("Reports." + s + ".Player");
                                String reason = openedReports.getString("Reports." + s + ".Reason");
                                String member = openedReports.getString("Reports." + s + ".Member");
                                String date = openedReports.getString("Reports." + s + ".Date");
                                Report report = new Report(player, targetSet, reason, statusSet, member, s, date);
                                list.add(report);
                            }
                        }
                    }
                    break;
                    case CLOSED: {
                        for (String s : closedReports.getSection("Reports").getAll().getKeys(false)) {
                            String targetSet = closedReports.getString("Reports." + s + ".Target");
                            String statusSet = closedReports.getString("Reports." + s + ".Status");
                            if (targetSet.equals(value) && statusSet.equals("Closed")) {
                                String player = closedReports.getString("Reports." + s + ".Player");
                                String reason = closedReports.getString("Reports." + s + ".Reason");
                                String member = closedReports.getString("Reports." + s + ".Member");
                                String date = closedReports.getString("Reports." + s + ".Date");
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
                        for (String s : openedReports.getSection("Reports").getAll().getKeys(false)) {
                            String memberSet = openedReports.getString("Reports." + s + ".Member");
                            String statusSet = openedReports.getString("Reports." + s + ".Status");
                            if (memberSet.equals(value) && statusSet.equals("Progress")) {
                                String player = openedReports.getString("Reports." + s + ".Player");
                                String target = openedReports.getString("Reports." + s + ".Target");
                                String reason = openedReports.getString("Reports." + s + ".Reason");
                                String date = openedReports.getString("Reports." + s + ".Date");
                                Report report = new Report(player, target, reason, statusSet, memberSet, s, date);
                                list.add(report);
                            }
                        }
                    }
                    break;
                    case CLOSED: {
                        for (String s : closedReports.getSection("Reports").getAll().getKeys(false)) {
                            String memberSet = closedReports.getString("Reports." + s + ".Member");
                            String statusSet = closedReports.getString("Reports." + s + ".Status");
                            if (memberSet.equals(value) && statusSet.equals("Closed")) {
                                String player = closedReports.getString("Reports." + s + ".Player");
                                String target = closedReports.getString("Reports." + s + ".Target");
                                String reason = closedReports.getString("Reports." + s + ".Reason");
                                String date = closedReports.getString("Reports." + s + ".Date");
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
        return list;
    }

    @Override
    public List<Report> getReports(ReportStatus status) {
        List<Report> list = new ArrayList<>();
        switch (status) {
            case PROGRESS: {
                for (String s : openedReports.getSection("Reports").getAll().getKeys(false)) {
                    String statusSet = openedReports.getString("Reports." + s + ".Status");
                    if (statusSet.equals("Progress")) {
                        String playerSet = openedReports.getString("Reports." + s + ".Player");
                        String target = openedReports.getString("Reports." + s + ".Target");
                        String reason = openedReports.getString("Reports." + s + ".Reason");
                        String member = openedReports.getString("Reports." + s + ".Member");
                        String date = openedReports.getString("Reports." + s + ".Date");
                        Report report = new Report(playerSet, target, reason, statusSet, member, s, date);
                        list.add(report);
                    }
                }
            }
            break;
            case PENDING: {
                for (String s : openedReports.getSection("Reports").getAll().getKeys(false)) {
                    String statusSet = openedReports.getString("Reports." + s + ".Status");
                    if (statusSet.equals("Pending")) {
                        String playerSet = openedReports.getString("Reports." + s + ".Player");
                        String target = openedReports.getString("Reports." + s + ".Target");
                        String reason = openedReports.getString("Reports." + s + ".Reason");
                        String member = openedReports.getString("Reports." + s + ".Member");
                        String date = openedReports.getString("Reports." + s + ".Date");
                        Report report = new Report(playerSet, target, reason, statusSet, member, s, date);
                        list.add(report);
                    }
                }
            }
            break;
            case CLOSED: {
                for (String s : closedReports.getSection("Reports").getAll().getKeys(false)) {
                    String statusSet = closedReports.getString("Reports." + s + ".Status");
                    if (statusSet.equals("Closed")) {
                        String playerSet = closedReports.getString("Reports." + s + ".Player");
                        String target = closedReports.getString("Reports." + s + ".Target");
                        String reason = closedReports.getString("Reports." + s + ".Reason");
                        String member = closedReports.getString("Reports." + s + ".Member");
                        String date = closedReports.getString("Reports." + s + ".Date");
                        Report report = new Report(playerSet, target, reason, statusSet, member, s, date);
                        list.add(report);
                    }
                }
            }
        }
        return list;
    }

    @Override
    public String getProvider() {
        return "Yaml";
    }
}
