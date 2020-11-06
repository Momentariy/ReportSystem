package net.llamadevelopment.reportsystem.components.provider;

import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.data.Report;

import java.util.Set;
import java.util.function.Consumer;

public class Provider {

    public void connect(ReportSystem server) {

    }

    public void disconnect(ReportSystem server) {

    }

    public void createReport(String player, String target, String reason) {

    }

    public void deleteReport(String id) {

    }

    public void hasReported(String player, String target, Consumer<Boolean> hasReported) {

    }

    public void reportIDExists(String id, Report.ReportStatus status, Consumer<Boolean> exists) {

    }

    public void closeReport(String id) {

    }

    public void updateStatus(String id, String status) {

    }

    public void updateStaffmember(String id, String s) {

    }

    public void getReport(Report.ReportStatus status, String value, Consumer<Report> report) {

    }

    public void getReports(Report.ReportStatus status, Report.ReportSearch search, String value, Consumer<Set<Report>> reports) {

    }

    public void getReports(Report.ReportStatus status, Consumer<Set<Report>> reports) {

    }

    public String getProvider() {
        return null;
    }

}
