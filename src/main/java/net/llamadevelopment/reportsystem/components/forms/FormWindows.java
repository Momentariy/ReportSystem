package net.llamadevelopment.reportsystem.components.forms;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.level.Sound;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.data.Report;
import net.llamadevelopment.reportsystem.components.forms.custom.CustomForm;
import net.llamadevelopment.reportsystem.components.forms.simple.SimpleForm;
import net.llamadevelopment.reportsystem.components.language.Language;
import net.llamadevelopment.reportsystem.components.provider.Provider;

import java.util.Arrays;

public class FormWindows {

    private static final Provider api = ReportSystemAPI.getProvider();

    public static void sendReportManager(Player player) {
        SimpleForm form = new SimpleForm.Builder(Language.getAndReplaceNP("manager-title"), Language.getAndReplaceNP("manager-content"))
                .addButton(new ElementButton(Language.getAndReplaceNP("manager-pendingreports", api.getReports(Report.ReportStatus.PENDING).size()),
                        new ElementButtonImageData("url", Language.getAndReplaceNP("manager-pendingreports-image"))), (FormWindows::sendPendingReports))
                .addButton(new ElementButton(Language.getAndReplaceNP("manager-myopenreports", api.getReports(Report.ReportStatus.PROGRESS, Report.ReportSearch.MEMBER, player.getName()).size()),
                        new ElementButtonImageData("url", Language.getAndReplaceNP("manager-myopenreports-image"))), (FormWindows::sendMyProgressReports))
                .addButton(new ElementButton(Language.getAndReplaceNP("manager-searchplayers"),
                        new ElementButtonImageData("url", Language.getAndReplaceNP("manager-searchplayers-image"))), (FormWindows::sendSearchPlayer))
                .addButton(new ElementButton(Language.getAndReplaceNP("manager-searchreport"),
                        new ElementButtonImageData("url", Language.getAndReplaceNP("manager-searchreport-image"))), (FormWindows::sendSearchReport))
                .onClose(executor -> {})
                .build();
        form.send(player);
    }

    public static void sendPendingReports(Player player) {
        SimpleForm.Builder pendingForm = new SimpleForm.Builder(Language.getAndReplaceNP("pending-title"), Language.getAndReplaceNP("pending-content"));
        api.getReports(Report.ReportStatus.PENDING).forEach(report -> pendingForm.addButton(new ElementButton(Language.getAndReplaceNP("pending-button", report.getTarget(), report.getPlayer(), report.getReason())), executor -> sendPendingReportMenu(executor, report.getId())));
        pendingForm.addButton(new ElementButton(Language.getAndReplaceNP("back-button")), FormWindows::sendReportManager);
        SimpleForm form = pendingForm
                .onClose(executor -> {})
                .build();
        form.send(player);
    }

    public static void sendPendingReportMenu(Player player, String id) {
        Report report = api.getReport(Report.ReportStatus.PENDING, id);
        if (report != null) {
            SimpleForm.Builder pendingReportForm = new SimpleForm.Builder(Language.getAndReplaceNP("pendingreport-title"),
                    Language.getAndReplaceNP("pendingreport-content", report.getPlayer(), report.getTarget(), report.getReason(), report.getStatus(), report.getMember(), report.getId(), report.getDate()));
            if (report.getMember().equals("Unknown")) {
                pendingReportForm.addButton(new ElementButton(Language.getAndReplaceNP("pendingreport-take"), new ElementButtonImageData("url", Language.getAndReplaceNP("pendingreport-take-image"))), executor -> {
                    api.updateStatus(id, "Progress");
                    api.updateStaffmember(id, executor.getName());
                    executor.sendMessage(Language.getAndReplace("report-took", report.getId()));
                    ReportSystemAPI.playSound(player, Sound.RANDOM_LEVELUP);
                });
            }
            pendingReportForm.addButton(new ElementButton(Language.getAndReplaceNP("back-button")), FormWindows::sendPendingReports);
            SimpleForm form = pendingReportForm
                    .onClose(executor -> {})
                    .build();
            form.send(player);
        } else {
            player.sendMessage(Language.getAndReplace("report-not-found"));
            ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
        }
    }

    public static void sendMyProgressReports(Player player) {
        SimpleForm.Builder pendingForm = new SimpleForm.Builder(Language.getAndReplaceNP("progress-title"), Language.getAndReplaceNP("progress-content"));
        api.getReports(Report.ReportStatus.PROGRESS, Report.ReportSearch.MEMBER, player.getName()).forEach(report -> pendingForm.addButton(new ElementButton(Language.getAndReplaceNP("progress-button", report.getTarget(), report.getPlayer(), report.getReason())), executor -> sendProgressReportMenu(executor, report.getId())));
        pendingForm.addButton(new ElementButton(Language.getAndReplaceNP("back-button")), FormWindows::sendReportManager);
        SimpleForm form = pendingForm
                .onClose(executor -> {})
                .build();
        form.send(player);
    }

    public static void sendProgressReportMenu(Player player, String id) {
        Report report = api.getReport(Report.ReportStatus.PROGRESS, id);
        if (report != null) {
            SimpleForm.Builder pendingReportForm = new SimpleForm.Builder(Language.getAndReplaceNP("progressreport-title"),
                    Language.getAndReplaceNP("progressreport-content", report.getPlayer(), report.getTarget(), report.getReason(), report.getStatus(), report.getMember(), report.getId(), report.getDate()));
            if (report.getMember().equals(player.getName()) || player.isOp()) {
                pendingReportForm.addButton(new ElementButton(Language.getAndReplaceNP("progressreport-close"), new ElementButtonImageData("url", Language.getAndReplaceNP("progressreport-close-image"))), executor -> {
                    api.closeReport(id);
                    executor.sendMessage(Language.getAndReplace("report-closed", report.getId()));
                    ReportSystemAPI.playSound(player, Sound.RANDOM_LEVELUP);
                });
                pendingReportForm.addButton(new ElementButton(Language.getAndReplaceNP("progressreport-delete"), new ElementButtonImageData("url", Language.getAndReplaceNP("progressreport-delete-image"))), executor -> {
                    api.deleteReport(id);
                    executor.sendMessage(Language.getAndReplace("report-deleted", report.getId()));
                    ReportSystemAPI.playSound(player, Sound.RANDOM_LEVELUP);
                });
            }
            pendingReportForm.addButton(new ElementButton(Language.getAndReplaceNP("back-button")), FormWindows::sendMyProgressReports);
            SimpleForm form = pendingReportForm
                    .onClose(executor -> {})
                    .build();
            form.send(player);
        } else {
            player.sendMessage(Language.getAndReplace("report-not-found"));
            ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
        }
    }

    public static void sendSearchPlayer(Player player) {
        CustomForm form = new CustomForm.Builder(Language.getAndReplaceNP("searchplayer-title"))
                .addElement(new ElementDropdown(Language.getAndReplaceNP("searchplayer-selectsearch"), Arrays.asList("Player", "Target", "Member")))
                .addElement(new ElementDropdown(Language.getAndReplaceNP("searchplayer-selectstatus"), Arrays.asList("Pending", "Progress", "Closed")))
                .addElement(new ElementInput(Language.getAndReplaceNP("searchplayer-player"), "Player"))
                .onSubmit((executor, response) -> {
                    if (!response.getInputResponse(2).isEmpty()) {
                        sendSearchPlayerResult(executor, Report.ReportSearch.valueOf(response.getDropdownResponse(0).getElementContent().toUpperCase()), Report.ReportStatus.valueOf(response.getDropdownResponse(1).getElementContent().toUpperCase()), response.getInputResponse(2));
                    } else {
                        executor.sendMessage(Language.getAndReplace("invalid-input"));
                        ReportSystemAPI.playSound(executor, Sound.NOTE_BASS);
                    }
                })
                .build();
        form.send(player);
    }

    public static void sendSearchPlayerResult(Player player, Report.ReportSearch search, Report.ReportStatus status, String value) {
        if (api.getReports(status, search, value).size() == 0) {
            player.sendMessage(Language.getAndReplace("no-reports-found"));
            ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
            return;
        }
        SimpleForm.Builder resultForm = new SimpleForm.Builder(Language.getAndReplaceNP("searchplayerresult-title"), Language.getAndReplaceNP("searchplayerresult-content"));
        api.getReports(status, search, value).forEach(report -> resultForm.addButton(new ElementButton(Language.getAndReplaceNP("searchplayerresult-button", report.getTarget(), report.getPlayer(), report.getReason())), executor -> sendSearchPlayerReport(executor, search, status, value, report.getId())));
        resultForm.addButton(new ElementButton(Language.getAndReplaceNP("back-button")), FormWindows::sendReportManager);
        SimpleForm form = resultForm
                .onClose(executor -> {})
                .build();
        form.send(player);
    }

    public static void sendSearchPlayerReport(Player player, Report.ReportSearch search, Report.ReportStatus status, String value, String id) {
        Report report = api.getReport(status, id);
        if (report != null) {
            SimpleForm.Builder pendingReportForm = new SimpleForm.Builder(Language.getAndReplaceNP("searchplayerreport-title"),
                    Language.getAndReplaceNP("searchplayerreport-content", report.getPlayer(), report.getTarget(), report.getReason(), report.getStatus(), report.getMember(), report.getId(), report.getDate()));
            pendingReportForm.addButton(new ElementButton(Language.getAndReplaceNP("back-button")), (executor) -> sendSearchPlayerResult(player, search, status, value));
            SimpleForm form = pendingReportForm
                    .onClose(executor -> {})
                    .build();
            form.send(player);
        } else {
            player.sendMessage(Language.getAndReplace("report-not-found"));
            ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
        }
    }

    public static void sendSearchReport(Player player) {
        CustomForm form = new CustomForm.Builder(Language.getAndReplaceNP("searchreport-title"))
                .addElement(new ElementDropdown(Language.getAndReplaceNP("searchreport-selectstatus"), Arrays.asList("Pending/Progress", "Closed")))
                .addElement(new ElementInput(Language.getAndReplaceNP("searchreport-id"), "Identification"))
                .onSubmit((executor, response) -> {
                    if (!response.getInputResponse(1).isEmpty()) {
                        if (response.getDropdownResponse(0).getElementContent().equals("Pending/Progress")) {
                            sendSearchReportReport(executor, Report.ReportStatus.PROGRESS, response.getInputResponse(1));
                        } else sendSearchReportReport(executor, Report.ReportStatus.CLOSED, response.getInputResponse(1));
                    } else {
                        executor.sendMessage(Language.getAndReplace("invalid-input"));
                        ReportSystemAPI.playSound(executor, Sound.NOTE_BASS);
                    }
                })
                .build();
        form.send(player);
    }

    public static void sendSearchReportReport(Player player, Report.ReportStatus status, String id) {
        if (!api.reportIDExists(id, status)) {
            player.sendMessage(Language.getAndReplace("no-reports-found"));
            ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
            return;
        }
        Report report = api.getReport(status, id);
        if (report != null) {
            SimpleForm.Builder pendingReportForm = new SimpleForm.Builder(Language.getAndReplaceNP("searchreportreport-title"),
                    Language.getAndReplaceNP("searchreportreport-content", report.getPlayer(), report.getTarget(), report.getReason(), report.getStatus(), report.getMember(), report.getId(), report.getDate()));
            pendingReportForm.addButton(new ElementButton(Language.getAndReplaceNP("back-button")), FormWindows::sendReportManager);
            SimpleForm form = pendingReportForm
                    .onClose(executor -> {})
                    .build();
            form.send(player);
        } else {
            player.sendMessage(Language.getAndReplace("report-not-found"));
            ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
        }
    }

    public static void sendMyreports(Player player) {
        SimpleForm.Builder myreportsForm = new SimpleForm.Builder(Language.getAndReplaceNP("myreports-title"), Language.getAndReplaceNP("myreports-content"));
        api.getReports(Report.ReportStatus.PENDING, Report.ReportSearch.PLAYER, player.getName()).forEach(report -> myreportsForm.addButton(new ElementButton(Language.getAndReplaceNP("myreports-button", report.getTarget(), report.getStatus())), executor -> sendMyreportsReport(executor, Report.ReportStatus.PENDING, report.getId())));
        api.getReports(Report.ReportStatus.PROGRESS, Report.ReportSearch.PLAYER, player.getName()).forEach(report -> myreportsForm.addButton(new ElementButton(Language.getAndReplaceNP("myreports-button", report.getTarget(), report.getStatus())), executor -> sendMyreportsReport(executor, Report.ReportStatus.PROGRESS, report.getId())));
        api.getReports(Report.ReportStatus.CLOSED, Report.ReportSearch.PLAYER, player.getName()).forEach(report -> myreportsForm.addButton(new ElementButton(Language.getAndReplaceNP("myreports-button", report.getTarget(), report.getStatus())), executor -> sendMyreportsReport(executor, Report.ReportStatus.CLOSED, report.getId())));
        SimpleForm form = myreportsForm
                .onClose(executor -> {})
                .build();
        form.send(player);
    }

    public static void sendMyreportsReport(Player player, Report.ReportStatus status, String id) {
        Report report = api.getReport(status, id);
        if (report != null) {
            SimpleForm.Builder pendingReportForm = new SimpleForm.Builder(Language.getAndReplaceNP("myreportsreport-title"),
                    Language.getAndReplaceNP("myreportsreport-content", report.getPlayer(), report.getTarget(), report.getReason(), report.getStatus(), report.getMember(), report.getId(), report.getDate()));
            pendingReportForm.addButton(new ElementButton(Language.getAndReplaceNP("back-button")), FormWindows::sendMyreports);
            SimpleForm form = pendingReportForm
                    .onClose(executor -> {})
                    .build();
            form.send(player);
        } else {
            player.sendMessage(Language.getAndReplace("report-not-found"));
            ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
        }
    }

}
