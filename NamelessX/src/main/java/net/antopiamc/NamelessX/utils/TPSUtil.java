package net.antopiamc.NamelessX.utils;

import net.antopiamc.NamelessX.NamelessPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import java.lang.reflect.Field;

public class TPSUtil {
    private static Object minecraftServer;
    private static Field recentTps;

    public static double getCurrentTPS() {
        try {
            return getRecentTpsRefl()[0];
        } catch (Throwable throwable) {
            NamelessPlugin.getLogManager().error("Error while fetching current TPS. Reflection based?");
            NamelessPlugin.getLogManager().error("Message: " + throwable.getMessage());
            NamelessPlugin.getLogManager().error("--------------------------");
            throwable.printStackTrace();
            return -1;
        }
    }

    private static double[] getRecentTpsRefl() throws Throwable {
        if (minecraftServer == null) {
            Server server = Bukkit.getServer();
            Field consoleField = server.getClass().getDeclaredField("console");
            consoleField.setAccessible(true);
            minecraftServer = consoleField.get(server);
        }
        if (recentTps == null) {
            recentTps = minecraftServer.getClass().getSuperclass().getDeclaredField("recentTps");
            recentTps.setAccessible(true);
        }
        return (double[]) recentTps.get(minecraftServer);
    }
}
