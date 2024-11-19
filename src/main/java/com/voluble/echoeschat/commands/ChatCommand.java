package com.voluble.echoeschat.commands;

import com.voluble.echoeschat.ChatChannel;
import com.voluble.echoeschat.EchoesChat;
import com.voluble.echoeschat.managers.ChannelManager;
import com.voluble.echoeschat.managers.EmoteColorManager;
import com.voluble.echoeschat.managers.EmoteColorManager.EmoteColorConfig;
import com.voluble.echoeschat.utils.HexColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ChatCommand implements CommandExecutor {
	private final EchoesChat plugin;
	private final EmoteColorManager emoteColorManager;
	private final Map<UUID, String> playerEmoteColors;
	private final ChannelManager channelManager;

	/**
	 * Constructor for the ChatCommand class.
	 *
	 * @param plugin            The main plugin instance.
	 * @param emoteColorManager The EmoteColorManager to handle emote color configurations.
	 * @param playerEmoteColors A map storing player-specific emote colors by their UUID.
	 * @param channelManager    The ChannelManager for managing chat channels.
	 */
	public ChatCommand(EchoesChat plugin, EmoteColorManager emoteColorManager, Map<UUID, String> playerEmoteColors, ChannelManager channelManager) {
		this.plugin = plugin;
		this.emoteColorManager = emoteColorManager;
		this.playerEmoteColors = playerEmoteColors;
		this.channelManager = channelManager;
	}

	/**
	 * Executes the /chat command.
	 *
	 * @param sender  The command sender (e.g., player or console).
	 * @param command The command being executed.
	 * @param label   The alias used for the command.
	 * @param args    The command arguments.
	 * @return True if the command was successfully executed, false otherwise.
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Ensure the sender is a player
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can use this command.");
			return false;
		}

		Player player = (Player) sender;

		if (args.length > 0 && args[0].equalsIgnoreCase("emotecolor")) {
			if (args.length == 1) {
				// Display the available emote colors
				player.sendMessage(getAvailableColorsMessage(player));
				return true;
			}

			// Handle color change logic for /chat emotecolor <color>
			String colorName = args[1].toLowerCase();
			EmoteColorConfig colorConfig = emoteColorManager.getEmoteColor(colorName);

			if (colorConfig == null) {
				player.sendMessage(ChatColor.RED + "Invalid emote color. Use /chat emotecolor to see available options.");
				return false;
			}

			if (!player.hasPermission(colorConfig.getPermission())) {
				player.sendMessage(ChatColor.RED + colorConfig.getDeniedMessage());
				return false;
			}

			playerEmoteColors.put(player.getUniqueId(), colorName);
			player.sendMessage(HexColorUtil.applyHexColors("&7Your emote color has been set to "
					+ HexColorUtil.applyHexColors(colorConfig.getColor())
					+ colorName + ChatColor.RESET + "!"));
			return true;
		}

		// Single argument
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("reload")) {
				// Handle /chat reload
				if (!player.hasPermission("echoeschat.reload")) {
					player.sendMessage(ChatColor.RED + "You do not have permission to reload the plugin.");
					return false;
				}

				// Reload plugin configurations and data
				plugin.reloadConfig();
				plugin.getChannelManager().reloadChannels(plugin.getConfig());
				plugin.getEmoteColorManager().reloadColors(plugin.getConfig());
				plugin.loadDataConfig();
				plugin.loadPlayerEmoteColors();
				player.sendMessage(ChatColor.GREEN + "EchoesChat has been reloaded!");
				return true;
			}
		}

		// Handle mute and unmute commands
		if (args[0].equalsIgnoreCase("mutechannel") || args[0].equalsIgnoreCase("unmutechannel")) {
			if (args.length < 2) {
				player.sendMessage(ChatColor.RED + "Usage: /chat " + args[0].toLowerCase() + " <channel_name>");
				return true;
			}

			String channelName = args[1].toLowerCase(); // Ensure case-insensitivity
			ChatChannel channel = channelManager.getAllChannels().get(channelName);

			if (channel == null) {
				player.sendMessage(ChatColor.RED + "Channel '" + args[1] + "' does not exist. Available channels: " +
						String.join(", ", channelManager.getAllChannels().keySet()));
				return true;
			}

			// Check if the player has read permission for the channel
			if (!player.hasPermission(channel.getReadPermission())) {
				player.sendMessage(ChatColor.RED + "You do not have permission to interact with the channel: " + channelName);
				return true;
			}

			if (args[0].equalsIgnoreCase("mutechannel")) {
				if (channelManager.isChannelMuted(player, channelName)) {
					player.sendMessage(ChatColor.RED + "Channel '" + channelName + "' is already muted.");
				} else {
					channelManager.muteChannel(player, channelName);
					player.sendMessage(ChatColor.YELLOW + "You have muted the channel: " + channelName);
				}
			} else if (args[0].equalsIgnoreCase("unmutechannel")) {
				if (!channelManager.isChannelMuted(player, channelName)) {
					player.sendMessage(ChatColor.RED + "Channel '" + channelName + "' is not muted.");
				} else {
					channelManager.unmuteChannel(player, channelName);
					player.sendMessage(ChatColor.GREEN + "You have unmuted the channel: " + channelName);
				}
			}

			return true;
		}

		// Invalid usage
		player.sendMessage(ChatColor.RED + "Usage: /chat [emotecolor|reload|mutechannel|unmutechannel]");
		return false;
	}

	/**
	 * Builds a message listing all available emote colors for the player.
	 *
	 * @param player The player requesting the list of available emote colors.
	 * @return A formatted string of available emote colors.
	 */
	private String getAvailableColorsMessage(Player player) {
		StringBuilder message = new StringBuilder(ChatColor.GRAY + "Available Emote Colors: " + ChatColor.RESET);

		for (Map.Entry<String, EmoteColorConfig> entry : emoteColorManager.getAllColors().entrySet()) {
			String colorName = entry.getKey();
			EmoteColorConfig colorConfig = entry.getValue();
			String colorCode = colorConfig.getColor();

			// Skip if color name or code is null
			if (colorCode == null || colorName == null) continue;

			// Add color to the message if the player has permission
			if (player.hasPermission(colorConfig.getPermission())) {
				message.append(HexColorUtil.applyHexColors(colorCode))
						.append(colorName)
						.append(ChatColor.RESET)
						.append(", ");
			}
		}

		// Remove the trailing comma and space
		if (message.length() > 2) {
			message.setLength(message.length() - 2);
		}

		return message.toString();
	}
}
