package net.llamadevelopment.reportsystem.listeners;

import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.level.Sound;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.data.Report;
import net.llamadevelopment.reportsystem.components.language.Language;

public class EventListener implements Listener {

    private final ReportSystem instance;

    public EventListener(ReportSystem instance) {
        this.instance = instance;
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Server.getInstance().getScheduler().scheduleDelayedTask(ReportSystem.getInstance(), () -> {
            if (event.getPlayer().hasPermission("reportsystem.command.reportmanager")) {
                this.instance.provider.getReports(Report.ReportStatus.PENDING, reports -> {
                    int pending = reports.size();
                    if (pending >= 1) {
                        event.getPlayer().sendMessage(Language.getAndReplace("pending-reports-info", pending));
                        ReportSystemAPI.playSound(event.getPlayer(), Sound.NOTE_BELL);
                    }
                });
            }
        }, 75);
    }

}
