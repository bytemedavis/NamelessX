package net.antopiamc.NamelessX.listeners;

import net.antopiamc.NamelessX.NamelessPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		NamelessPlugin.LOGIN_TIME.remove(player.getUniqueId());
	}

}
