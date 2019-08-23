package net.antopiamc.NamelessX.commands;

import co.aikar.taskchain.TaskChain;
import net.antopiamc.NamelessX.NamelessPlugin;
import net.antopiamc.NamelessX.api.ApiError;
import net.antopiamc.NamelessX.api.NamelessException;
import net.antopiamc.NamelessX.api.NamelessPlayer;
import net.antopiamc.NamelessX.utils.Config;
import net.antopiamc.NamelessX.utils.Message;
import net.antopiamc.NamelessX.utils.Permission;
import org.apache.commons.validator.routines.EmailValidator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand extends Command {

	public RegisterCommand() {
		super(Config.COMMANDS.getConfig().getString("register"),
				"Creates an account. Will output a link or email address to complete registration.",
				"{command} <E-Mail>",
				Permission.COMMAND_REGISTER);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length != 1) {
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
					NamelessPlayer namelessPlayer;

					boolean valid = EmailValidator.getInstance().isValid(args[0]);
					if(!valid){
						player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_EMAILINVALID.getMessage());
						return;
					}

					try {
						namelessPlayer = NamelessPlugin.getInstance().api.getPlayer(player.getUniqueId());
					} catch (NamelessException e) {
						sender.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_GENERIC.getMessage());
						e.printStackTrace();
						return;
					}

					if (namelessPlayer.exists()) {
						sender.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_ALREADYEXISTS.getMessage());
						return;
					}

					try {
						String link = namelessPlayer.register(player.getName(), args[0]);
						if (link.equals("")) {
							player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_SUCCESS_EMAIL.getMessage());
						} else {
							player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_SUCCESS_LINK.getMessage("link", link));
						}
					} catch (ApiError e) {
						if (e.getErrorCode() == ApiError.EMAIL_ALREADY_EXISTS) {
							player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_EMAILUSED.getMessage());
						} else if (e.getErrorCode() == ApiError.USERNAME_ALREADY_EXISTS) {
							player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_ALREADYEXISTS.getMessage());
						} else if (e.getErrorCode() == ApiError.INVALID_EMAIL_ADDRESS) {
							player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_EMAILINVALID.getMessage());
						} else {
							player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_GENERIC.getMessage());
							e.printStackTrace();
						}
					} catch (NamelessException e) {
						player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_GENERIC.getMessage());
						e.printStackTrace();
					}
				})
				.execute();
		return true;
	}

}