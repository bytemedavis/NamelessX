package net.antopiamc.NamelessX.commands;

import java.util.UUID;

import co.aikar.taskchain.TaskChain;
import net.antopiamc.NamelessX.NamelessPlugin;
import net.antopiamc.NamelessX.api.NamelessException;
import net.antopiamc.NamelessX.api.NamelessPlayer;
import net.antopiamc.NamelessX.utils.Config;
import net.antopiamc.NamelessX.utils.Message;
import net.antopiamc.NamelessX.utils.Permission;
import net.antopiamc.NamelessX.utils.UUIDFetcher;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UserInfoCommand extends Command {

	public UserInfoCommand() {
		super(Config.COMMANDS.getConfig().getString("user-info"),
				"Gets information about a user.",
				"{command} [Name]",
				Permission.COMMAND_USER_INFO);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(Message.COMMAND_NOTAPLAYER.getMessage());
				return true;
			}
			
			// Player itself as first argument
			return execute(sender, new String[] {((Player) sender).getUniqueId().toString()});
		}
		
		if (args.length != 1) {
			return false;
		}
		
		final String targetID = args[0]; // Name or UUID

		TaskChain<?> chain = NamelessPlugin.getChainFactory().newChain();
		chain
				.async(() -> {
					NamelessPlayer target;

					try {
						final UUID uuid = UUIDFetcher.getUUID(targetID);
						target = NamelessPlugin.getInstance().api.getPlayer(uuid);
					} catch (IllegalArgumentException e) {
						sender.sendMessage(Message.PLAYER_OTHER_NOTFOUND.getMessage());
						return;
					} catch (NamelessException e) {
						sender.sendMessage(e.getMessage());
						e.printStackTrace();
						return;
					}

					if (!target.exists()) {
						sender.sendMessage(Message.PLAYER_OTHER_NOTREGISTERED.getMessage());
						return;
					}

					String yes = Message.COMMAND_USERINFO_OUTPUT_BOOLEAN_TRUE.getMessage();
					String no = Message.COMMAND_USERINFO_OUTPUT_BOOLEAN_FALSE.getMessage();

					String validated = target.isValidated() ? yes : no;
					String banned = target.isBanned() ? yes : no;

					sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_USERNAME.getMessage("username", target.getUsername()));
					sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_DISPLAYNAME.getMessage("displayname", target.getDisplayName()));
					sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_UUID.getMessage("uuid", target.getUniqueId()));
					sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_GROUP.getMessage("groupname", target.getGroupName(), "id", target.getGroupID()));
					sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_REGISTERDATE.getMessage("date", target.getRegisteredDate())); // TODO Format nicely (add option in config for date format)
					sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_REPUTATION.getMessage("{reputation}", target.getReputation()));
					sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_VALIDATED.getMessage("validated", validated));
					sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_BANNED.getMessage("banned", banned));
				})
				.execute();
		return true;
	}

}