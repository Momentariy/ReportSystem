package net.llamadevelopment.reportsystem.components.provider;

import cn.nukkit.utils.Config;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.data.Report;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MySqlProvider extends Provider {

    Connection connection;

    @Override
    public void connect(ReportSystem server) {
        CompletableFuture.runAsync(() -> {
            try {
                Config config = ReportSystem.getInstance().getConfig();
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + config.getString("MySql.Host") + ":" + config.getString("MySql.Port") + "/" + config.getString("MySql.Database") + "?autoReconnect=true&useGmtMillisForDatetimes=true&serverTimezone=GMT", config.getString("MySql.User"), config.getString("MySql.Password"));
                update("CREATE TABLE IF NOT EXISTS opened_reports(player VARCHAR(30), target VARCHAR(30), reason VARCHAR(100), status VARCHAR(30), member VARCHAR(30), id VARCHAR(6), date VARCHAR(30), PRIMARY KEY (id));");
                update("CREATE TABLE IF NOT EXISTS closed_reports(player VARCHAR(30), target VARCHAR(30), reason VARCHAR(100), status VARCHAR(30), member VARCHAR(30), id VARCHAR(6), date VARCHAR(30), PRIMARY KEY (id));");
                server.getLogger().info("[MySqlClient] Connection opened.");
            } catch (Exception e) {
                e.printStackTrace();
                server.getLogger().info("[MySqlClient] Failed to connect to database.");
            }
        });
    }

    public Connection getConnection() {
        return connection;
    }

    public void update(String query) {
        CompletableFuture.runAsync(() -> {
            if (connection != null) {
                try {
                    PreparedStatement ps = connection.prepareStatement(query);
                    ps.executeUpdate();
                    ps.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void disconnect(ReportSystem server) {
        if (connection != null) {
            try {
                connection.close();
                server.getLogger().info("[MySqlClient] Connection closed.");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                server.getLogger().info("[MySqlClient] Failed to close connection.");
            }
        }
    }

    @Override
    public void createReport(String player, String target, String reason) {
        update("INSERT INTO opened_reports (PLAYER, TARGET, REASON, STATUS, MEMBER, ID, DATE) VALUES ('" + player + "', '" + target + "', '" + reason + "', 'Pending', 'Unknown', '" + ReportSystemAPI.getRandomIDCode() + "', '" + ReportSystemAPI.getDate() + "');");
    }

    @Override
    public void deleteReport(String id) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement preparedStatement = getConnection().prepareStatement("DELETE FROM opened_reports WHERE ID = ?");
                preparedStatement.setString(1, id);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public boolean hasReported(String player, String target) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM opened_reports WHERE TARGET = ?");
            preparedStatement.setString(1, target);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                if (rs.getString("TARGET").equals(target) && rs.getString("PLAYER").equals(player) && rs.getString("STATUS").equals("Pending")) return true;
            } else return false;
            rs.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean reportIDExists(String id, Report.ReportStatus status) {
        switch (status) {
            case PENDING:
            case PROGRESS: {
                try {
                    PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM opened_reports WHERE ID = ?");
                    preparedStatement.setString(1, id);
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()) return rs.getString("ID") != null;
                    rs.close();
                    preparedStatement.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case CLOSED: {
                try {
                    PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM closed_reports WHERE ID = ?");
                    preparedStatement.setString(1, id);
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()) return rs.getString("ID") != null;
                    rs.close();
                    preparedStatement.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
        }
        return false;
    }

    @Override
    public void closeReport(String id) {
        CompletableFuture.runAsync(() -> {
            Report report = getReport(Report.ReportStatus.PROGRESS, id);
            update("INSERT INTO closed_reports (PLAYER, TARGET, REASON, STATUS, MEMBER, ID, DATE) VALUES ('" + report.getPlayer() + "', '" + report.getTarget() + "', '" + report.getReason() + "', 'Closed', '" + report.getMember() + "', '" + report.getId() + "', '" + report.getDate() + "');");
            deleteReport(id);
        });
    }

    @Override
    public void updateStatus(String id, String status) {
        update("UPDATE opened_reports SET STATUS= '" + status + "' WHERE ID= '" + id + "';");
    }

    @Override
    public void updateStaffmember(String id, String s) {
        update("UPDATE opened_reports SET MEMBER= '" + s + "' WHERE ID= '" + id + "';");
    }

    @Override
    public Report getReport(Report.ReportStatus status, String value) {
        switch (status) {
            case PENDING:
            case PROGRESS: {
                try {
                    PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM opened_reports WHERE ID = ?");
                    preparedStatement.setString(1, value);
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()) {
                        return new Report(rs.getString("PLAYER"), rs.getString("TARGET"), rs.getString("REASON"), rs.getString("STATUS"), rs.getString("MEMBER"), rs.getString("ID"), rs.getString("DATE"));
                    }
                    rs.close();
                    preparedStatement.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case CLOSED: {
                try {
                    PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM closed_reports WHERE ID = ?");
                    preparedStatement.setString(1, value);
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()) {
                        return new Report(rs.getString("PLAYER"), rs.getString("TARGET"), rs.getString("REASON"), rs.getString("STATUS"), rs.getString("MEMBER"), rs.getString("ID"), rs.getString("DATE"));
                    }
                    rs.close();
                    preparedStatement.close();
                } catch (Exception e) {
                    e.printStackTrace();
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
                        try {
                            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM opened_reports WHERE PLAYER = ?");
                            preparedStatement.setString(1, value);
                            ResultSet rs = preparedStatement.executeQuery();
                            while (rs.next()) {
                                if (rs.getString("STATUS").equals("Progress")) {
                                    Report report = new Report(rs.getString("PLAYER"), rs.getString("TARGET"), rs.getString("REASON"), rs.getString("STATUS"), rs.getString("MEMBER"), rs.getString("ID"), rs.getString("DATE"));
                                    list.add(report);
                                }
                            }
                            rs.close();
                            preparedStatement.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                    case PENDING: {
                        try {
                            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM opened_reports WHERE PLAYER = ?");
                            preparedStatement.setString(1, value);
                            ResultSet rs = preparedStatement.executeQuery();
                            while (rs.next()) {
                                if (rs.getString("STATUS").equals("Pending")) {
                                    Report report = new Report(rs.getString("PLAYER"), rs.getString("TARGET"), rs.getString("REASON"), rs.getString("STATUS"), rs.getString("MEMBER"), rs.getString("ID"), rs.getString("DATE"));
                                    list.add(report);
                                }
                            }
                            rs.close();
                            preparedStatement.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                    case CLOSED: {
                        try {
                            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM closed_reports WHERE PLAYER = ?");
                            preparedStatement.setString(1, value);
                            ResultSet rs = preparedStatement.executeQuery();
                            while (rs.next()) {
                                if (rs.getString("STATUS").equals("Closed")) {
                                    Report report = new Report(rs.getString("PLAYER"), rs.getString("TARGET"), rs.getString("REASON"), rs.getString("STATUS"), rs.getString("MEMBER"), rs.getString("ID"), rs.getString("DATE"));
                                    list.add(report);
                                }
                            }
                            rs.close();
                            preparedStatement.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
            }
            break;
            case TARGET: {
                switch (status) {
                    case PROGRESS: {
                        try {
                            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM opened_reports WHERE TARGET = ?");
                            preparedStatement.setString(1, value);
                            ResultSet rs = preparedStatement.executeQuery();
                            while (rs.next()) {
                                if (rs.getString("STATUS").equals("Progress")) {
                                    Report report = new Report(rs.getString("PLAYER"), rs.getString("TARGET"), rs.getString("REASON"), rs.getString("STATUS"), rs.getString("MEMBER"), rs.getString("ID"), rs.getString("DATE"));
                                    list.add(report);
                                }
                            }
                            rs.close();
                            preparedStatement.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                    case PENDING: {
                        try {
                            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM opened_reports WHERE TARGET = ?");
                            preparedStatement.setString(1, value);
                            ResultSet rs = preparedStatement.executeQuery();
                            while (rs.next()) {
                                if (rs.getString("STATUS").equals("Pending")) {
                                    Report report = new Report(rs.getString("PLAYER"), rs.getString("TARGET"), rs.getString("REASON"), rs.getString("STATUS"), rs.getString("MEMBER"), rs.getString("ID"), rs.getString("DATE"));
                                    list.add(report);
                                }
                            }
                            rs.close();
                            preparedStatement.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                    case CLOSED: {
                        try {
                            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM closed_reports WHERE TARGET = ?");
                            preparedStatement.setString(1, value);
                            ResultSet rs = preparedStatement.executeQuery();
                            while (rs.next()) {
                                if (rs.getString("STATUS").equals("Closed")) {
                                    Report report = new Report(rs.getString("PLAYER"), rs.getString("TARGET"), rs.getString("REASON"), rs.getString("STATUS"), rs.getString("MEMBER"), rs.getString("ID"), rs.getString("DATE"));
                                    list.add(report);
                                }
                            }
                            rs.close();
                            preparedStatement.close();
                        } catch (Exception e) {
                            e.printStackTrace();
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
                        try {
                            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM opened_reports WHERE MEMBER = ?");
                            preparedStatement.setString(1, value);
                            ResultSet rs = preparedStatement.executeQuery();
                            while (rs.next()) {
                                if (rs.getString("STATUS").equals("Progress")) {
                                    Report report = new Report(rs.getString("PLAYER"), rs.getString("TARGET"), rs.getString("REASON"), rs.getString("STATUS"), rs.getString("MEMBER"), rs.getString("ID"), rs.getString("DATE"));
                                    list.add(report);
                                }
                            }
                            rs.close();
                            preparedStatement.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                    case CLOSED: {
                        try {
                            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM closed_reports WHERE MEMBER = ?");
                            preparedStatement.setString(1, value);
                            ResultSet rs = preparedStatement.executeQuery();
                            while (rs.next()) {
                                if (rs.getString("STATUS").equals("Closed")) {
                                    Report report = new Report(rs.getString("PLAYER"), rs.getString("TARGET"), rs.getString("REASON"), rs.getString("STATUS"), rs.getString("MEMBER"), rs.getString("ID"), rs.getString("DATE"));
                                    list.add(report);
                                }
                            }
                            rs.close();
                            preparedStatement.close();
                        } catch (Exception e) {
                            e.printStackTrace();
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
    public List<Report> getReports(Report.ReportStatus status) {
        List<Report> list = new ArrayList<>();
        switch (status) {
            case PROGRESS: {
                try {
                    PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM opened_reports WHERE STATUS = ?");
                    preparedStatement.setString(1, "Progress");
                    ResultSet rs = preparedStatement.executeQuery();
                    while (rs.next()) {
                        Report report = new Report(rs.getString("PLAYER"), rs.getString("TARGET"), rs.getString("REASON"), rs.getString("STATUS"), rs.getString("MEMBER"), rs.getString("ID"), rs.getString("DATE"));
                        list.add(report);
                    }
                    rs.close();
                    preparedStatement.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case PENDING: {
                try {
                    PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM opened_reports WHERE STATUS = ?");
                    preparedStatement.setString(1, "Pending");
                    ResultSet rs = preparedStatement.executeQuery();
                    while (rs.next()) {
                        Report report = new Report(rs.getString("PLAYER"), rs.getString("TARGET"), rs.getString("REASON"), rs.getString("STATUS"), rs.getString("MEMBER"), rs.getString("ID"), rs.getString("DATE"));
                        list.add(report);
                    }
                    rs.close();
                    preparedStatement.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case CLOSED: {
                try {
                    PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM closed_reports WHERE STATUS = ?");
                    preparedStatement.setString(1, "Closed");
                    ResultSet rs = preparedStatement.executeQuery();
                    while (rs.next()) {
                        Report report = new Report(rs.getString("PLAYER"), rs.getString("TARGET"), rs.getString("REASON"), rs.getString("STATUS"), rs.getString("MEMBER"), rs.getString("ID"), rs.getString("DATE"));
                        list.add(report);
                    }
                    rs.close();
                    preparedStatement.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
        }
        return list;
    }

    @Override
    public String getProvider() {
        return "MySql";
    }
}
