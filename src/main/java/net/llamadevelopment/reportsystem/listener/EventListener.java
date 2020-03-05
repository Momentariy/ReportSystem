package net.llamadevelopment.reportsystem.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.utils.Config;
import net.llamadevelopment.reportsystem.ReportSystem;
import net.llamadevelopment.reportsystem.components.managers.database.MongoDBProvider;
import net.llamadevelopment.reportsystem.components.managers.database.MySqlProvider;
import net.llamadevelopment.reportsystem.components.messaging.Messages;
import org.bson.Document;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EventListener implements Listener {

    private static ReportSystem instance = ReportSystem.getInstance();

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Config config = ReportSystem.getInstance().getConfig();
        if (config.getBoolean("Settings.JoinNotification")) {
            Player player = event.getPlayer();
            if (player.hasPermission("reportsystem.command.reportmanager")) {
                if (instance.isMongodb()) {
                    int i = 0;
                    for (Document doc : MongoDBProvider.getOpenReportCollection().find(new Document("status", "Open"))) {
                        i++;
                    }
                    if (i != 0) {
                        player.sendMessage(Messages.getAndReplace("Messages.JoinMessage", "" + i));
                    }
                } else if (instance.isMysql()) {
                    int i = 0;
                    try {
                        PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM opened WHERE STATUS = ?");
                        preparedStatement.setString(1, "Open");
                        ResultSet rs = preparedStatement.executeQuery();
                        while (rs.next()) {
                            i++;
                        }
                        if (i != 0) {
                            player.sendMessage(Messages.getAndReplace("Messages.JoinMessage", "" + i));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (instance.isYaml()) {
                    Config rd = new Config(ReportSystem.getInstance().getDataFolder() + "/data/reportdata.yml", Config.YAML);
                    int i = 0;
                    for (String s : rd.getStringList("Data")) {
                        i++;
                    }
                    if (i != 0) {
                        player.sendMessage(Messages.getAndReplace("Messages.JoinMessage", "" + i));
                    }
                }
            }
        }
    }
}
