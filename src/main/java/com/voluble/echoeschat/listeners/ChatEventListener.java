package com.voluble.echoeschat.listeners;

import com.voluble.echoeschat.ChatChannel;
import com.voluble.echoeschat.managers.ChannelManager;
import com.voluble.echoeschat.format.ChatFormatter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEventListener implements Listener {
	private final ChannelManager channelManager;
	private final ChatFormatter chatFormatter;

	public ChatEventListener(ChannelManager channelManager, ChatFormatter chatFormatter) {
		this.channelManager = channelManager;
		this.chatFormatter = chatFormatter;
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage();

		// Get the player's active channel
		ChatChannel activeChannel = channelManager.getPlayerChannel(player);
		if (activeChannel == null) {
			activeChannel = channelManager.getDefaultChannel(); // Fallback to the default channel
		}

		// Check if the channel is enabled
		if (!activeChannel.isEnabled()) {
			player.sendMessage(ChatColor.RED + "The channel " + activeChannel.getName() + " is currently disabled.");
			event.setCancelled(true);
			return;
		}

		// Format the message using ChatFormatter
		String formattedMessage = chatFormatter.formatMessage(player, message, activeChannel);

		// Broadcast the formatted message based on the channel's range
		if (activeChannel.getRange() == -1) {
			// Global broadcast
			Bukkit.broadcastMessage(formattedMessage);
		} else {
			// Use broadcastToRange and ensure proper notification
			channelManager.broadcastToRange(player, formattedMessage, activeChannel.getRange());
		}

		event.setCancelled(true);
	}
}
