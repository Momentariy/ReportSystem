package net.llamadevelopment.reportsystem.components.utils;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.Config;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.managers.ReportManager;
import net.llamadevelopment.reportsystem.components.managers.database.MongoDBProvider;
import net.llamadevelopment.reportsystem.components.managers.database.MySqlProvider;
import net.llamadevelopment.reportsystem.components.messaging.Messages;
import org.bson.Document;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class FormUiUtil {

    private static ReportSystem instance = ReportSystem.getInstance();
    public static HashMap<Player, String> searchCache = new HashMap<Player, String>();


    public static void sendReportPanel(Player player) {
        Config config = ReportSystem.getInstance().getConfig();
        FormWindowSimple reportPanelForm = new FormWindowSimple(config.getString("Title.Panel"), config.getString("Text.Panel"));
        reportPanelForm.addButton(new ElementButton(config.getString("Buttons.Panel.Reports").replace("&", "§")));
        reportPanelForm.addButton(new ElementButton(config.getString("Buttons.Panel.MyReports").replace("&", "§")));
        reportPanelForm.addButton(new ElementButton(config.getString("Buttons.Panel.SearchReport").replace("&", "§")));
        if (instance.isMysql() || instance.isMongodb()) reportPanelForm.addButton(new ElementButton(config.getString("Buttons.Panel.SearchPlayer").replace("&", "§")));
        reportPanelForm.addButton(new ElementButton(config.getString("Buttons.Panel.Close").replace("&", "§")));
        player.showFormWindow(reportPanelForm);
    }

    public static void sendOpenReports(Player player) {
        Config config = ReportSystem.getInstance().getConfig();
        FormWindowSimple openReportsForm = new FormWindowSimple(config.getString("Title.OpenReports").replace("&", "§"), config.getString("Text.OpenReports").replace("&", "§"));
        if (instance.isMongodb()) {
            int i = 0;
            for (Document doc : MongoDBProvider.getOpenReportCollection().find(new Document("status", "Open"))) {
                openReportsForm.addButton(new ElementButton(doc.getString("id")));
                i++;
            }
            if (i == 0) {
                openReportsForm.addButton(new ElementButton(config.getString("Buttons.NoReports").replace("&", "§")));
            }
        } else if (instance.isMysql()) {
            int i = 0;
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM opened WHERE STATUS = ?");
                preparedStatement.setString(1, "Open");
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    openReportsForm.addButton(new ElementButton(rs.getString("ID")));
                    i++;
                }
                if (i == 0) {
                    openReportsForm.addButton(new ElementButton(config.getString("Buttons.NoReports").replace("&", "§")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (instance.isYaml()) {
            Config rd = new Config(ReportSystem.getInstance().getDataFolder() + "/data/reportdata.yml", Config.YAML);
            int i = 0;
            for (String s : rd.getStringList("Data")) {
                openReportsForm.addButton(new ElementButton(s));
                i++;
            }
            if (i == 0) {
                openReportsForm.addButton(new ElementButton(config.getString("Buttons.NoReports").replace("&", "§")));
            }
        }
        player.showFormWindow(openReportsForm);
    }

    public static void sendMyReports(Player player) {
        Config config = ReportSystem.getInstance().getConfig();
        FormWindowSimple myReportsForm = new FormWindowSimple(config.getString("Title.MyReports").replace("&", "§"), config.getString("Text.MyReports").replace("&", "§"));
        if (instance.isMongodb()) {
            int i = 0;
            for (Document doc : MongoDBProvider.getOpenReportCollection().find(new Document("manager", player.getName()))) {
                myReportsForm.addButton(new ElementButton(doc.getString("id")));
                i++;
            }
            if (i == 0) {
                myReportsForm.addButton(new ElementButton(config.getString("Buttons.NoReports").replace("&", "§")));
            }
            player.showFormWindow(myReportsForm);
        } else if (instance.isMysql()) {
            int i = 0;
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM opened WHERE MANAGER = ?");
                preparedStatement.setString(1, player.getName());
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    myReportsForm.addButton(new ElementButton(rs.getString("ID")));
                    i++;
                }
                if (i == 0) {
                    myReportsForm.addButton(new ElementButton(config.getString("Buttons.NoReports").replace("&", "§")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            player.showFormWindow(myReportsForm);
        } else if (instance.isYaml()) {
            Config rd = new Config(ReportSystem.getInstance().getDataFolder() + "/data/reportdata.yml", Config.YAML);
            int i = 0;
            for (String d : rd.getStringList(player.getName())) {
                myReportsForm.addButton(new ElementButton(d));
                i++;
            }
            if (i == 0) {
                myReportsForm.addButton(new ElementButton(config.getString("Buttons.NoReports").replace("&", "§")));
            }
            player.showFormWindow(myReportsForm);
        }
    }

    public static void sendReportForm(Player player, String id) {
        Config config = ReportSystem.getInstance().getConfig();
        ReportUtil reportUtil = ReportManager.getOpenReport(id);
        FormWindowSimple seeReportForm = new FormWindowSimple(id, config.getString("Text.SeeReport").replace("&", "§").replace("%target%", reportUtil.getTarget()).replace("%reason%", reportUtil.getReason()).replace("%creator%", reportUtil.getCreator()).replace("%id%", reportUtil.getId()).replace("%date%", reportUtil.getDate()).replace("%status%", reportUtil.getStatus()).replace("%manager%", reportUtil.getManager()));
        if (reportUtil.getStatus().equalsIgnoreCase("Open") && reportUtil.getManager().equalsIgnoreCase("none")) {
            seeReportForm.addButton(new ElementButton(config.getString("Buttons.SeeReport.TakeReport").replace("&", "§")));
        }
        if (reportUtil.getManager().equalsIgnoreCase(player.getName())) {
            seeReportForm.addButton(new ElementButton(config.getString("Buttons.SeeReport.CloseReport").replace("&", "§")));
            seeReportForm.addButton(new ElementButton(config.getString("Buttons.SeeReport.DeleteReport").replace("&", "§")));
        }
        seeReportForm.addButton(new ElementButton(config.getString("Buttons.Back").replace("&", "§")));
        player.showFormWindow(seeReportForm);

    }

    public static void sendClosedReportForm(Player player, String id) {
        Config config = ReportSystem.getInstance().getConfig();
        ReportUtil reportUtil = ReportManager.getClosedReport(id);
        FormWindowSimple seeReportForm = new FormWindowSimple(id, config.getString("Text.SeeReport").replace("&", "§").replace("%target%", reportUtil.getTarget()).replace("%reason%", reportUtil.getReason()).replace("%creator%", reportUtil.getCreator()).replace("%id%", reportUtil.getId()).replace("%date%", reportUtil.getDate()).replace("%status%", reportUtil.getStatus()).replace("%manager%", reportUtil.getManager()));
        seeReportForm.addButton(new ElementButton(config.getString("Buttons.Panel.Close").replace("&", "§")));
        player.showFormWindow(seeReportForm);

    }

    public static void sendOpenedReportsAgainst(Player player, String search) {
        Config config = ReportSystem.getInstance().getConfig();
        FormWindowSimple myReportsForm = new FormWindowSimple(config.getString("Title.OpenedReportsAgainst").replace("&", "§"), config.getString("Text.Info").replace("&", "§"));
        if (instance.isMongodb()) {
            int i = 0;
            for (Document doc : MongoDBProvider.getOpenReportCollection().find(new Document("target", search))) {
                myReportsForm.addButton(new ElementButton(doc.getString("id")));
                i++;
            }
            if (i == 0) {
                myReportsForm.addButton(new ElementButton(config.getString("Buttons.NoReports").replace("&", "§")));
            }
            player.showFormWindow(myReportsForm);
        } else if (instance.isMysql()) {
            int i = 0;
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM opened WHERE TARGET = ?");
                preparedStatement.setString(1, search);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    myReportsForm.addButton(new ElementButton(rs.getString("ID")));
                    i++;
                }
                if (i == 0) {
                    myReportsForm.addButton(new ElementButton(config.getString("Buttons.NoReports").replace("&", "§")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        player.showFormWindow(myReportsForm);
    }

    public static void sendClosedReportsAgainst(Player player, String search) {
        Config config = ReportSystem.getInstance().getConfig();
        FormWindowSimple myReportsForm = new FormWindowSimple(config.getString("Title.ClosedReportsAgainst").replace("&", "§"), config.getString("Text.Info").replace("&", "§"));
        if (instance.isMongodb()) {
            int i = 0;
            for (Document doc : MongoDBProvider.getCloseReportCollection().find(new Document("target", search))) {
                myReportsForm.addButton(new ElementButton(doc.getString("id")));
                i++;
            }
            if (i == 0) {
                myReportsForm.addButton(new ElementButton(config.getString("Buttons.NoReports").replace("&", "§")));
            }
            player.showFormWindow(myReportsForm);
        } else if (instance.isMysql()) {
            int i = 0;
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM closed WHERE TARGET = ?");
                preparedStatement.setString(1, search);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    myReportsForm.addButton(new ElementButton(rs.getString("ID")));
                    i++;
                }
                if (i == 0) {
                    myReportsForm.addButton(new ElementButton(config.getString("Buttons.NoReports").replace("&", "§")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        player.showFormWindow(myReportsForm);
    }

    public static void sendOpenedReportsBy(Player player, String search) {
        Config config = ReportSystem.getInstance().getConfig();
        FormWindowSimple myReportsForm = new FormWindowSimple(config.getString("Title.OpenedReportsBy").replace("&", "§"), config.getString("Text.Info").replace("&", "§"));
        if (instance.isMongodb()) {
            int i = 0;
            for (Document doc : MongoDBProvider.getOpenReportCollection().find(new Document("creator", search))) {
                myReportsForm.addButton(new ElementButton(doc.getString("id")));
                i++;
            }
            if (i == 0) {
                myReportsForm.addButton(new ElementButton(config.getString("Buttons.NoReports").replace("&", "§")));
            }
            player.showFormWindow(myReportsForm);
        } else if (instance.isMysql()) {
            int i = 0;
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM opened WHERE CREATOR = ?");
                preparedStatement.setString(1, search);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    myReportsForm.addButton(new ElementButton(rs.getString("ID")));
                    i++;
                }
                if (i == 0) {
                    myReportsForm.addButton(new ElementButton(config.getString("Buttons.NoReports").replace("&", "§")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        player.showFormWindow(myReportsForm);
    }

    public static void sendClosedReportsBy(Player player, String search) {
        Config config = ReportSystem.getInstance().getConfig();
        FormWindowSimple myReportsForm = new FormWindowSimple(config.getString("Title.ClosedReportsBy").replace("&", "§"), config.getString("Text.Info").replace("&", "§"));
        if (instance.isMongodb()) {
            int i = 0;
            for (Document doc : MongoDBProvider.getCloseReportCollection().find(new Document("creator", search))) {
                myReportsForm.addButton(new ElementButton(doc.getString("id")));
                i++;
            }
            if (i == 0) {
                myReportsForm.addButton(new ElementButton(config.getString("Buttons.NoReports").replace("&", "§")));
            }
            player.showFormWindow(myReportsForm);
        } else if (instance.isMysql()) {
            int i = 0;
            try {
                PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM closed WHERE CREATOR = ?");
                preparedStatement.setString(1, search);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    myReportsForm.addButton(new ElementButton(rs.getString("ID")));
                    i++;
                }
                if (i == 0) {
                    myReportsForm.addButton(new ElementButton(config.getString("Buttons.NoReports").replace("&", "§")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        player.showFormWindow(myReportsForm);
    }

    public static void sendSubOptions(Player player) {
        Config config = ReportSystem.getInstance().getConfig();
        FormWindowSimple seeReportForm = new FormWindowSimple(config.getString("Title.SubOptions"), config.getString("Text.SubOptions"));
        seeReportForm.addButton(new ElementButton(config.getString("Buttons.SearchPlayer.OpenedReportsAgainst").replace("&", "§")));
        seeReportForm.addButton(new ElementButton(config.getString("Buttons.SearchPlayer.ClosedReportsAgainst").replace("&", "§")));
        seeReportForm.addButton(new ElementButton(config.getString("Buttons.SearchPlayer.OpenedReportsBy").replace("&", "§")));
        seeReportForm.addButton(new ElementButton(config.getString("Buttons.SearchPlayer.ClosedReportsBy").replace("&", "§")));
        seeReportForm.addButton(new ElementButton(config.getString("Buttons.Back").replace("&", "§")));
        player.showFormWindow(seeReportForm);
    }

    public static void sendSearchReport(Player player) {
        Config config = ReportSystem.getInstance().getConfig();
        FormWindowCustom searchReportForm = new FormWindowCustom(config.getString("Title.SearchReport").replace("&", "§"));
        searchReportForm.addElement(new ElementInput(config.getString("Text.SearchReport").replace("&", "§"), "Report ID", ""));
        player.showFormWindow(searchReportForm, 1);
    }

    public static void sendSearchPlayer(Player player) {
        Config config = ReportSystem.getInstance().getConfig();
        FormWindowCustom searchPlayerForm = new FormWindowCustom(config.getString("Title.SearchPlayer").replace("&", "§"));
        searchPlayerForm.addElement(new ElementInput(config.getString("Text.SearchPlayer").replace("&", "§"), "Name", ""));
        player.showFormWindow(searchPlayerForm, 2);
    }

    public static void sendReportConfirmation(Player player, String id, String type) {
        Config config = ReportSystem.getInstance().getConfig();
        if (type.equalsIgnoreCase("delete")) {
            FormWindowModal formWindowModal = new FormWindowModal(id, config.getString("Text.DeleteReport").replace("&", "§"), config.getString("Buttons.ConfirmReport.Delete").replace("&", "§"), config.getString("Buttons.Back").replace("&", "§"));
            player.showFormWindow(formWindowModal);
        } else if (type.equalsIgnoreCase("close")) {
            FormWindowModal formWindowModal = new FormWindowModal(id, config.getString("Text.CloseReport").replace("&", "§"), config.getString("Buttons.ConfirmReport.Close").replace("&", "§"), config.getString("Buttons.Back").replace("&", "§"));
            player.showFormWindow(formWindowModal);
        }
    }
}
