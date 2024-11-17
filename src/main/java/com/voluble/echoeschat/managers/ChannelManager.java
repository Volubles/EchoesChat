package com.voluble.echoeschat.managers;

import com.voluble.echoeschat.ChatChannel;
import com.voluble.echoeschat.EchoesChat;
import com.voluble.echoeschat.utils.HexColorUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelManager {
	private final EchoesChat plugin;
	private final Map<String, ChatChannel> channels = new HashMap<>();
	private final Map<Player, ChatChannel> playerChannels = new HashMap<>();

	public ChannelManager(EchoesChat plugin) {
		this.plugin = plugin;
		loadChannels();
	}

	public void reloadChannels(FileConfiguration config) {
		channels.clear(); // Clear the existing channels
		playerChannels.clear(); // Clear player-specific channels
		loadChannels(); // Reload channels from the channels.yml file
		plugin.getLogger().info("Channels have been reloaded.");
	}

	private void loadChannels() {
		// Locate the channels.yml file
		File channelFile = new File(plugin.getDataFolder(), "channels.yml");

		// Check if the file exists
		if (!channelFile.exists()) {
			plugin.saveResource("channels.yml", false); // Save the default file if it doesn't exist
			plugin.getLogger().info("channels.yml file created as it was missing.");
		} else {
			plugin.getLogger().info("channels.yml file found. No changes made.");
		}

		// Load the configuration from the file
		FileConfiguration channelConfig = YamlConfiguration.loadConfiguration(channelFile);

		// Iterate through the channels defined in channels.yml
		for (String channelName : channelConfig.getKeys(false)) {
			ConfigurationSection channelSection = channelConfig.getConfigurationSection(channelName);

			if (channelSection != null) {
				// Load properties of the channel
				String format = channelSection.getString("format", "{player}: \"&f{message}&f\"");
				int range = channelSection.getInt("range", -1);
				String permission = channelSection.getString("permission", "");
				boolean isDefault = channelSection.getBoolean("default", false);
				boolean isEnabled = channelSection.getBoolean("enabled", true);
				String prefix = channelSection.getString("prefix", ""); // Load prefix from channels.yml
				List<String> commands = channelSection.getStringList("commands");
				boolean quotes = channelSection.getBoolean("quotes", true); // Default to true if not specified
				boolean autoFormat = channelSection.getBoolean("autoFormat", true);
				boolean capitalizeAll = channelSection.getBoolean("capitalizeAll", false); // Default to false
				boolean allowsEmotes = channelSection.getBoolean("allowsEmotes", true); // Default to true if not specified

				ChatChannel channel = new ChatChannel(channelName, format, range, permission, isDefault, isEnabled, commands, prefix, quotes, autoFormat, capitalizeAll, allowsEmotes);
				channels.put(channelName, channel);
			}
		}

		// Log the loaded channels
		plugin.getLogger().info("Loaded channels: " + channels.keySet());
	}

	// Set the player's active channel
	public void setPlayerChannel(Player player, ChatChannel channel) {
		playerChannels.put(player, channel);
	}

	// Get the player's active channel
	public ChatChannel getPlayerChannel(Player player) {
		return playerChannels.get(player);
	}

	// Get the default channel
	public ChatChannel getDefaultChannel() {
		return channels.values().stream()
				.filter(ChatChannel::isDefault)
				.findFirst()
				.orElse(null);
	}

	// Get all channels
	public Map<String, ChatChannel> getAllChannels() {
		return channels;
	}

	public boolean broadcastToRange(Player sender, String formattedMessage, int range) {
		// Check if any player (other than the sender) is within range
		boolean someoneHeard = sender.getWorld().getPlayers().stream()
				.filter(player -> !player.equals(sender)) // Exclude the sender
				.filter(player -> player.getLocation().distance(sender.getLocation()) <= range)
				.peek(player -> player.sendMessage(formattedMessage)) // Send the message to players in range
				.findAny() // Check if any players were found
				.isPresent();

		// Notify the sender if no one heard them
		if (!someoneHeard) {
			sender.sendMessage(HexColorUtil.applyHexColors("&#F05A7ENobody could hear you..."));
		}

		// Send the message to the sender regardless
		sender.sendMessage(formattedMessage);

		return someoneHeard;
	}
}
