package net.antopiamc.NamelessX.commands;

import co.aikar.taskchain.TaskChain;
import net.antopiamc.NamelessX.NamelessPlugin;
import net.antopiamc.NamelessX.api.ApiError;
import net.antopiamc.NamelessX.api.NamelessException;
import net.antopiamc.NamelessX.api.NamelessPlayer;
import net.antopiamc.NamelessX.utils.Config;
import net.antopiamc.NamelessX.utils.Message;
import net.antopiamc.NamelessX.utils.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


/**
 * Command used to submit a code to validate a user's NamelessMC account
 */
public class ValidateCommand extends Command {
	
	public ValidateCommand() {
		super(Config.COMMANDS.getConfig().getString("validate"),
				"Validates the user's website account using the given code.",
				"{command} <Code>",
				Permission.COMMAND_VALIDATE);
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

					try {
						namelessPlayer = NamelessPlugin.getInstance().api.getPlayer(player.getUniqueId());
					} catch (NamelessException e) {
						sender.sendMessage(Message.COMMAND_VALIDATE_OUTPUT_FAIL_GENERIC.getMessage());
						return;
					}

					if (!namelessPlayer.exists()) {
						sender.sendMessage(Message.PLAYER_SELF_NOTREGISTERED.getMessage());
						return;
					}

					if (namelessPlayer.isValidated()) {
						sender.sendMessage(Message.COMMAND_VALIDATE_OUTPUT_FAIL_ALREADYVALIDATED.getMessage());
						return;
					}

					final String code = args[0];

					try {
						namelessPlayer.validate(code);
						sender.sendMessage(Message.COMMAND_VALIDATE_OUTPUT_SUCCESS.getMessage());
					} catch (ApiError e) {
						if (e.getErrorCode() == ApiError.INVALID_VALIDATE_CODE) {
							sender.sendMessage(Message.COMMAND_VALIDATE_OUTPUT_FAIL_INVALIDCODE.getMessage());
						} else {
							throw new RuntimeException(e);
						}
					} catch (NamelessException e) {
						throw new RuntimeException(e);
					}
				})
				.execute();
		return true;
	}

}
