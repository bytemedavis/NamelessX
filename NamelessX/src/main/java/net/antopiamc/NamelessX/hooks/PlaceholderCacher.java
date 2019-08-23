package net.antopiamc.NamelessX.hooks;

import net.antopiamc.NamelessX.utils.Config;
import net.antopiamc.NamelessX.NamelessPlugin;
import net.antopiamc.NamelessX.api.NamelessException;
import net.antopiamc.NamelessX.api.NamelessPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import xyz.derkades.derkutils.caching.Cache;

public class PlaceholderCacher implements Runnable {
	
	@Override
	public void run() {
		try {
			int delay = Config.MAIN.getConfig().getInt("placeholders-request-delay", 5000);
			while (true) {
				Thread.sleep(500); // In case no players are online, wait in between checking for online players
				for (Player player : Bukkit.getOnlinePlayers()) {
					
					Thread.sleep(delay);
					try {
						NamelessPlayer nameless = NamelessPlugin.getInstance().api.getPlayer(player.getUniqueId());
						
						if (!(nameless.exists() && nameless.isValidated())) {
							continue;
						}
						
						int notificationCount = nameless.getNotifications().size();
						Cache.addCachedObject("nlmc-not" + player.getName(), notificationCount, 60);
					} catch (NamelessException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	

}
