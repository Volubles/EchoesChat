package com.voluble.echoeschat.commands;

import com.voluble.echoeschat.ChatChannel;
import com.voluble.echoeschat.format.ChatFormatter;
import com.voluble.echoeschat.managers.ChannelManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChannelCommand implements CommandExecutor {
	private final ChatChannel channel;
	private final ChannelManager channelManager;
	private final ChatFormatter chatFormatter; // Add the ChatFormatter

	public ChannelCommand(ChatChannel channel, ChannelManager channelManager, ChatFormatter chatFormatter) {
		this.channel = channel;
		this.channelManager = channelManager;
		this.chatFormatter = chatFormatter; // Initialize ChatFormatter
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can use this command.");
			return true;
		}

		Player player = (Player) sender;

		// Check permissions
		if (!player.hasPermission(channel.getPermission())) {
			player.sendMessage("You do not have permission to use this channel.");
			return true;
		}

		if (args.length == 0) {
			// Switch to the channel
			channelManager.setPlayerChannel(player, channel);
			player.sendMessage("You are now chatting in the " + channel.getName() + " channel.");
		} else {
			// Send a message to the channel
			String rawMessage = String.join(" ", args);

			// Use ChatFormatter to format the message
			String formattedMessage = chatFormatter.formatMessage(player, rawMessage, channel);

			// Broadcast the formatted message
			if (channel.getRange() == -1) {
				player.getServer().broadcastMessage(formattedMessage);
			} else {
				channelManager.broadcastToRange(player, formattedMessage, channel.getRange());
			}
		}

		return true;
	}
}
