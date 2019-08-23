package net.antopiamc.NamelessX.commands;

import java.util.List;

import co.aikar.taskchain.TaskChain;
import net.antopiamc.NamelessX.NamelessPlugin;
import net.antopiamc.NamelessX.api.NamelessException;
import net.antopiamc.NamelessX.api.NamelessPlayer;
import net.antopiamc.NamelessX.api.Notification;
import net.antopiamc.NamelessX.utils.Config;
import net.antopiamc.NamelessX.utils.Message;
import net.antopiamc.NamelessX.utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class GetNotificationsCommand extends Command {

	public GetNotificationsCommand() {
		super(Config.COMMANDS.getConfig().getString("get-notifications"),
				"Displays a list of website notifications",
				"{command}",
				Permission.COMMAND_GET_NOTIFICATIONS);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length != 0) {
			return false;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(Message.COMMAND_NOTAPLAYER.getMessage());
			return true;
		}
		
		Player player = (Player) sender;

		TaskChain<?> chain = NamelessPlugin.getChainFactory().newChain();
		chain
				.async(() -> {
					try {
						NamelessPlayer nameless = NamelessPlugin.getInstance().api.getPlayer(player.getUniqueId());

						if(!(nameless.exists())) {
							sender.sendMessage(Message.PLAYER_SELF_NOTREGISTERED.getMessage());
							chain.abort();
							return;
						}

						if (!(nameless.isValidated())) {
							sender.sendMessage(Message.PLAYER_SELF_NOTVALIDATED.getMessage());
							chain.abort();
							return;
						}

						List<Notification> notifications = nameless.getNotifications();
						if (notifications.size() == 0) {
							player.sendMessage(Message.COMMAND_NOTIFICATIONS_OUTPUT_NONOTIFICATIONS.getMessage());
							chain.abort();
							return;
						}

						chain.setTaskData("notifications", notifications);
						return;
					} catch (NamelessException e) {
						player.sendMessage(Message.COMMAND_NOTIFICATIONS_OUTPUT_FAIL_GENERIC.getMessage());
						e.printStackTrace();
						chain.abort();
						return;
					}
				})
				.abortIfNull()
				.sync(() -> {
					List<Notification> notifications = chain.getTaskData("notifications");
					notifications.forEach((notification) -> {
						BaseComponent[] message = new ComponentBuilder(notification.getMessage())
								.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Message.COMMAND_NOTIFICATIONS_OUTPUT_CLICKTOOPEN.getMessage()).create()))
								.event(new ClickEvent(ClickEvent.Action.OPEN_URL, notification.getUrl()))
								.create();
						player.spigot().sendMessage(message);
					});
				})
				.execute();
		return true;
	}

}