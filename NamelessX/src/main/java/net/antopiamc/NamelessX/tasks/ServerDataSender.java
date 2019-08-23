package net.antopiamc.NamelessX.tasks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.antopiamc.NamelessX.utils.Config;
import net.antopiamc.NamelessX.NamelessPlugin;
import net.antopiamc.NamelessX.api.ApiError;
import net.antopiamc.NamelessX.api.NamelessException;
import net.antopiamc.NamelessX.utils.TPSUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;

import static org.bukkit.Bukkit.getServer;

public class ServerDataSender extends BukkitRunnable {

	Gson gson = new Gson();
	
	@Override
	public void run() {
		int serverId = Config.MAIN.getConfig().getInt("server-id");
		if (serverId < 1) {
			return;
		}

		double currentTPS = TPSUtil.getCurrentTPS();
		if(currentTPS == -1){
			currentTPS = 20;
		}
		
		Map<String, Object> map = new HashMap<>();
		map.put("tps", currentTPS); // TODO tps
		map.put("time", System.currentTimeMillis());
		map.put("free-memory", Runtime.getRuntime().freeMemory());
		map.put("max-memory", Runtime.getRuntime().maxMemory());
		map.put("allocated-memory", Runtime.getRuntime().totalMemory());
		map.put("server-id", serverId);
		
		Map<String, Map<String, Object>> players = new HashMap<>();
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			Map<String, Object> playerInfo = new HashMap<>();
			
			playerInfo.put("name", player.getName());
			
			Map<String, Object> location = new HashMap<>();
			Location loc = player.getLocation();
			location.put("world", loc.getWorld().getName());
			location.put("x", loc.getBlockX());
			location.put("y", loc.getBlockY());
			location.put("z", loc.getBlockZ());
			
			playerInfo.put("location", location);
			playerInfo.put("ip", player.getAddress().getAddress().getHostAddress());
			
			try {
				if (NamelessPlugin.permissions != null) playerInfo.put("rank", NamelessPlugin.permissions.getPrimaryGroup(player));
			} catch (UnsupportedOperationException e) {}
			
			try {
				if (NamelessPlugin.economy != null) playerInfo.put("balance", NamelessPlugin.economy.getBalance(player));
			} catch (UnsupportedOperationException e) {}
			
			Map<String, String> placeholders = new HashMap<>();
			
			Config.MAIN.getConfig().getStringList("upload-placeholders")
				.forEach(placeholder -> 
				placeholders.put(placeholder, NamelessPlugin.getInstance().papiParser.parse(player, placeholder)));
			
			playerInfo.put("placeholders", placeholders);
			
			playerInfo.put("login-time", NamelessPlugin.LOGIN_TIME.get(player.getUniqueId()));
			
			players.put(player.getUniqueId().toString().replace("-", ""), playerInfo);
		}
		
		map.put("players", players);
		
		String data = gson.toJson(map);

		try {
			NamelessPlugin.getInstance().api.submitServerInfo(data);
		} catch (ApiError e) {
			if (e.getErrorCode() == 27) {
				NamelessPlugin.getLogManager().warn("Server ID is incorrect. Please enter a correct server ID or disable the server data uploader.");
			} else {
				e.printStackTrace();
			}
		} catch (NamelessException e) {
			e.printStackTrace();
		}
	}
}
