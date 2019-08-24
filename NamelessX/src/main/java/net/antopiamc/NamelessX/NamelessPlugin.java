package net.antopiamc.NamelessX;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import net.antopiamc.NamelessX.api.NamelessAPI;
import net.antopiamc.NamelessX.commands.Command;
import net.antopiamc.NamelessX.commands.PluginCommand;
import net.antopiamc.NamelessX.hooks.*;
import net.antopiamc.NamelessX.listeners.PlayerLogin;
import net.antopiamc.NamelessX.listeners.PlayerQuit;
import net.antopiamc.NamelessX.tasks.ServerDataSender;
import net.antopiamc.NamelessX.tasks.WhitelistModifierTask;
import net.antopiamc.NamelessX.utils.Config;
import net.antopiamc.NamelessX.utils.LogManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class NamelessPlugin extends JavaPlugin {

	private static NamelessPlugin instance;

	public static final Map<UUID, Long> LOGIN_TIME = new HashMap<>();

	public static net.milkbowl.vault.permission.Permission permissions;
	public static Economy economy;

	public NamelessAPI api;
	private static LogManager LOG_MANAGER;
	private static TaskChainFactory CHAIN_FACTORY;

	public PapiParser papiParser;

	@Override
	public void onLoad() {
		instance = this;
		LOG_MANAGER = new LogManager(this);

		if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
			final RegisteredServiceProvider<Permission> permissionProvider = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
			if (permissionProvider == null) {
				getLogManager().warn("No vault compatible permissions plugin was found. Rank sync will not work.");
			} else {
				permissions = permissionProvider.getProvider();

				if (permissions == null) {
					getLogManager().warn("No vault compatible permissions plugin was found. Rank sync will not work.");
				}
			}

			final RegisteredServiceProvider<Economy> economyProvider = this.getServer().getServicesManager().getRegistration(Economy.class);
			if (economyProvider == null) {
				getLogManager().warn("No economy plugin was found.");
			} else {
				economy = economyProvider.getProvider();

				if (economy == null) {
					getLogManager().warn("No economy plugin was found.");
				}
			}
		} else {
			getLogManager().warn("Vault was not found. Rank & Group Sync will not work.");
		}

		try {
			Config.initialize();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		if (!this.initApi()) {
			throw new RuntimeException("Unable to initialize API");
		}
	}

	@Override
	public void onEnable() {
		CHAIN_FACTORY = BukkitTaskChainFactory.create(this);
		this.initHooks();

		// Connection is successful, move on with registering listeners and commands.
		this.registerCommands();
		this.getServer().getPluginManager().registerEvents(new PlayerLogin(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerQuit(), this);

		// Start saving data files every 15 minutes
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new SaveConfig(), 5*60*20, 5*60*20);

		final int uploadPeriod = Config.MAIN.getConfig().getInt("server-data-upload-rate", 10) * 20;
		if (uploadPeriod > 0) {
			new ServerDataSender().runTaskTimerAsynchronously(this, uploadPeriod, uploadPeriod);
		}

		// For reloads
		for (final Player player : Bukkit.getOnlinePlayers()) {
			LOGIN_TIME.put(player.getUniqueId(), System.currentTimeMillis());
		}


		initWhitelistTask();
		getLogManager().info("NamelessX was started successfully!");
	}

	@Override
	public void onDisable() {
		// Save all configuration files that require saving
		for (final Config config : Config.values()) {
			if (config.autoSave()) {
				config.save();
			}
		}
		getServer().getScheduler().cancelTasks(this);
	}

	private boolean initApi() {
		final FileConfiguration config = Config.MAIN.getConfig();
		final String url = config.getString("api-url");
		if (url.equals("")) {
			getLogManager().error("No API URL set in the NamelessMC configuration. Nothing will work until you set the correct url.");
			return false; // Prevent registering of commands, listeners, etc.
		} else {
			URL apiUrl;
			try {
				apiUrl = new URL(url);
			} catch (final MalformedURLException e) {
				// There is an exception, so the connection was not successful.
				getLogManager().error("Syntax error in API URL. Nothing will work until you set the correct url.");
				getLogManager().error("Error: " + e.getMessage());
				return false; // Prevent registering of commands, listeners, etc.
			}

			final boolean debug = config.getBoolean("api-debug-mode", false);

			this.api = new NamelessAPI(apiUrl, debug);

			if (config.contains("user-agent")) {
				this.api.setUserAgent(config.getString("user-agent"));
			}

			final Exception exception = this.api.checkWebAPIConnection();
			if (exception != null) {
				// There is an exception, so the connection was unsuccessful.
				getLogManager().error("Invalid API URL/key. Nothing will work until you set the correct url.");
				getLogManager().error("Error: " + exception.getMessage());
				exception.printStackTrace();
				return false; // Prevent registering of commands, listeners, etc.
			}
		}
		return true;
	}

	private void registerCommands() {
		this.getServer().getPluginCommand("namelessplugin").setExecutor(new PluginCommand());

		try {
			final Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			field.setAccessible(true);
			final CommandMap map = (CommandMap) field.get(Bukkit.getServer());

			final String name = this.getName(); //Get name of plugin from config.yml just in case we ever change it

			final boolean additional = Config.COMMANDS.getConfig().getBoolean("enable-additional-commands", true);

			if (additional) {
				for (final Command command : Command.COMMANDS) {
					if (command.getName().equals("disabled")) {
						continue;
					}

					map.register(name, command);
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}

	private void initHooks() {
		boolean placeholderPluginInstalled = false;

		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			final PapiHook placeholders = new PapiHook();
			placeholders.register();
			placeholderPluginInstalled = true;

			this.papiParser = new PapiParserEnabled();
		} else {
			this.papiParser = new PapiParserDisabled();
		}

		if (placeholderPluginInstalled && Config.MAIN.getConfig().getBoolean("enable-placeholders", false)) {
			Bukkit.getScheduler().runTaskAsynchronously(this, new PlaceholderCacher());
		}
	}

	private void initWhitelistTask(){
		if (!Config.MAIN.getConfig().getBoolean("auto-whitelist-registered.enabled", false)) {
			getLogManager().warn("Whitelist Modifier not enabled in config, aborting.");
			return;
		}

		new WhitelistModifierTask().runTaskTimerAsynchronously(this, 1L, 3600L);
	}

	public static NamelessPlugin getInstance() {
		return instance;
	}
	public static LogManager getLogManager() { return LOG_MANAGER; }
	public static TaskChainFactory getChainFactory() { return CHAIN_FACTORY; }


	public static class SaveConfig implements Runnable {

		@Override
		public void run() {
			final NamelessPlugin plugin = NamelessPlugin.getInstance();
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				for (final Config config : Config.values()) {
					if (config.autoSave()) {
						config.save();
					}
				}
			});
		}

	}

}