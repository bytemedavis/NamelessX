package net.antopiamc.NamelessX.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public enum Message {
	
	PLAYER_OTHER_NOTFOUND("player.other.not-found", 
			"This player could not be found."),
	PLAYER_OTHER_NOTREGISTERED("player.other.not-registered", 
			"This player is not registered on the website."),
	PLAYER_SELF_NOTVALIDATED("player.self.not-validated", 
			"Your account must be validated to perform this action."),
	PLAYER_SELF_NOTREGISTERED("player.self.not-registered", 
			"You must register for an account to perform this action."),
	PLAYER_SELF_NO_PERMISSION_COMMAND("player.self.no-permission.command",
			"You don't have permission to execute this command."),

	COMMAND_NOTAPLAYER("command.not-a-player", 
			"You must be a player to perform this command."),

	COMMAND_NOTIFICATIONS_OUTPUT_NONOTIFICATIONS("command.notifications.output.no-notifications",
			"You do not have any unread notifications."),
	COMMAND_NOTIFICATIONS_OUTPUT_CLICKTOOPEN("command.notifications.output.click-to-open",
			"Click to open in web browser"),
	COMMAND_NOTIFICATIONS_OUTPUT_FAIL_GENERIC("command.notifications.output.fail.generic",
			"An error occured while trying to retrieve a list of notifications. Please notify the server administrator about this issue."),

	COMMAND_REGISTER_OUTPUT_SUCCESS_EMAIL("command.register.output.success.email",
			"Please check your inbox to complete registration."),
	COMMAND_REGISTER_OUTPUT_SUCCESS_LINK("command.register.output.success.link",
			"Please visit {link} to complete registration."),
	COMMAND_REGISTER_OUTPUT_FAIL_GENERIC("command.register.output.fail.generic",
			"An error occured while trying to register. Please notify the server administrator about this issue."),
	COMMAND_REGISTER_OUTPUT_FAIL_ALREADYEXISTS("command.register.output.fail.already-exists",
			"You already have an account."),
	COMMAND_REGISTER_OUTPUT_FAIL_EMAILUSED("command.register.output.fail.email-used",
			"This email address is already used for a different user account."),
	COMMAND_REGISTER_OUTPUT_FAIL_EMAILINVALID("commands.register.output.fail.email-invalid",
			"The provided email address is invalid."),

	COMMAND_VALIDATE_OUTPUT_SUCCESS("command.validate.output.success",
			"Your account has been validated."),
	COMMAND_VALIDATE_OUTPUT_FAIL_ALREADYVALIDATED("command.validate.output.fail.already-validated",
			"Your account has already been validated."),
	COMMAND_VALIDATE_OUTPUT_FAIL_INVALIDCODE("command.validate.output.fail.invalid-code",
			"Your validation code is incorrect. Please check if you copied it correctly and try again."),
	COMMAND_VALIDATE_OUTPUT_FAIL_GENERIC("command.user-info.output.fail.generic",
			"An unknown error occured while trying to submit a validation code."),

	COMMAND_USERINFO_OUTPUT_USERNAME("command.user-info.output.username",
			"Username: {username}"),
	COMMAND_USERINFO_OUTPUT_DISPLAYNAME("command.user-info.output.displayname",
			"Display name: {displayname}"),
	COMMAND_USERINFO_OUTPUT_UUID("command.user-info.output.uuid",
			"UUID: {uuid}"),
	COMMAND_USERINFO_OUTPUT_GROUP("command.user-info.output.group",
			"Group: {groupname} (id: {id})"),
	COMMAND_USERINFO_OUTPUT_REGISTERDATE("command.user-info.output.registered-date",
			"Registered on {date}"),
	COMMAND_USERINFO_OUTPUT_VALIDATED("command.user-info.output.validated",
			"Account validated: {validated}"),
	COMMAND_USERINFO_OUTPUT_REPUTATION("command.user-info.output.reputation",
			"Reputation: {reputation}"),
	COMMAND_USERINFO_OUTPUT_BANNED("command.user-info.output.banned",
			"Banned: {banned}"),
	COMMAND_USERINFO_OUTPUT_BOOLEAN_TRUE("command.user-info.output.boolean.true",
			"yes"),
	COMMAND_USERINFO_OUTPUT_BOOLEAN_FALSE("command.user-info.output.boolean.false",
			"no"),
	
	JOIN_NOTREGISTERED("join.not-registed",
			"You do not have an account on our website yet. Please register using /register"),
	
	;

	private String path;
	private String defaultMessage;
	
	Message(String path, String defaultMessage){
		this.path = path;
		this.defaultMessage = defaultMessage;
	}
	
	public String getMessage() {
		return ChatColor.translateAlternateColorCodes('&', Config.MESSAGES.getConfig().getString(path));
	}

	public String getMessage(Object... placeholders) {
		if (placeholders.length % 2 != 0) { // False if length is 1, 3, 5, 6, etc.
			throw new IllegalArgumentException("Placeholder array length must be an even number");
		}
		
		if (placeholders.length == 0) {
			return getMessage();
		}
		
		Map<String, String> placeholderMap = new HashMap<>();
		
		Object key = null;
		
		for (Object object : placeholders) {
			if (key == null) {
				// 'placeholder' is a key
				key = object;
			} else {
				// Key has been set previously, 'placeholder' must be a value
				placeholderMap.put(key.toString(), object.toString());
				key = null; // Next 'placeholder' is a key
			}
		}
		
		String message = this.getMessage();

		for(Map.Entry<String, String> entry : placeholderMap.entrySet()) {
			message = message.replace("{" + entry.getKey() + "}", entry.getValue());
		}

		return message;
	}
	
	public void send(CommandSender sender) {
		sender.sendMessage(this.getMessage());
	}
	
	public void send(CommandSender sender, Object... placeholders) {
		sender.sendMessage(this.getMessage(placeholders));
	}
	
	public static void generateConfig(Config config) throws IOException {
		FileConfiguration fileConfig = config.getConfig();
		for (Message message : Message.values()) {
			if (!fileConfig.contains(message.path))
				fileConfig.set(message.path, message.defaultMessage);
		}
		config.setConfig(fileConfig);
		config.save();
	}

}
