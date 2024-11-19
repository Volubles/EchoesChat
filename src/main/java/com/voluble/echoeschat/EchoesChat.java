package com.voluble.echoeschat;

import com.voluble.echoeschat.commands.ChannelCommand;
import com.voluble.echoeschat.commands.ChatCommand;
import com.voluble.echoeschat.commands.ChatTabCompleter;
import com.voluble.echoeschat.format.ChatFormatter;
import com.voluble.echoeschat.listeners.ChatEventListener;
import com.voluble.echoeschat.listeners.PlayerJoinListener;
import com.voluble.echoeschat.managers.ChannelManager;
import com.voluble.echoeschat.managers.EmoteColorManager;
import com.voluble.echoeschat.utils.CommandRegistrar;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class EchoesChat extends JavaPlugin {

	// Core managers for the plugin
	private ChannelManager channelManager;
	private ChatFormatter chatFormatter;
	private EmoteColorManager emoteColorManager;

	// Data configuration for saving player-specific data
	private FileConfiguration dataConfig;
	private File dataFile;
	private ChatEventListener chatEventListener;

	// Stores player UUIDs and their selected emote colors
	private final Map<UUID, String> playerEmoteColors = new HashMap<>();

	@Override
	public void onEnable() {
		getLogger().info("EchoesChat has been enabled!");
		saveDefaultConfig();
		channelManager = new ChannelManager(this);
		emoteColorManager = new EmoteColorManager(getConfig());
		// Load default configurations and initialize the plugin
		loadConfigurations();
		initializeComponents();
		registerCommands();
		registerListeners();

		// Load data (e.g., player emote colors) from the data.yml file
		loadDataConfig();
		loadPlayerEmoteColors();
	}

	@Override
	public void onDisable() {
		getLogger().info("EchoesChat has been disabled!");

		// Save player-specific data to the data.yml file
		savePlayerEmoteColors();
	}

	/**
	 * Loads the default configuration files for the plugin.
	 * - Creates config.yml and channels.yml if they do not exist.
	 */
	private void loadConfigurations() {
		saveDefaultConfig(); // Creates config.yml if it doesn't exist
		saveResource("channels.yml", false); // Creates channels.yml if it doesn't exist
	}

	/**
	 * Initializes core components such as managers and utilities.
	 */
	private void initializeComponents() {
		// Initialize ChannelManager to manage chat channels
		channelManager = new ChannelManager(this);

		// Initialize EmoteColorManager to handle emote color configuration
		emoteColorManager = new EmoteColorManager(getConfig());

		// Initialize ChatFormatter to handle message formatting with emote colors
		chatFormatter = new ChatFormatter(emoteColorManager, playerEmoteColors);

		// Log the loaded channels for debugging purposes
		getLogger().info("Loaded channels: " + channelManager.getAllChannels().keySet());
		boolean isPlaceholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
	}

	/**
	 * Registers all commands for the plugin.
	 * - Dynamic commands for chat channels.
	 * - The main /chat command with subcommands.
	 */
	private void registerCommands() {
		registerDynamicChannelCommands(); // Register channel-based commands
		registerChatCommand(); // Register the main /chat command
	}

	/**
	 * Registers dynamic commands for each enabled chat channel.
	 */
	private void registerDynamicChannelCommands() {
		for (ChatChannel channel : channelManager.getAllChannels().values()) {
			if (channel.isEnabled()) {
				for (String command : channel.getCommands()) {
					String commandName = command.replace("/", ""); // Clean up command name
					BukkitCommand dynamicCommand = createDynamicChannelCommand(channel, commandName);

					// Register the dynamic command
					CommandRegistrar.registerCommand(commandName, "echoeschat", dynamicCommand);
				}
			}
		}
	}

	/**
	 * Registers the main /chat command and its subcommands.
	 */
	private void registerChatCommand() {
		BukkitCommand chatCommand = new BukkitCommand("chat") {
			@Override
			public boolean execute(CommandSender sender, String label, String[] args) {
				// Delegate the entire command execution to ChatCommand
				return new ChatCommand(EchoesChat.this, emoteColorManager, playerEmoteColors, channelManager).onCommand(sender, this, label, args);
			}

			@Override
			public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
				ChatTabCompleter tabCompleter = new ChatTabCompleter(emoteColorManager, channelManager);
				return tabCompleter.onTabComplete(sender, this, alias, args);
			}
		};

		chatCommand.setDescription("Primary command for EchoesChat.");
		chatCommand.setAliases(Collections.singletonList("ec")); // Optional alias for the command
		CommandRegistrar.registerCommand("chat", "echoeschat", chatCommand);
	}

	/**
	 * Creates dynamic channel commands for each chat channel.
	 */
	private BukkitCommand createDynamicChannelCommand(ChatChannel channel, String commandName) {
		return new BukkitCommand(commandName) {
			@Override
			public boolean execute(CommandSender sender, String label, String[] args) {
				// Create a new ChannelCommand instance and delegate the command execution
				return new ChannelCommand(channel, channelManager, chatFormatter)
						.onCommand(sender, this, label, args);
			}

			@Override
			public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
				if (!(sender instanceof Player)) {
					return Collections.emptyList(); // Tab completion is only available for players
				}

				Player player = (Player) sender;

				// Use read permission to suggest channels
				if (!player.hasPermission(channel.getReadPermission())) {
					return Collections.emptyList(); // Restrict tab completion for unauthorized players
				}

				// Additional filtering logic could be added here if necessary
				return Collections.emptyList(); // Provide appropriate tab completions if needed
			}

			@Override
			public boolean testPermissionSilent(CommandSender sender) {
				// Check if the sender has either read or write permission for the channel
				if (sender instanceof Player) {
					Player player = (Player) sender;
					return player.hasPermission(channel.getReadPermission()) || player.hasPermission(channel.getWritePermission());
				}
				return false;
			}

		};
	}

	/**
	 * Registers event listeners for the plugin.
	 */
	private void registerListeners() {
		// Correct order: EchoesChat (this), ChannelManager, ChatFormatter
		getServer().getPluginManager().registerEvents(new ChatEventListener(this, channelManager, chatFormatter), this);
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(channelManager, emoteColorManager, playerEmoteColors), this);
	}

	/**
	 * Loads the data.yml file into memory.
	 */
	public void loadDataConfig() {
		dataFile = new File(getDataFolder(), "data.yml");
		if (!dataFile.exists()) {
			try {
				dataFile.getParentFile().mkdirs();
				dataFile.createNewFile();
			} catch (IOException e) {
				getLogger().severe("Could not create data.yml!");
				e.printStackTrace();
			}
		}
		dataConfig = YamlConfiguration.loadConfiguration(dataFile);
	}

	/**
	 * Saves the current state of the data.yml file to disk.
	 */
	public void saveDataConfig() {
		try {
			dataConfig.save(dataFile);
		} catch (IOException e) {
			getLogger().severe("Could not save data.yml!");
			e.printStackTrace();
		}
	}

	/**
	 * Saves player emote colors to the data.yml file.
	 */
	public void savePlayerEmoteColors() {
		for (Map.Entry<UUID, String> entry : playerEmoteColors.entrySet()) {
			dataConfig.set("playerEmoteColors." + entry.getKey().toString(), entry.getValue());
		}
		saveDataConfig();
	}

	/**
	 * Loads player emote colors from the data.yml file.
	 */
	public void loadPlayerEmoteColors() {
		if (dataConfig.contains("playerEmoteColors")) {
			for (String key : dataConfig.getConfigurationSection("playerEmoteColors").getKeys(false)) {
				UUID uuid = UUID.fromString(key);
				String color = dataConfig.getString("playerEmoteColors." + key);
				playerEmoteColors.put(uuid, color);
			}
		}
	}

	// Accessor methods for other classes
	public ChannelManager getChannelManager() {
		return channelManager;
	}

	public ChatFormatter getChatFormatter() {
		return chatFormatter;
	}

	public EmoteColorManager getEmoteColorManager() {
		return emoteColorManager;
	}

	public FileConfiguration getDataConfig() {
		return dataConfig;
	}

	public Map<UUID, String> getPlayerEmoteColors() {
		return playerEmoteColors;
	}
}