//This file was created by Mundschutziii. 
//You can change the code here, but do not sell this file as your own.

package net.llamadevelopment.reportsystem.utils;

import cn.nukkit.Player;
import cn.nukkit.form.element.*;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.Config;
import net.llamadevelopment.reportsystem.ReportSystem;
import org.bson.Document;

public class FormUiUtil {

    public static void sendReportPanel(Player player) {
        Config config = ReportSystem.getInstance().getConfig();
        FormWindowSimple reportPanelForm = new FormWindowSimple(config.getString("Title.Panel"), config.getString("Text.Panel"));
        reportPanelForm.addButton(new ElementButton(config.getString("Buttons.Panel.Reports").replace("&", "§")));
        reportPanelForm.addButton(new ElementButton(config.getString("Buttons.Panel.MyReports").replace("&", "§")));
        reportPanelForm.addButton(new ElementButton(config.getString("Buttons.Panel.SearchReport").replace("&", "§")));
        reportPanelForm.addButton(new ElementButton(config.getString("Buttons.Panel.Close").replace("&", "§")));
        player.showFormWindow(reportPanelForm);
    }

    public static void sendOpenReports(Player player) {
        Config config = ReportSystem.getInstance().getConfig();
        FormWindowSimple openReportsForm = new FormWindowSimple(config.getString("Title.OpenReports").replace("&", "§"), config.getString("Text.OpenReports").replace("&", "§"));
        if (config.getBoolean("MongoDB")) {
            int i = 0;
            for (Document doc : ReportSystem.getInstance().getOpenReportCollection().find(new Document("status", "Open"))) {
                openReportsForm.addButton(new ElementButton(doc.getString("id")));
                i++;
            }
            if (i == 0) {
                openReportsForm.addButton(new ElementButton(config.getString("Buttons.OpenedReports.NoReports").replace("&", "§")));
            }
        } else {
            Config rd = new Config(ReportSystem.getInstance().getDataFolder() + "/data/reportdata.yml", Config.YAML);
            int i = 0;
            for (String s : rd.getStringList("Data")) {
                openReportsForm.addButton(new ElementButton(s));
                i++;
            }
            if (i == 0) {
                openReportsForm.addButton(new ElementButton(config.getString("Buttons.OpenedReports.NoReports").replace("&", "§")));
            }
        }
        player.showFormWindow(openReportsForm);
    }

    public static void sendMyReports(Player player) {
        Config config = ReportSystem.getInstance().getConfig();
        FormWindowSimple myReportsForm = new FormWindowSimple(config.getString("Title.MyReports").replace("&", "§"), config.getString("Text.MyReports").replace("&", "§"));
        if (config.getBoolean("MongoDB")) {
            int i = 0;
            for (Document doc : ReportSystem.getInstance().getOpenReportCollection().find(new Document("manager", player.getName()))) {
                myReportsForm.addButton(new ElementButton(doc.getString("id")));
                i++;
            }
            if (i == 0) {
                myReportsForm.addButton(new ElementButton(config.getString("Buttons.MyReports.NoReports").replace("&", "§")));
            }
            player.showFormWindow(myReportsForm);
        } else {
            Config rd = new Config(ReportSystem.getInstance().getDataFolder() + "/data/reportdata.yml", Config.YAML);
            int i = 0;
            for (String d : rd.getStringList(player.getName())) {
                myReportsForm.addButton(new ElementButton(d));
                i++;
            }
            if (i == 0) {
                myReportsForm.addButton(new ElementButton(config.getString("Buttons.MyReports.NoReports").replace("&", "§")));
            }
            player.showFormWindow(myReportsForm);
        }
    }

    public static void sendReportForm(Player player, String id) {
        Config config = ReportSystem.getInstance().getConfig();
        ReportUtil reportUtil = ReportSystem.getInstance().reportManager.getOpenReport(id);
        if (config.getBoolean("MongoDB")) {
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
        } else {
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
    }

    public static void sendSearchReport(Player player) {
        Config config = ReportSystem.getInstance().getConfig();
        FormWindowCustom searchReportForm = new FormWindowCustom(config.getString("Title.SearchReport").replace("&", "§"));
        searchReportForm.addElement(new ElementInput(config.getString("Text.SearchReport").replace("&", "§"), "Report ID", ""));
        player.showFormWindow(searchReportForm, 1);
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
