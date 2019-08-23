package net.antopiamc.NamelessX.utils;

import org.bukkit.plugin.Plugin;

public class LogManager {

    private Plugin INSTANCE;

    public LogManager(Plugin instance){
        INSTANCE = instance;
    }

    public void info(String message){
        INSTANCE.getLogger().info("§b" + message);
    }

    public void success(String message){
        INSTANCE.getLogger().info("§a" + message);
    }

    public void warn(String message){
        INSTANCE.getLogger().info("§e" + message);
    }

    public void error(String message){
        INSTANCE.getLogger().info("§c" + message);
    }
}
