package net.llamadevelopment.reportsystem.components.utils;

public class ReportUtil {

    private String target;
    private String reason;
    private String creator;
    private String id;
    private String date;
    private String status;
    private String manager;

    public ReportUtil(String target, String reason, String creator, String id, String date, String status, String manager) {
        this.target = target;
        this.reason = reason;
        this.creator = creator;
        this.id = id;
        this.date = date;
        this.status = status;
        this.manager = manager;
    }

    public String getTarget() {
        return target;
    }

    public String getReason() {
        return reason;
    }

    public String getCreator() {
        return creator;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public String getManager() {
        return manager;
    }
}
