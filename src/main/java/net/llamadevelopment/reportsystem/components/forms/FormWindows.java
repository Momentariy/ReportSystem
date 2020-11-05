package net.llamadevelopment.reportsystem.components.forms;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.level.Sound;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.data.Report;
import net.llamadevelopment.reportsystem.components.forms.custom.CustomForm;
import net.llamadevelopment.reportsystem.components.forms.simple.SimpleForm;
import net.llamadevelopment.reportsystem.components.language.Language;

import java.util.Arrays;
import java.util.Set;

public class FormWindows {

    private final ReportSystem instance;

    public FormWindows(ReportSystem instance) {
        this.instance = instance;
    }

    public void sendReportManager(Player player) {
        this.instance.provider.getReports(Report.ReportStatus.PENDING, pending -> this.instance.provider.getReports(Report.ReportStatus.PROGRESS, Report.ReportSearch.MEMBER, player.getName(), progress -> {
            SimpleForm form = new SimpleForm.Builder(Language.getAndReplaceNP("manager-title"), Language.getAndReplaceNP("manager-content"))
                    .addButton(new ElementButton(Language.getAndReplaceNP("manager-pendingreports", pending.size()),
                            new ElementButtonImageData("url", Language.getAndReplaceNP("manager-pendingreports-image"))), e -> this.sendPendingReports(player))
                    .addButton(new ElementButton(Language.getAndReplaceNP("manager-myopenreports", progress.size()),
                            new ElementButtonImageData("url", Language.getAndReplaceNP("manager-myopenreports-image"))), e -> this.sendMyProgressReports(player))
                    .addButton(new ElementButton(Language.getAndReplaceNP("manager-searchplayers"),
                            new ElementButtonImageData("url", Language.getAndReplaceNP("manager-searchplayers-image"))), e -> this.sendSearchPlayer(player))
                    .addButton(new ElementButton(Language.getAndReplaceNP("manager-searchreport"),
                            new ElementButtonImageData("url", Language.getAndReplaceNP("manager-searchreport-image"))), e -> this.sendSearchReport(player))
                    .build();
            form.send(player);
        }));

    }

    public void sendPendingReports(Player player) {
        SimpleForm.Builder pendingForm = new SimpleForm.Builder(Language.getAndReplaceNP("pending-title"), Language.getAndReplaceNP("pending-content"));
        this.instance.provider.getReports(Report.ReportStatus.PENDING, reports -> {
            reports.forEach(report -> pendingForm.addButton(new ElementButton(Language.getAndReplaceNP("pending-button", report.getTarget(), report.getPlayer(), report.getReason())), executor -> sendPendingReportMenu(executor, report.getId())));
        });
        pendingForm.addButton(new ElementButton(Language.getAndReplaceNP("back-button")), e -> this.sendReportManager(player));
        SimpleForm form = pendingForm.build();
        form.send(player);
    }

    public void sendPendingReportMenu(Player player, String id) {
        this.instance.provider.getReport(Report.ReportStatus.PENDING, id, report -> {
            if (report != null) {
                SimpleForm.Builder pendingReportForm = new SimpleForm.Builder(Language.getAndReplaceNP("pendingreport-title"),
                        Language.getAndReplaceNP("pendingreport-content", report.getPlayer(), report.getTarget(), report.getReason(), report.getStatus(), report.getMember(), report.getId(), report.getDate()));
                if (report.getMember().equals("Unknown")) {
                    pendingReportForm.addButton(new ElementButton(Language.getAndReplaceNP("pendingreport-take"), new ElementButtonImageData("url", Language.getAndReplaceNP("pendingreport-take-image"))), executor -> {
                        this.instance.provider.updateStatus(id, "Progress");
                        this.instance.provider.updateStaffmember(id, executor.getName());
                        executor.sendMessage(Language.getAndReplace("report-took", report.getId()));
                        ReportSystemAPI.playSound(player, Sound.RANDOM_LEVELUP);
                    });
                }
                pendingReportForm.addButton(new ElementButton(Language.getAndReplaceNP("back-button")), e -> this.sendPendingReports(player));
                SimpleForm form = pendingReportForm.build();
                form.send(player);
            } else {
                player.sendMessage(Language.getAndReplace("report-not-found"));
                ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
            }
        });
    }

    public void sendMyProgressReports(Player player) {
        SimpleForm.Builder pendingForm = new SimpleForm.Builder(Language.getAndReplaceNP("progress-title"), Language.getAndReplaceNP("progress-content"));
        this.instance.provider.getReports(Report.ReportStatus.PROGRESS, Report.ReportSearch.MEMBER, player.getName(), reports -> {
            reports.forEach(report -> pendingForm.addButton(new ElementButton(Language.getAndReplaceNP("progress-button", report.getTarget(), report.getPlayer(), report.getReason())), executor -> sendProgressReportMenu(executor, report.getId())));
        });
        pendingForm.addButton(new ElementButton(Language.getAndReplaceNP("back-button")), e -> this.sendReportManager(player));
        SimpleForm form = pendingForm.build();
        form.send(player);
    }

    public void sendProgressReportMenu(Player player, String id) {
        this.instance.provider.getReport(Report.ReportStatus.PROGRESS, id, report -> {
            if (report != null) {
                SimpleForm.Builder pendingReportForm = new SimpleForm.Builder(Language.getAndReplaceNP("progressreport-title"),
                        Language.getAndReplaceNP("progressreport-content", report.getPlayer(), report.getTarget(), report.getReason(), report.getStatus(), report.getMember(), report.getId(), report.getDate()));
                if (report.getMember().equals(player.getName()) || player.isOp()) {
                    pendingReportForm.addButton(new ElementButton(Language.getAndReplaceNP("progressreport-close"), new ElementButtonImageData("url", Language.getAndReplaceNP("progressreport-close-image"))), executor -> {
                        this.instance.provider.closeReport(id);
                        executor.sendMessage(Language.getAndReplace("report-closed", report.getId()));
                        ReportSystemAPI.playSound(player, Sound.RANDOM_LEVELUP);
                    });
                    pendingReportForm.addButton(new ElementButton(Language.getAndReplaceNP("progressreport-delete"), new ElementButtonImageData("url", Language.getAndReplaceNP("progressreport-delete-image"))), executor -> {
                        this.instance.provider.deleteReport(id);
                        executor.sendMessage(Language.getAndReplace("report-deleted", report.getId()));
                        ReportSystemAPI.playSound(player, Sound.RANDOM_LEVELUP);
                    });
                }
                pendingReportForm.addButton(new ElementButton(Language.getAndReplaceNP("back-button")), e -> this.sendMyProgressReports(player));
                SimpleForm form = pendingReportForm.build();
                form.send(player);
            } else {
                player.sendMessage(Language.getAndReplace("report-not-found"));
                ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
            }
        });
    }

    public void sendSearchPlayer(Player player) {
        CustomForm form = new CustomForm.Builder(Language.getAndReplaceNP("searchplayer-title"))
                .addElement(new ElementDropdown(Language.getAndReplaceNP("searchplayer-selectsearch"), Arrays.asList("Player", "Target", "Member")))
                .addElement(new ElementDropdown(Language.getAndReplaceNP("searchplayer-selectstatus"), Arrays.asList("Pending", "Progress", "Closed")))
                .addElement(new ElementInput(Language.getAndReplaceNP("searchplayer-player"), "Player"))
                .onSubmit((executor, response) -> {
                    if (!response.getInputResponse(2).isEmpty()) {
                        this.sendSearchPlayerResult(executor, Report.ReportSearch.valueOf(response.getDropdownResponse(0).getElementContent().toUpperCase()), Report.ReportStatus.valueOf(response.getDropdownResponse(1).getElementContent().toUpperCase()), response.getInputResponse(2));
                    } else {
                        executor.sendMessage(Language.getAndReplace("invalid-input"));
                        ReportSystemAPI.playSound(executor, Sound.NOTE_BASS);
                    }
                })
                .build();
        form.send(player);
    }

    public void sendSearchPlayerResult(Player player, Report.ReportSearch search, Report.ReportStatus status, String value) {
        this.instance.provider.getReports(status, search, value, reports -> {
            if (reports.size() == 0) {
                player.sendMessage(Language.getAndReplace("no-reports-found"));
                ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
                return;
            }
            SimpleForm.Builder resultForm = new SimpleForm.Builder(Language.getAndReplaceNP("searchplayerresult-title"), Language.getAndReplaceNP("searchplayerresult-content"));
            reports.forEach(report -> resultForm.addButton(new ElementButton(Language.getAndReplaceNP("searchplayerresult-button", report.getTarget(), report.getPlayer(), report.getReason())), executor -> sendSearchPlayerReport(executor, search, status, value, report.getId())));
            resultForm.addButton(new ElementButton(Language.getAndReplaceNP("back-button")), e -> this.sendReportManager(player));
            SimpleForm form = resultForm.build();
            form.send(player);
        });
    }

    public void sendSearchPlayerReport(Player player, Report.ReportSearch search, Report.ReportStatus status, String value, String id) {
        this.instance.provider.getReport(status, id, report -> {
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
        });
    }

    public void sendSearchReport(Player player) {
        CustomForm form = new CustomForm.Builder(Language.getAndReplaceNP("searchreport-title"))
                .addElement(new ElementDropdown(Language.getAndReplaceNP("searchreport-selectstatus"), Arrays.asList("Pending/Progress", "Closed")))
                .addElement(new ElementInput(Language.getAndReplaceNP("searchreport-id"), "Identification"))
                .onSubmit((executor, response) -> {
                    if (!response.getInputResponse(1).isEmpty()) {
                        if (response.getDropdownResponse(0).getElementContent().equals("Pending/Progress")) {
                            this.sendSearchReportReport(executor, Report.ReportStatus.PROGRESS, response.getInputResponse(1));
                        } else this.sendSearchReportReport(executor, Report.ReportStatus.CLOSED, response.getInputResponse(1));
                    } else {
                        executor.sendMessage(Language.getAndReplace("invalid-input"));
                        ReportSystemAPI.playSound(executor, Sound.NOTE_BASS);
                    }
                })
                .build();
        form.send(player);
    }

    public void sendSearchReportReport(Player player, Report.ReportStatus status, String id) {
        this.instance.provider.reportIDExists(id, status, exists -> {
            if (!exists) {
                player.sendMessage(Language.getAndReplace("no-reports-found"));
                ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
                return;
            }
            this.instance.provider.getReport(status, id, report -> {
                if (report != null) {
                    SimpleForm.Builder pendingReportForm = new SimpleForm.Builder(Language.getAndReplaceNP("searchreportreport-title"),
                            Language.getAndReplaceNP("searchreportreport-content", report.getPlayer(), report.getTarget(), report.getReason(), report.getStatus(), report.getMember(), report.getId(), report.getDate()));
                    pendingReportForm.addButton(new ElementButton(Language.getAndReplaceNP("back-button")), e -> this.sendReportManager(player));
                    SimpleForm form = pendingReportForm
                            .onClose(executor -> {})
                            .build();
                    form.send(player);
                } else {
                    player.sendMessage(Language.getAndReplace("report-not-found"));
                    ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
                }
            });
        });
    }

    public void sendMyreports(Player player, Set<Report> pending, Set<Report> progress, Set<Report> closed) {
        SimpleForm.Builder myreportsForm = new SimpleForm.Builder(Language.getAndReplaceNP("myreports-title"), Language.getAndReplaceNP("myreports-content"));
        pending.forEach(report -> myreportsForm.addButton(new ElementButton(Language.getAndReplaceNP("myreports-button", report.getTarget(), report.getStatus())), executor -> sendMyreportsReport(executor, Report.ReportStatus.PENDING, report.getId(), pending, progress, closed)));
        progress.forEach(report -> myreportsForm.addButton(new ElementButton(Language.getAndReplaceNP("myreports-button", report.getTarget(), report.getStatus())), executor -> sendMyreportsReport(executor, Report.ReportStatus.PROGRESS, report.getId(), pending, progress, closed)));
        closed.forEach(report -> myreportsForm.addButton(new ElementButton(Language.getAndReplaceNP("myreports-button", report.getTarget(), report.getStatus())), executor -> sendMyreportsReport(executor, Report.ReportStatus.CLOSED, report.getId(), pending, progress, closed)));
        SimpleForm form = myreportsForm.build();
        form.send(player);
    }

    public void sendMyreportsReport(Player player, Report.ReportStatus status, String id, Set<Report> pending, Set<Report> progress, Set<Report> closed) {
        this.instance.provider.getReport(status, id, report -> {
            if (report != null) {
                SimpleForm.Builder pendingReportForm = new SimpleForm.Builder(Language.getAndReplaceNP("myreportsreport-title"),
                        Language.getAndReplaceNP("myreportsreport-content", report.getPlayer(), report.getTarget(), report.getReason(), report.getStatus(), report.getMember(), report.getId(), report.getDate()));
                pendingReportForm.addButton(new ElementButton(Language.getAndReplaceNP("back-button")), e -> this.sendMyreports(player, pending, progress, closed));
                SimpleForm form = pendingReportForm
                        .onClose(executor -> {})
                        .build();
                form.send(player);
            } else {
                player.sendMessage(Language.getAndReplace("report-not-found"));
                ReportSystemAPI.playSound(player, Sound.NOTE_BASS);
            }
        });
    }

}
