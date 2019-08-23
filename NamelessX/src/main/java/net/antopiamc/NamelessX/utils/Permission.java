package net.antopiamc.NamelessX.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public enum Permission {

	COMMAND_GET_NOTIFICATIONS("namelessx.command.getnotifications"),
	COMMAND_REGISTER("namelessx.command.register"),
	COMMAND_REPORT("namelessx.command.report"),
	COMMAND_SET_GROUP("namelessx.command.setgroup"),
	COMMAND_USER_INFO("namelessx.command.userinfo"),
	COMMAND_VALIDATE("namelessx.command.validate"),

	COMMAND_NAMELESS("namelessx.command.nameless"),

	;

	private org.bukkit.permissions.Permission permission;
	private String permissionString;

	Permission(final String permissionString){
		this.permission = new org.bukkit.permissions.Permission(permissionString);
		this.permissionString = permissionString;
	}

	@Override
	public String toString() {
		return this.permissionString;
	}

	public org.bukkit.permissions.Permission asPermission() {
		return this.permission;
	}

	public boolean hasPermission(final CommandSender sender) {
		return sender.hasPermission(this.asPermission());
	}

	public static org.bukkit.permissions.Permission toGroupSyncPermission(final String permission) {
		return new org.bukkit.permissions.Permission(permission, PermissionDefault.FALSE);
	}

}
