package com.voluble.echoeschat.commands;

import com.voluble.echoeschat.ChatChannel;
import com.voluble.echoeschat.format.ChatFormatter;
import com.voluble.echoeschat.managers.ChannelManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChannelCommand implements CommandExecutor {
	private final ChatChannel channel;
	private final ChannelManager channelManager;
	private final ChatFormatter chatFormatter;

	public ChannelCommand(ChatChannel channel, ChannelManager channelManager, ChatFormatter chatFormatter) {
		this.channel = channel;
		this.channelManager = channelManager;
		this.chatFormatter = chatFormatter;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can use this command.");
			return true;
		}

		Player player = (Player) sender;

		// Check if the channel is enabled
		if (!channel.isEnabled()) {
			player.sendMessage("The " + channel.getName() + " channel is currently disabled.");
			return true;
		}

		// Check write permission when switching to the channel
		if (!player.hasPermission(channel.getWritePermission())) {
			player.sendMessage("You do not have permission to send messages in the " + channel.getName() + " channel.");
			return true;
		}

		if (args.length == 0) {
			// Switch to the channel
			channelManager.setPlayerChannel(player, channel);
			player.sendMessage("You are now chatting in the " + channel.getName() + " channel.");
			return true;
		}

		// Process and send a message in the channel
		String rawMessage = String.join(" ", args);

		// Check if the player has muted the channel
		if (channelManager.isChannelMuted(player, channel.getName())) {
			channelManager.unmuteChannel(player, channel.getName());
			player.sendMessage("The channel was muted, but it has been unmuted since you're sending a message.");
		}

		// Use ChatFormatter to format the message
		String formattedMessage = chatFormatter.formatMessage(player, rawMessage, channel);

		// Broadcast the formatted message with mute and range checks
		Bukkit.getOnlinePlayers().forEach(recipient -> {
			boolean isMuted = channelManager.isChannelMuted(recipient, channel.getName().toLowerCase());

			// Skip recipients who have muted the channel
			if (isMuted) return;

			if (channel.getRange() == -1 ||
					(recipient.getWorld().equals(player.getWorld()) &&
							recipient.getLocation().distance(player.getLocation()) <= channel.getRange())) {
				recipient.sendMessage(formattedMessage);
			}
		});

		return true;
	}
}
