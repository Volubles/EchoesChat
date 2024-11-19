package com.voluble.echoeschat.managers;

import com.voluble.echoeschat.ChatChannel;
import com.voluble.echoeschat.EchoesChat;
import com.voluble.echoeschat.utils.HexColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChannelManager {
	private final EchoesChat plugin;
	private final Map<String, ChatChannel> channels = new HashMap<>();
	private final Map<Player, ChatChannel> playerChannels = new HashMap<>();
	private final Map<UUID, List<String>> mutedChannels = new ConcurrentHashMap<>();

	public ChannelManager(EchoesChat plugin) {
		this.plugin = plugin;
		loadChannels();
	}

	public Map<UUID, List<String>> getMutedChannels() {
		return mutedChannels;
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
				String readPermission = channelSection.getString("readPermission", "");
				String writePermission = channelSection.getString("writePermission", "");
				boolean isDefault = channelSection.getBoolean("default", false);
				boolean isEnabled = channelSection.getBoolean("enabled", true);
				String prefix = channelSection.getString("prefix", "");
				List<String> commands = channelSection.getStringList("commands");
				boolean quotes = channelSection.getBoolean("quotes", true);
				boolean autoFormat = channelSection.getBoolean("autoFormat", true);
				boolean capitalizeAll = channelSection.getBoolean("capitalizeAll", false);
				boolean allowsEmotes = channelSection.getBoolean("allowsEmotes", true);

				// Convert channelName to lowercase
				String lowerCaseChannelName = channelName.toLowerCase();

				ChatChannel channel = new ChatChannel(lowerCaseChannelName, format, range, readPermission, writePermission, isDefault, isEnabled, commands, prefix, quotes, autoFormat, capitalizeAll, allowsEmotes);

				// Store channel with lowercase name as key
				channels.put(lowerCaseChannelName, channel);
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
		return channels != null ? channels : Collections.emptyMap();
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

	// Method to mute the channel
	public void muteChannel(Player player, String channelName) {
		UUID playerUUID = player.getUniqueId();

		// Debug: Log current mutedChannels map
		System.out.println("Current mutedChannels: " + mutedChannels);

		// Convert channelName to lowercase
		String lowerCaseChannelName = channelName.toLowerCase();

		// Ensure the channel exists
		ChatChannel channel = channels.get(lowerCaseChannelName);
		if (channel == null) {
			player.sendMessage(ChatColor.RED + "Channel '" + channelName + "' does not exist.");
			return;
		}

		// Initialize the player's muted channels list if absent
		mutedChannels.putIfAbsent(playerUUID, new CopyOnWriteArrayList<>());
		List<String> playerMutedChannels = mutedChannels.get(playerUUID);

		if (!playerMutedChannels.contains(lowerCaseChannelName)) {
			playerMutedChannels.add(lowerCaseChannelName);
		} else {
			player.sendMessage(ChatColor.RED + "Channel '" + channelName + "' is already muted.");
		}

		// Debug: Log updated mutedChannels map
		System.out.println("Updated mutedChannels: " + mutedChannels);
	}



	public void unmuteChannel(Player player, String channelName) {
		UUID playerUUID = player.getUniqueId();

		// Ensure the channel exists
		ChatChannel channel = channels.get(channelName.toLowerCase());
		if (channel == null) {
			player.sendMessage(ChatColor.RED + "Channel '" + channelName + "' does not exist.");
			return;
		}

		List<String> playerMutedChannels = mutedChannels.get(playerUUID);

		if (playerMutedChannels != null && playerMutedChannels.remove(channelName.toLowerCase())) {

			// Remove the entry if the list is empty
			if (playerMutedChannels.isEmpty()) {
				mutedChannels.remove(playerUUID);
			}
		} else {
			player.sendMessage(ChatColor.RED + "Channel '" + channelName + "' is not muted.");
		}
	}



	// Method to check if a channel is muted for a player
	public boolean isChannelMuted(Player player, String channelName) {
		List<String> playerMutedChannels = mutedChannels.get(player.getUniqueId());

		// Debugging: Show the current state of muted channels for this player
		System.out.println("Muted channels for " + player.getName() + ": " + (playerMutedChannels != null ? playerMutedChannels : "None"));

		// Ensure case consistency for channel names
		return playerMutedChannels != null && playerMutedChannels.contains(channelName.toLowerCase());
	}

	public List<String> getMutedChannelsForDebugging(UUID playerUUID) {
		return mutedChannels.getOrDefault(playerUUID, Collections.emptyList());
	}
}