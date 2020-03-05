package net.llamadevelopment.reportsystem.components.managers.database;

import net.llamadevelopment.reportsystem.ReportSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class MySqlProvider {

    private static Connection connection;

    private void connect(ReportSystem instance) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + instance.getConfig().getString("MySql.Host") + ":" + instance.getConfig().getString("MySql.Port") + "/" + instance.getConfig().getString("MySql.Database") + "?autoReconnect=true", instance.getConfig().getString("MySql.User"), instance.getConfig().getString("MySql.Password"));
            instance.getLogger().info("§aConnected successfully to database!");
        } catch (Exception e) {
            instance.getLogger().error("§4Failed to connect to database.");
            instance.getLogger().error("§4Please check your details in the config.yml.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public void createTables() {
        this.connect(ReportSystem.getInstance());
        update("CREATE TABLE IF NOT EXISTS opened(target VARCHAR(255), creator VARCHAR(255), reason VARCHAR(255), id VARCHAR(255), date VARCHAR(255), status VARCHAR(255), manager VARCHAR(255), PRIMARY KEY (id));");
        update("CREATE TABLE IF NOT EXISTS closed(target VARCHAR(255), creator VARCHAR(255), reason VARCHAR(255), id VARCHAR(255), date VARCHAR(255), status VARCHAR(255), manager VARCHAR(255), PRIMARY KEY (id));");
    }

    public static void update(String qry) {
        if (connection != null) {
            try {
                PreparedStatement ps = connection.prepareStatement(qry);
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
