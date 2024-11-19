package com.voluble.echoeschat.commands;

import com.voluble.echoeschat.ChatChannel;
import com.voluble.echoeschat.managers.ChannelManager;
import com.voluble.echoeschat.managers.EmoteColorManager;
import com.voluble.echoeschat.managers.EmoteColorManager.EmoteColorConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ChatTabCompleter implements TabCompleter {

	private final EmoteColorManager emoteColorManager;
	private final ChannelManager channelManager;

	public ChatTabCompleter(EmoteColorManager emoteColorManager, ChannelManager channelManager) {
		this.emoteColorManager = emoteColorManager;
		this.channelManager = channelManager;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!(sender instanceof Player)) {
			return Collections.emptyList(); // No tab completion for non-players
		}

		Player player = (Player) sender;

		if (args.length == 1) {
			List<String> suggestions = new ArrayList<>();

			// Suggest commands
			if ("emotecolor".startsWith(args[0].toLowerCase())) {
				suggestions.add("emotecolor");
			}
			if ("reload".startsWith(args[0].toLowerCase()) && player.hasPermission("echoeschat.reload")) {
				suggestions.add("reload");
			}
			if ("mutechannel".startsWith(args[0].toLowerCase())) {
				suggestions.add("mutechannel");
			}
			if ("unmutechannel".startsWith(args[0].toLowerCase())) {
				suggestions.add("unmutechannel");
			}

			return suggestions;
		}

		// Handle /chat emotecolor command
		if (args.length == 2 && args[0].equalsIgnoreCase("emotecolor")) {
			List<String> suggestions = new ArrayList<>();

			// Add all emote colors the player has permission for
			for (Map.Entry<String, EmoteColorConfig> entry : emoteColorManager.getAllColors().entrySet()) {
				String colorName = entry.getKey();
				EmoteColorConfig colorConfig = entry.getValue();

				if (player.hasPermission(colorConfig.getPermission()) && colorName.toLowerCase().startsWith(args[1].toLowerCase())) {
					suggestions.add(colorName);
				}
			}

			return suggestions;
		}

		// Tab completion for /chat mutechannel or /chat unmutechannel
		if (args.length == 2 && (args[0].equalsIgnoreCase("mutechannel") || args[0].equalsIgnoreCase("unmutechannel"))) {
			List<String> suggestions = new ArrayList<>();

			// Suggest channels based on read permission
			for (Map.Entry<String, ChatChannel> entry : channelManager.getAllChannels().entrySet()) {
				String channelName = entry.getKey();
				ChatChannel channel = entry.getValue();

				// Check if the player has read permission for the channel
				if (player.hasPermission(channel.getReadPermission()) && channelName.toLowerCase().startsWith(args[1].toLowerCase())) {
					suggestions.add(channelName);
				}
			}

			return suggestions;
		}

		// Tab completion for channels
		if (args.length == 2 && args[0].equalsIgnoreCase("channel")) {
			List<String> suggestions = new ArrayList<>();

			// Suggest channels based on write permission
			for (Map.Entry<String, ChatChannel> entry : channelManager.getAllChannels().entrySet()) {
				String channelName = entry.getKey();
				ChatChannel channel = entry.getValue();

				// Check if the player has write permission for the channel
				if (player.hasPermission(channel.getWritePermission()) && channelName.toLowerCase().startsWith(args[1].toLowerCase())) {
					suggestions.add(channelName);
				}
			}

			return suggestions;
		}

		return Collections.emptyList(); // No other arguments are handled
	}
}
