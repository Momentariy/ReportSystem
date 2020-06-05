package net.llamadevelopment.reportsystem.components.managers.database;

import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.data.Report;
import net.llamadevelopment.reportsystem.components.data.ReportSearch;
import net.llamadevelopment.reportsystem.components.data.ReportStatus;

import java.util.List;

public class Provider {

    public void connect(ReportSystem server) {

    }

    public void disconnect(ReportSystem server) {

    }

    public void createReport(String player, String target, String reason) {

    }

    public void deleteReport(String id) {

    }

    public boolean hasReported(String player, String target) {
        return false;
    }

    public boolean reportIDExists(String id, ReportStatus status) {
        return false;
    }

    public void closeReport(String id) {

    }

    public void updateStatus(String id, String status) {

    }

    public void updateStaffmember(String id, String s) {

    }

    public Report getReport(ReportStatus status, String value) {
        return null;
    }

    public List<Report> getReports(ReportStatus status, ReportSearch search, String value) {
        return null;
    }

    public List<Report> getReports(ReportStatus status) {
        return null;
    }

    public String getProvider() {
        return null;
    }

}
