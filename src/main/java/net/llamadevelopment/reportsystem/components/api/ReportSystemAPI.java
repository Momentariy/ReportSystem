package net.llamadevelopment.reportsystem.components.api;

import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.network.protocol.PlaySoundPacket;
import net.llamadevelopment.reportsystem.components.managers.database.Provider;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class ReportSystemAPI {

    private static Provider provider;

    public static void setProvider(Provider provider) {
        ReportSystemAPI.provider = provider;
    }

    public static Provider getProvider() {
        return provider;
    }

    public static String getRandomIDCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder stringBuilder = new StringBuilder();
        Random rnd = new Random();
        while (stringBuilder.length() < 6) {
            int index = (int) (rnd.nextFloat() * chars.length());
            stringBuilder.append(chars.charAt(index));
        }
        return stringBuilder.toString();
    }

    public static String getDate() {
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");
        return dateFormat.format(now);
    }

    public static void playSound(Player player, Sound sound) {
        PlaySoundPacket packet = new PlaySoundPacket();
        packet.name = sound.getSound();
        packet.x = new Double(player.getLocation().getX()).intValue();
        packet.y = (new Double(player.getLocation().getY())).intValue();
        packet.z = (new Double(player.getLocation().getZ())).intValue();
        packet.volume = 1.0F;
        packet.pitch = 1.0F;
        player.dataPacket(packet);
    }
}
