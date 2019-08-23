package net.antopiamc.NamelessX.listeners;

import co.aikar.taskchain.TaskChain;
import net.antopiamc.NamelessX.tasks.NotificationFetcher;
import net.antopiamc.NamelessX.utils.Config;
import net.antopiamc.NamelessX.utils.Message;
import net.antopiamc.NamelessX.NamelessPlugin;
import net.antopiamc.NamelessX.api.NamelessException;
import net.antopiamc.NamelessX.api.NamelessPlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerLogin implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		NamelessPlugin.LOGIN_TIME.put(player.getUniqueId(), System.currentTimeMillis());
		
		FileConfiguration config = Config.MAIN.getConfig();
		
		if (config.getBoolean("not-registered-join-message")) {
			TaskChain<?> chain = NamelessPlugin.getChainFactory().newChain();
			chain
					.delay(20)
					.async(() -> {
						try {
							NamelessPlayer namelessPlayer = NamelessPlugin.getInstance().api.getPlayer(player.getUniqueId());
							if (!namelessPlayer.exists()) {
								Message.JOIN_NOTREGISTERED.send(player);
							}
						} catch (NamelessException e) {
							e.printStackTrace();
						}
					})
					.execute();
		}

		if (config.getBoolean("check-notifications-join-message")) {
			new NotificationFetcher(player).runTask(NamelessPlugin.getInstance());
		}
	}

}