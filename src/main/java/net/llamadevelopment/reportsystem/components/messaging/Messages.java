package net.llamadevelopment.reportsystem.components.messaging;

import net.llamadevelopment.reportsystem.ReportSystem;

public class Messages {

    public static String getAndReplace(String path, String... replacements) {
        String message = ReportSystem.getInstance().getConfig().getString(path);
        int i = 0;
        for (String replacement : replacements) {
            message = message.replace("[" + i + "]", replacement);
            i++;
        }
        return ReportSystem.getInstance().getConfig().getString("Messages.Prefix").replace("&", "§") + message.replace("&", "§");
    }

    public static String getAndReplaceNP(String path, String... replacements) {
        String message = ReportSystem.getInstance().getConfig().getString(path);
        int i = 0;
        for (String replacement : replacements) {
            message = message.replace("[" + i + "]", replacement);
            i++;
        }
        return message.replace("&", "§");
    }

}
