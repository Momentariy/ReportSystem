package net.llamadevelopment.reportsystem.components.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Report {

    private final String player;
    private final String target;
    private final String reason;
    private final String status;
    private final String member;
    private final String id;
    private final String date;

    public enum  ReportSearch {
        PLAYER, TARGET, MEMBER
    }

    public enum ReportStatus {
        PENDING, PROGRESS, CLOSED
    }

}
