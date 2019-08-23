package net.antopiamc.NamelessX.commands;

import java.util.ArrayList;

import net.antopiamc.NamelessX.utils.Message;
import net.antopiamc.NamelessX.NamelessPlugin;
import net.antopiamc.NamelessX.utils.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;

public abstract class Command extends org.bukkit.command.Command implements PluginIdentifiableCommand {
	
	public static final Command[] COMMANDS = {
			new GetNotificationsCommand(),
			new RegisterCommand(),
			new UserInfoCommand(),
			new ValidateCommand(),
	};
	
	private String usageMessage;
	private Permission permission;

	protected Command(String name, String description, String usageMessage, Permission permission) {
		super(name, description, usageMessage.replace("{command}", "/" + name), new ArrayList<>());
		
		this.usageMessage = usageMessage.replace("{command}", "/" + name);
		this.permission = permission;
	}

	@Override
	public NamelessPlugin getPlugin() {
		return NamelessPlugin.getInstance();
	}
	
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!permission.hasPermission(sender)) {
			Message.PLAYER_SELF_NO_PERMISSION_COMMAND.send(sender);
			return true;
		}
		
		boolean success = execute(sender, args);
		
		if (!success) {
			sender.sendMessage(this.getUsage());
		}
		
		return success;
	}
	
	public abstract boolean execute(CommandSender sender, String[] args);

}