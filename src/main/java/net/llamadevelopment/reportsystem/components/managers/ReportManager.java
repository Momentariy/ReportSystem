package net.llamadevelopment.reportsystem.components.managers;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import com.mongodb.client.MongoCollection;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.managers.database.MongoDBProvider;
import net.llamadevelopment.reportsystem.components.managers.database.MySqlProvider;
import net.llamadevelopment.reportsystem.components.messaging.Messages;
import net.llamadevelopment.reportsystem.components.utils.ReportUtil;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReportManager {

    public static ArrayList<Player> cooldown = new ArrayList<Player>();
    private static ReportSystem instance = ReportSystem.getInstance();

    public static void createReport(String target, String creator, String reason, String id, String date) {
        if (instance.isMongodb()) {
            Document document = new Document("target", target)
                    .append("creator", creator)
                    .append("reason", reason)
                    .append("id", id)
                    .append("date", date)
                    .append("status", "Open")
                    .append("manager", "none");
            MongoDBProvider.getOpenReportCollection().insertOne(document);
        } else if (instance.isMysql()) {
            MySqlProvider.update("INSERT INTO opened (TARGET, CREATOR, REASON, ID, DATE, STATUS, MANAGER) VALUES ('" + target + "', '" + creator + "', '" + reason + "', '" + id + "', '" + date + "', 'Open', 'none');");
        } else if (instance.isYaml()) {
            Config rd = new Config(ReportSystem.getInstance().getDataFolder() + "/data/reportdata.yml", Config.YAML);
            Config or = new Config(ReportSystem.getInstance().getDataFolder() + "/data/openreports.yml", Config.YAML);
            or.set("Report." + id, id);
            or.set("Report." + id + ".Target", target);
            or.set("Report." + id + ".Creator", creator);
            or.set("Report." + id + ".Reason", reason);
            or.set("Report." + id + ".Date", date);
            or.set("Report." + id + ".Status", "Open");
            or.set("Report." + id + ".Manager", "none");
            or.save();
            or.reload();
            List<String> list = rd.getStringList("Data");
            list.add(id);
            rd.set("Data", list);
            rd.save();
            rd.reload();
        }
        for (Player player : ReportSystem.getInstance().getServer().getOnlinePlayers().values()) {
            if (player.hasPermission("reportsystem.command.reportmanager")) {
                player.sendMessage(Messages.getAndReplace("Messages.ReportNotification"));
            }
        }
    }

    public static void deleteReport(String id) {
        ReportUtil reportUtil = getOpenReport(id);
        Config rd = new Config(ReportSystem.getInstance().getDataFolder() + "/data/reportdata.yml", Config.YAML);
        List<String> list = rd.getStringList(reportUtil.getManager());
        list.remove(id);
        rd.set(reportUtil.getManager(), list);
        rd.save();
        rd.reload();
        if (instance.isMongodb()) {
            MongoCollection<Document> collection = MongoDBProvider.getOpenReportCollection();
            collection.deleteOne(new Document("id", id));
        } else if (instance.isMysql()) {
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("DELETE FROM opened WHERE ID = ?");
                preparedStatement.setString(1, id);
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (instance.isYaml()) {
            Config or = new Config(ReportSystem.getInstance().getDataFolder() + "/data/openreports.yml", Config.YAML);
            Map<String, Object> map = or.getSection("Report").getAllMap();
            map.remove(id);
            or.set("Report", map);
            or.save();
            or.reload();
        }
    }

    public static void updateManager(String id, String manager) {
        Config rd = new Config(ReportSystem.getInstance().getDataFolder() + "/data/reportdata.yml", Config.YAML);
        List<String> list = rd.getStringList(manager);
        list.add(id);
        rd.set(manager, list);
        rd.save();
        rd.reload();
        if (instance.isMongodb()) {
            Document document = new Document("id", id);
            Document found = MongoDBProvider.getOpenReportCollection().find(document).first();
            Bson newManager = new Document("manager", manager);
            Bson newManager1 = new Document("$set", newManager);
            MongoDBProvider.getOpenReportCollection().updateOne(found, newManager1);
        } else if (instance.isMysql()) {
            MySqlProvider.update("UPDATE opened SET MANAGER= '" + manager +"' WHERE ID= '" + id + "';");
        } else if (instance.isYaml()) {
            Config or = new Config(ReportSystem.getInstance().getDataFolder() + "/data/openreports.yml", Config.YAML);
            or.set("Report." + id + ".Manager", manager);
            or.save();
            or.reload();
        }
    }

    public static void closeReport(String id) {
        ReportUtil reportUtil = getOpenReport(id);
        Config rd = new Config(ReportSystem.getInstance().getDataFolder() + "/data/reportdata.yml", Config.YAML);
        List<String> list = rd.getStringList(reportUtil.getManager());
        list.remove(id);
        rd.set(reportUtil.getManager(), list);
        rd.save();
        rd.reload();
        if (instance.isMongodb()) {
            Document document = new Document("target", reportUtil.getTarget())
                    .append("creator", reportUtil.getCreator())
                    .append("reason", reportUtil.getReason())
                    .append("id", reportUtil.getId())
                    .append("date", reportUtil.getDate())
                    .append("status", "Closed")
                    .append("manager", reportUtil.getManager());
            MongoDBProvider.getCloseReportCollection().insertOne(document);
            MongoCollection<Document> collection = MongoDBProvider.getOpenReportCollection();
            collection.deleteOne(new Document("id", id));
        } else if (instance.isMysql()) {
            MySqlProvider.update("INSERT INTO closed (TARGET, CREATOR, REASON, ID, DATE, STATUS, MANAGER) VALUES ('" + reportUtil.getTarget() + "', '" + reportUtil.getCreator() + "', '" + reportUtil.getReason() + "', '" + reportUtil.getId() + "', '" + reportUtil.getDate() + "', 'Closed', '" + reportUtil.getManager() + "');");
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("DELETE FROM opened WHERE ID = ?");
                preparedStatement.setString(1, id);
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (instance.isYaml()) {
            Config cr = new Config(ReportSystem.getInstance().getDataFolder() + "/data/closereports.yml", Config.YAML);
            cr.set("Report." + reportUtil.getId(), reportUtil.getId());
            cr.set("Report." + reportUtil.getId() + ".Target", reportUtil.getTarget());
            cr.set("Report." + reportUtil.getId() + ".Creator", reportUtil.getCreator());
            cr.set("Report." + reportUtil.getId() + ".Reason", reportUtil.getReason());
            cr.set("Report." + reportUtil.getId() + ".Date", reportUtil.getDate());
            cr.set("Report." + reportUtil.getId() + ".Status", "Closed");
            cr.set("Report." + reportUtil.getId() + ".Manager", reportUtil.getManager());
            cr.save();
            cr.reload();
            Config or = new Config(ReportSystem.getInstance().getDataFolder() + "/data/openreports.yml", Config.YAML);
            Map<String, Object> map = or.getSection("Report").getAllMap();
            map.remove(id);
            or.set("Report", map);
            or.save();
            or.reload();
        }
    }

    public static void updateStatus(String id, String status) {
        ReportUtil reportUtil = getOpenReport(id);
        if (reportUtil.getStatus().equalsIgnoreCase("Open")) {
            Config rd = new Config(ReportSystem.getInstance().getDataFolder() + "/data/reportdata.yml", Config.YAML);
            List<String> list = rd.getStringList("Data");
            list.remove(id);
            rd.set("Data", list);
            rd.save();
            rd.reload();
        }
        if (instance.isMongodb()) {
            Document document = new Document("id", id);
            Document found = MongoDBProvider.getOpenReportCollection().find(document).first();
            Bson newStatus = new Document("status", status);
            Bson newStatus1 = new Document("$set", newStatus);
            MongoDBProvider.getOpenReportCollection().updateOne(found, newStatus1);
        } else if (instance.isMysql()) {
            MySqlProvider.update("UPDATE opened SET STATUS= '" + status +"' WHERE ID= '" + id + "';");
        } else if (instance.isYaml()) {
            Config or = new Config(ReportSystem.getInstance().getDataFolder() + "/data/openreports.yml", Config.YAML);
            or.set("Report." + id + ".Status", status);
            or.save();
            or.reload();
        }
    }

    public static ReportUtil getOpenReport(String rID) {
        String target = "";
        String creator = "";
        String reason = "";
        String id = "";
        String date = "";
        String status = "";
        String manager = "";

        if (instance.isMongodb()) {
            Document found = MongoDBProvider.getOpenReportCollection().find(new Document("id", rID)).first();
            if (found != null) {
                target = found.getString("target");
                creator = found.getString("creator");
                reason = found.getString("reason");
                id = rID;
                date = found.getString("date");
                status = found.getString("status");
                manager = found.getString("manager");
            }
        } else if (instance.isMysql()) {
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM opened WHERE ID = ?");
                preparedStatement.setString(1, rID);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    target = rs.getString("TARGET");
                    creator = rs.getString("CREATOR");
                    reason = rs.getString("REASON");
                    id = rs.getString("ID");
                    date = rs.getString("DATE");
                    status = rs.getString("STATUS");
                    manager = rs.getString("MANAGER");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (instance.isYaml()) {
            Config or = new Config(ReportSystem.getInstance().getDataFolder() + "/data/openreports.yml", Config.YAML);
            target = or.getString("Report." + rID + ".Target");
            creator = or.getString("Report." + rID + ".Creator");
            reason = or.getString("Report." + rID + ".Reason");
            id = rID;
            date = or.getString("Report." + rID + ".Date");
            status = or.getString("Report." + rID + ".Status");
            manager = or.getString("Report." + rID + ".Manager");
        }
        return new ReportUtil(target, reason, creator, id, date, status, manager);
    }

    public static ReportUtil getClosedReport(String rID) {
        String target = "";
        String creator = "";
        String reason = "";
        String id = "";
        String date = "";
        String status = "";
        String manager = "";

        if (instance.isMongodb()) {
            Document found = MongoDBProvider.getCloseReportCollection().find(new Document("id", rID)).first();
            if (found != null) {
                target = found.getString("target");
                creator = found.getString("creator");
                reason = found.getString("reason");
                id = rID;
                date = found.getString("date");
                status = found.getString("status");
                manager = found.getString("manager");
            }
        } else if (instance.isMysql()) {
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM closed WHERE ID = ?");
                preparedStatement.setString(1, rID);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    target = rs.getString("TARGET");
                    creator = rs.getString("CREATOR");
                    reason = rs.getString("REASON");
                    id = rs.getString("ID");
                    date = rs.getString("DATE");
                    status = rs.getString("STATUS");
                    manager = rs.getString("MANAGER");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (instance.isYaml()) {
            Config or = new Config(ReportSystem.getInstance().getDataFolder() + "/data/closereports.yml", Config.YAML);
            target = or.getString("Report." + rID + ".Target");
            creator = or.getString("Report." + rID + ".Creator");
            reason = or.getString("Report." + rID + ".Reason");
            id = rID;
            date = or.getString("Report." + rID + ".Date");
            status = or.getString("Report." + rID + ".Status");
            manager = or.getString("Report." + rID + ".Manager");
        }
        return new ReportUtil(target, reason, creator, id, date, status, manager);
    }

    public static String getID() {
        String string = "";
        int lastrandom = 0;
        for (int i = 0; i < 6; i++) {
            Random random = new Random();
            int rand = random.nextInt(9);
            while (rand == lastrandom) {
                rand = random.nextInt(9);
            }
            lastrandom = rand;
            string = string + rand;
        }
        return string;
    }

    public static String getDate() {
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");
        String now1 = dateFormat.format(now);
        return now1;
    }

}
