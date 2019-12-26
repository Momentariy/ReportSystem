//This file was created by Mundschutziii. 
//You can change the code here, but do not sell this file as your own.

package net.llamadevelopment.reportsystem.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.Config;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.managers.ReportManager;
import net.llamadevelopment.reportsystem.utils.FormUiUtil;
import org.bson.Document;

import java.util.List;

public class FormListener implements Listener {

    @EventHandler
    public void on(PlayerFormRespondedEvent event) {
        Config config = ReportSystem.getInstance().getConfig();
        Config or = new Config(ReportSystem.getInstance().getDataFolder() + "/data/openreports.yml", Config.YAML);
        if (event.getWindow() instanceof FormWindowSimple) {
            FormWindowSimple form = (FormWindowSimple) event.getWindow();
            Player player = event.getPlayer();
            if (form.getTitle().equalsIgnoreCase(config.getString("Title.Panel").replace("&", "§"))) {
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButton().getText();
                if (response.equalsIgnoreCase(config.getString("Buttons.Panel.Reports").replace("&", "§"))) {
                    FormUiUtil.sendOpenReports(player);
                    form.setResponse("");
                } else if (response.equalsIgnoreCase(config.getString("Buttons.Panel.MyReports").replace("&", "§"))) {
                    FormUiUtil.sendMyReports(player);
                    form.setResponse("");
                } else if (response.equalsIgnoreCase(config.getString("Buttons.Panel.SearchReport").replace("&", "§"))) {
                    FormUiUtil.sendSearchReport(player);
                    form.setResponse("");
                }
            } else if (form.getTitle().equalsIgnoreCase(config.getString("Title.OpenReports").replace("&", "§"))) {
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButton().getText();
                if (response.equalsIgnoreCase(config.getString("Buttons.OpenedReports.NoReports").replace("&", "§"))) {
                    FormUiUtil.sendReportPanel(player);
                    return;
                }
                FormUiUtil.sendReportForm(player, response);
                form.setResponse("");
            } else if (form.getTitle().equalsIgnoreCase(config.getString("Title.MyReports").replace("&", "§"))) {
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButton().getText();
                if (response.equalsIgnoreCase(config.getString("Buttons.MyReports.NoReports").replace("&", "§"))) {
                    FormUiUtil.sendReportPanel(player);
                    return;
                }
                FormUiUtil.sendReportForm(player, response);
                form.setResponse("");
            } else if (or.exists("Report." + form.getTitle())) {
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButton().getText();
                if (response.equalsIgnoreCase(config.getString("Buttons.SeeReport.TakeReport").replace("&", "§"))) {
                    ReportManager.updateStatus(form.getTitle(), "In progress");
                    ReportManager.updateManager(form.getTitle(), player.getName());
                    player.sendMessage(config.getString("Prefix").replace("&", "§") + config.getString("TakeReport").replace("&", "§"));
                    form.setResponse("");
                } else if (response.equalsIgnoreCase(config.getString("Buttons.Back").replace("&", "§"))) {
                    FormUiUtil.sendReportPanel(player);
                    form.setResponse("");
                } else if (response.equalsIgnoreCase(config.getString("Buttons.SeeReport.CloseReport").replace("&", "§"))) {
                    FormUiUtil.sendReportConfirmation(player, form.getTitle(), "close");
                } else if (response.equalsIgnoreCase(config.getString("Buttons.SeeReport.DeleteReport").replace("&", "§"))) {
                    FormUiUtil.sendReportConfirmation(player, form.getTitle(), "delete");
                }
            } else {
                if (config.getBoolean("MongoDB")) {
                    Document document = ReportSystem.getInstance().getOpenReportCollection().find(new Document("id", form.getTitle())).first();
                    if (document != null) {
                        if (form.getResponse() == null) return;
                        String response = form.getResponse().getClickedButton().getText();
                        if (response.equalsIgnoreCase(config.getString("Buttons.SeeReport.TakeReport").replace("&", "§"))) {
                            ReportManager.updateStatus(form.getTitle(), "In progress");
                            ReportManager.updateManager(form.getTitle(), player.getName());
                            player.sendMessage(config.getString("Prefix").replace("&", "§") + config.getString("TakeReport").replace("&", "§"));
                            form.setResponse("");
                        } else if (response.equalsIgnoreCase(config.getString("Buttons.Back").replace("&", "§"))) {
                            FormUiUtil.sendReportPanel(player);
                            form.setResponse("");
                        } else if (response.equalsIgnoreCase(config.getString("Buttons.SeeReport.CloseReport").replace("&", "§"))) {
                            FormUiUtil.sendReportConfirmation(player, form.getTitle(), "close");
                        } else if (response.equalsIgnoreCase(config.getString("Buttons.SeeReport.DeleteReport").replace("&", "§"))) {
                            FormUiUtil.sendReportConfirmation(player, form.getTitle(), "delete");
                        }
                    }
                }
            }
        } else if (event.getWindow() instanceof FormWindowCustom) {
            FormWindowCustom form = (FormWindowCustom) event.getWindow();
            Player player = event.getPlayer();
            if (form.getTitle().equalsIgnoreCase(config.getString("Title.SearchReport").replace("&", "§"))) {
                if (config.getBoolean("MongoDB")) {
                    FormResponseCustom response = form.getResponse();
                    if (response != null) {
                        String input = response.getInputResponse(0).toString();
                        Document document = ReportSystem.getInstance().getOpenReportCollection().find(new Document("id", input)).first();
                        if (document != null) {
                            FormUiUtil.sendReportForm(player, input);
                        } else {
                            player.sendMessage(config.getString("Prefix").replace("&", "§") + config.getString("NotExists").replace("&", "§"));
                        }
                    }
                } else {
                    FormResponseCustom response = form.getResponse();
                    if (response != null) {
                        String input = response.getInputResponse(0).toString();
                        if (or.exists("Report." + input)) {
                            FormUiUtil.sendReportForm(player, input);
                        } else {
                            player.sendMessage(config.getString("Prefix").replace("&", "§") + config.getString("NotExists").replace("&", "§"));
                        }
                    }
                }
            }
        } else if (event.getWindow() instanceof FormWindowModal) {
            FormWindowModal form = (FormWindowModal) event.getWindow();
            Player player = event.getPlayer();
            if (or.exists("Report." + form.getTitle())) {
                if (form.getResponse().getClickedButtonText().equalsIgnoreCase(config.getString("Buttons.ConfirmReport.Delete").replace("&", "§"))) {
                    ReportManager.deleteReport(form.getTitle());
                    player.sendMessage(config.getString("Prefix").replace("&", "§") + config.getString("DeleteReport").replace("&", "§"));
                    form.setResponse("");
                } else if (form.getResponse().getClickedButtonText().equalsIgnoreCase(config.getString("Buttons.ConfirmReport.Close").replace("&", "§"))) {
                    ReportManager.closeReport(form.getTitle());
                    player.sendMessage(config.getString("Prefix").replace("&", "§") + config.getString("CompleteReport").replace("&", "§"));
                    form.setResponse("");
                } else if (form.getResponse().getClickedButtonText().equalsIgnoreCase(config.getString("Buttons.Back").replace("&", "§"))) {
                    FormUiUtil.sendReportForm(player, form.getTitle());
                    form.setResponse("");
                }
            } else {
                Document document = ReportSystem.getInstance().getOpenReportCollection().find(new Document("id", form.getTitle())).first();
                if (document != null) {
                    if (form.getResponse().getClickedButtonText().equalsIgnoreCase(config.getString("Buttons.ConfirmReport.Delete").replace("&", "§"))) {
                        ReportManager.deleteReport(form.getTitle());
                        player.sendMessage(config.getString("Prefix").replace("&", "§") + config.getString("DeleteReport").replace("&", "§"));
                        form.setResponse("");
                    } else if (form.getResponse().getClickedButtonText().equalsIgnoreCase(config.getString("Buttons.ConfirmReport.Close").replace("&", "§"))) {
                        ReportManager.closeReport(form.getTitle());
                        player.sendMessage(config.getString("Prefix").replace("&", "§") + config.getString("CompleteReport").replace("&", "§"));
                        form.setResponse("");
                    } else if (form.getResponse().getClickedButtonText().equalsIgnoreCase(config.getString("Buttons.Back").replace("&", "§"))) {
                        FormUiUtil.sendReportForm(player, form.getTitle());
                        form.setResponse("");
                    }
                }
            }
        }
    }
}
