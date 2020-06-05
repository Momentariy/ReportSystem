package net.llamadevelopment.reportsystem.listeners;

import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.level.Sound;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.api.ReportSystemAPI;
import net.llamadevelopment.reportsystem.components.data.ReportStatus;
import net.llamadevelopment.reportsystem.components.tools.Language;

public class EventListener implements Listener {

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Server.getInstance().getScheduler().scheduleDelayedTask(ReportSystem.getInstance(), () -> {
            if (event.getPlayer().hasPermission("reportsystem.command.reportmanager")) {
                int pending = ReportSystemAPI.getProvider().getReports(ReportStatus.PENDING).size();
                if (pending >= 1) {
                    event.getPlayer().sendMessage(Language.getAndReplace("pending-reports-info", pending));
                    ReportSystemAPI.playSound(event.getPlayer(), Sound.NOTE_BELL);
                }
            }
        }, 50);
    }
}
