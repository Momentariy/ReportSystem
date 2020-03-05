package net.llamadevelopment.reportsystem.listener;

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
import net.llamadevelopment.reportsystem.components.managers.ReportManager;
import net.llamadevelopment.reportsystem.components.managers.database.MongoDBProvider;
import net.llamadevelopment.reportsystem.components.managers.database.MySqlProvider;
import net.llamadevelopment.reportsystem.components.messaging.Messages;
import net.llamadevelopment.reportsystem.components.utils.FormUiUtil;
import org.bson.Document;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FormListener implements Listener {

    private static ReportSystem instance = ReportSystem.getInstance();

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
                } else if (response.equalsIgnoreCase(config.getString("Buttons.Panel.SearchPlayer").replace("&", "§"))) {
                    if (instance.isMysql() || instance.isMongodb()) {
                        FormUiUtil.sendSearchPlayer(player);
                        form.setResponse("");
                    }
                    form.setResponse("");
                }
            } else if (form.getTitle().equalsIgnoreCase(config.getString("Title.OpenReports").replace("&", "§"))) {
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButton().getText();
                if (response.equalsIgnoreCase(config.getString("Buttons.NoReports").replace("&", "§"))) {
                    FormUiUtil.sendReportPanel(player);
                    return;
                }
                FormUiUtil.sendReportForm(player, response);
                form.setResponse("");
            } else if (form.getTitle().equalsIgnoreCase(config.getString("Title.MyReports").replace("&", "§"))) {
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButton().getText();
                if (response.equalsIgnoreCase(config.getString("Buttons.NoReports").replace("&", "§"))) {
                    FormUiUtil.sendReportPanel(player);
                    return;
                }
                FormUiUtil.sendReportForm(player, response);
                form.setResponse("");
            } else if (form.getTitle().equalsIgnoreCase(config.getString("Title.SubOptions").replace("&", "§"))) {
                String search = FormUiUtil.searchCache.get(player);
                String response = form.getResponse().getClickedButton().getText();
                if (search != null) {
                    if (response.equalsIgnoreCase(config.getString("Buttons.SearchPlayer.OpenedReportsAgainst").replace("&", "§"))) {
                        FormUiUtil.sendOpenedReportsAgainst(player, search);
                        form.setResponse("");
                    } else if (response.equalsIgnoreCase(config.getString("Buttons.SearchPlayer.ClosedReportsAgainst").replace("&", "§"))) {
                        FormUiUtil.sendClosedReportsAgainst(player, search);
                        form.setResponse("");
                    } else if (response.equalsIgnoreCase(config.getString("Buttons.SearchPlayer.OpenedReportsBy").replace("&", "§"))) {
                        FormUiUtil.sendOpenedReportsBy(player, search);
                        form.setResponse("");
                    } else if (response.equalsIgnoreCase(config.getString("Buttons.SearchPlayer.ClosedReportsBy").replace("&", "§"))) {
                        FormUiUtil.sendClosedReportsBy(player, search);
                        form.setResponse("");
                    }
                }
            } else if (form.getTitle().equalsIgnoreCase(config.getString("Title.OpenedReportsAgainst").replace("&", "§"))) {
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButton().getText();
                if (response.equalsIgnoreCase(config.getString("Buttons.NoReports").replace("&", "§"))) {
                    FormUiUtil.sendReportPanel(player);
                    return;
                }
                FormUiUtil.sendReportForm(player, response);
                form.setResponse("");
            } else if (form.getTitle().equalsIgnoreCase(config.getString("Title.ClosedReportsAgainst").replace("&", "§"))) {
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButton().getText();
                if (response.equalsIgnoreCase(config.getString("Buttons.NoReports").replace("&", "§"))) {
                    FormUiUtil.sendReportPanel(player);
                    return;
                }
                FormUiUtil.sendClosedReportForm(player, response);
                form.setResponse("");
            } else if (form.getTitle().equalsIgnoreCase(config.getString("Title.OpenedReportsBy").replace("&", "§"))) {
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButton().getText();
                if (response.equalsIgnoreCase(config.getString("Buttons.NoReports").replace("&", "§"))) {
                    FormUiUtil.sendReportPanel(player);
                    return;
                }
                FormUiUtil.sendReportForm(player, response);
                form.setResponse("");
            } else if (form.getTitle().equalsIgnoreCase(config.getString("Title.ClosedReportsBy").replace("&", "§"))) {
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButton().getText();
                if (response.equalsIgnoreCase(config.getString("Buttons.NoReports").replace("&", "§"))) {
                    FormUiUtil.sendReportPanel(player);
                    return;
                }
                FormUiUtil.sendClosedReportForm(player, response);
                form.setResponse("");
            } else if (or.exists("Report." + form.getTitle())) {
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButton().getText();
                if (response.equalsIgnoreCase(config.getString("Buttons.SeeReport.TakeReport").replace("&", "§"))) {
                    ReportManager.updateStatus(form.getTitle(), "In progress");
                    ReportManager.updateManager(form.getTitle(), player.getName());
                    player.sendMessage(Messages.getAndReplace("Messages.TakeReport"));
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
                if (instance.isMongodb()) {
                    Document document = MongoDBProvider.getOpenReportCollection().find(new Document("id", form.getTitle())).first();
                    if (document != null) {
                        if (form.getResponse() == null) return;
                        String response = form.getResponse().getClickedButton().getText();
                        if (response.equalsIgnoreCase(config.getString("Buttons.SeeReport.TakeReport").replace("&", "§"))) {
                            ReportManager.updateStatus(form.getTitle(), "In progress");
                            ReportManager.updateManager(form.getTitle(), player.getName());
                            player.sendMessage(Messages.getAndReplace("Messages.TakeReport"));
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
                } else if (instance.isMysql()) {
                    try {
                        PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM opened WHERE ID = ?");
                        preparedStatement.setString(1, form.getTitle());
                        ResultSet rs = preparedStatement.executeQuery();
                        if (rs.next()) {
                            if (form.getResponse() == null) return;
                            String response = form.getResponse().getClickedButton().getText();
                            if (response.equalsIgnoreCase(config.getString("Buttons.SeeReport.TakeReport").replace("&", "§"))) {
                                ReportManager.updateStatus(form.getTitle(), "In progress");
                                ReportManager.updateManager(form.getTitle(), player.getName());
                                player.sendMessage(Messages.getAndReplace("Messages.TakeReport"));
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (event.getWindow() instanceof FormWindowCustom) {
            FormWindowCustom form = (FormWindowCustom) event.getWindow();
            Player player = event.getPlayer();
            if (form.getTitle().equalsIgnoreCase(config.getString("Title.SearchReport").replace("&", "§"))) {
                if (instance.isMongodb()) {
                    FormResponseCustom response = form.getResponse();
                    if (response != null) {
                        String input = response.getInputResponse(0).toString();
                        Document document = MongoDBProvider.getOpenReportCollection().find(new Document("id", input)).first();
                        if (document != null) {
                            FormUiUtil.sendReportForm(player, input);
                        } else {
                            player.sendMessage(Messages.getAndReplace("Messages.NotExists"));
                        }
                    }
                } else if (instance.isMysql()) {
                    FormResponseCustom response = form.getResponse();
                    if (response != null) {
                        String input = response.getInputResponse(0).toString();
                        try {
                            PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM opened WHERE ID = ?");
                            preparedStatement.setString(1, input);
                            ResultSet rs = preparedStatement.executeQuery();
                            if (rs.next()) {
                                FormUiUtil.sendReportForm(player, input);
                            } else {
                                player.sendMessage(Messages.getAndReplace("Messages.NotExists"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } else if (instance.isYaml()) {
                    FormResponseCustom response = form.getResponse();
                    if (response != null) {
                        String input = response.getInputResponse(0).toString();
                        if (or.exists("Report." + input)) {
                            FormUiUtil.sendReportForm(player, input);
                        } else {
                            player.sendMessage(Messages.getAndReplace("Messages.NotExists"));
                        }
                    }
                }
            } else if (form.getTitle().equalsIgnoreCase(config.getString("Title.SearchPlayer").replace("&", "§"))) {
                FormResponseCustom response = form.getResponse();
                if (response != null) {
                    String input = response.getInputResponse(0).toString();
                    FormUiUtil.searchCache.remove(player);
                    FormUiUtil.searchCache.put(player, input);
                    FormUiUtil.sendSubOptions(player);
                }
            }
        } else if (event.getWindow() instanceof FormWindowModal) {
            FormWindowModal form = (FormWindowModal) event.getWindow();
            Player player = event.getPlayer();
            if (or.exists("Report." + form.getTitle())) {
                if (form.getResponse().getClickedButtonText().equalsIgnoreCase(config.getString("Buttons.ConfirmReport.Delete").replace("&", "§"))) {
                    ReportManager.deleteReport(form.getTitle());
                    player.sendMessage(Messages.getAndReplace("Messages.DeleteReport"));
                    form.setResponse("");
                } else if (form.getResponse().getClickedButtonText().equalsIgnoreCase(config.getString("Buttons.ConfirmReport.Close").replace("&", "§"))) {
                    ReportManager.closeReport(form.getTitle());
                    player.sendMessage(Messages.getAndReplace("Messages.CompleteReport"));
                    form.setResponse("");
                } else if (form.getResponse().getClickedButtonText().equalsIgnoreCase(config.getString("Buttons.Back").replace("&", "§"))) {
                    FormUiUtil.sendReportForm(player, form.getTitle());
                    form.setResponse("");
                }
            } else {
                if (instance.isMongodb()) {
                    Document document = MongoDBProvider.getOpenReportCollection().find(new Document("id", form.getTitle())).first();
                    if (document != null) {
                        if (form.getResponse().getClickedButtonText().equalsIgnoreCase(config.getString("Buttons.ConfirmReport.Delete").replace("&", "§"))) {
                            ReportManager.deleteReport(form.getTitle());
                            player.sendMessage(Messages.getAndReplace("Messages.DeleteReport"));
                            form.setResponse("");
                        } else if (form.getResponse().getClickedButtonText().equalsIgnoreCase(config.getString("Buttons.ConfirmReport.Close").replace("&", "§"))) {
                            ReportManager.closeReport(form.getTitle());
                            player.sendMessage(Messages.getAndReplace("Messages.CompleteReport"));
                            form.setResponse("");
                        } else if (form.getResponse().getClickedButtonText().equalsIgnoreCase(config.getString("Buttons.Back").replace("&", "§"))) {
                            FormUiUtil.sendReportForm(player, form.getTitle());
                            form.setResponse("");
                        }
                    }
                } else if (instance.isMysql()) {
                    try {
                        PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM opened WHERE ID = ?");
                        preparedStatement.setString(1, form.getTitle());
                        ResultSet rs = preparedStatement.executeQuery();
                        if (rs.next()) {
                            if (form.getResponse().getClickedButtonText().equalsIgnoreCase(config.getString("Buttons.ConfirmReport.Delete").replace("&", "§"))) {
                                ReportManager.deleteReport(form.getTitle());
                                player.sendMessage(Messages.getAndReplace("Messages.DeleteReport"));
                                form.setResponse("");
                            } else if (form.getResponse().getClickedButtonText().equalsIgnoreCase(config.getString("Buttons.ConfirmReport.Close").replace("&", "§"))) {
                                ReportManager.closeReport(form.getTitle());
                                player.sendMessage(Messages.getAndReplace("Messages.CompleteReport"));
                                form.setResponse("");
                            } else if (form.getResponse().getClickedButtonText().equalsIgnoreCase(config.getString("Buttons.Back").replace("&", "§"))) {
                                FormUiUtil.sendReportForm(player, form.getTitle());
                                form.setResponse("");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
