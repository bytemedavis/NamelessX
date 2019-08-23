package net.antopiamc.NamelessX.tasks;

import java.util.Set;
import java.util.UUID;

import net.antopiamc.NamelessX.utils.Config;
import net.antopiamc.NamelessX.NamelessPlugin;
import net.antopiamc.NamelessX.api.NamelessException;
import net.antopiamc.NamelessX.utils.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class WhitelistModifierTask extends BukkitRunnable {

	public WhitelistModifierTask() {
		NamelessPlugin.getLogManager().info("Whitelist Modifier enabled, starting...");
	}

	@Override
	public void run() {
		final boolean hideInactive = Config.MAIN.getConfig().getBoolean("auto-whitelist-registered.exclude-inactive");
		final boolean hideBanned = Config.MAIN.getConfig().getBoolean("auto-whitelist-registered.exclude-banned");

		final LogManager logger = NamelessPlugin.getLogManager();

		final Set<UUID> uuids;
		try {
			uuids = NamelessPlugin.getInstance().api.getRegisteredUsers(hideInactive, hideBanned).keySet();
		} catch (final NamelessException e) {
			logger.warn(
					"An error occured while getting a list of registered users from the website for the auto-whitelist-registered feature.");
			e.printStackTrace();
			return;
		}

		Bukkit.getScheduler().runTask(NamelessPlugin.getInstance(), () -> {
			for (final OfflinePlayer whitelistedPlayer : Bukkit.getWhitelistedPlayers()) {
				if (!uuids.contains(whitelistedPlayer.getUniqueId())) {
					// The player is whitelisted, but no(t) (longer) registered.
					whitelistedPlayer.setWhitelisted(false);
					uuids.remove(whitelistedPlayer.getUniqueId());
					logger.info("Removed " + whitelistedPlayer.getName() + " from the whitelist.");
				}
			}

			// All remaining UUIDs in the set are from players that are not on the whitelist
			// yet
			for (final UUID uuid : uuids) {
				final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				player.setWhitelisted(true);
				logger.info("Added " + player.getName() + " to the whitelist.");
			}
		});
	}

}
