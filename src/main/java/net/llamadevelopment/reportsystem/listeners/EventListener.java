//This file was created by Mundschutziii. 
//You can change the code here, but do not sell this file as your own.

package net.llamadevelopment.reportsystem.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.utils.Config;
import net.llamadevelopment.reportsystem.ReportSystem;
import org.bson.Document;

public class EventListener implements Listener {

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Config config = ReportSystem.getInstance().getConfig();
        if (config.getBoolean("JoinNotification")) {
            Player player = event.getPlayer();
            if (player.hasPermission("reportsystem.command.reportmanager")) {
                if (config.getBoolean("MongoDB")) {
                    int i = 0;
                    for (Document doc : ReportSystem.getInstance().getOpenReportCollection().find(new Document("status", "Open"))) {
                        i++;
                    }
                    if (i != 0) {
                        player.sendMessage(config.getString("Prefix").replace("&", "§") + config.getString("JoinMessage").replace("&", "§").replace("%count%", "" + i));
                    }
                } else {
                    Config rd = new Config(ReportSystem.getInstance().getDataFolder() + "/data/reportdata.yml", Config.YAML);
                    int i = 0;
                    for (String s : rd.getStringList("Data")) {
                        i++;
                    }
                    if (i != 0) {
                        player.sendMessage(config.getString("Prefix").replace("&", "§") + config.getString("JoinMessage").replace("&", "§").replace("%count%", "" + i));
                    }
                }
            }
        }
    }
}
