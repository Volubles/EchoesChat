package com.voluble.echoeschat.listeners;

import com.voluble.echoeschat.ChatChannel;
import com.voluble.echoeschat.EchoesChat;
import com.voluble.echoeschat.managers.ChannelManager;
import com.voluble.echoeschat.format.ChatFormatter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEventListener implements Listener {
	private final EchoesChat plugin;
	private final ChannelManager channelManager;
	private final ChatFormatter chatFormatter;

	public ChatEventListener(EchoesChat plugin, ChannelManager channelManager, ChatFormatter chatFormatter) {
		this.plugin = plugin;
		this.channelManager = channelManager;
		this.chatFormatter = chatFormatter;
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player sender = event.getPlayer();
		String message = event.getMessage();

		// Cancel the event to prevent default broadcasting
		event.setCancelled(true);

		// Schedule the broadcasting task on the main thread
		Bukkit.getScheduler().runTask(plugin, () -> {
			// Get the sender's active channel
			ChatChannel activeChannel = channelManager.getPlayerChannel(sender);
			if (activeChannel == null) {
				activeChannel = channelManager.getDefaultChannel(); // Fallback to the default channel
			}

			// Check if the channel is enabled
			if (!activeChannel.isEnabled()) {
				sender.sendMessage(ChatColor.RED + "The channel '" + activeChannel.getName() + "' is currently disabled.");
				return;
			}

			// Format the message
			String formattedMessage = chatFormatter.formatMessage(sender, message, activeChannel);

			// Broadcast the message based on range and muted channels
			for (Player recipient : Bukkit.getOnlinePlayers()) {
				// Check if the recipient has muted the channel
				boolean isMuted = channelManager.isChannelMuted(recipient, activeChannel.getName().toLowerCase());

				// Skip recipients who have muted the channel
				if (isMuted) {
					continue;
				}

				// Handle global and range-based broadcasts
				if (activeChannel.getRange() == -1) {
					recipient.sendMessage(formattedMessage); // Global broadcast
				} else if (recipient.getWorld().equals(sender.getWorld())
						&& recipient.getLocation().distance(sender.getLocation()) <= activeChannel.getRange()) {
					recipient.sendMessage(formattedMessage); // Range-based broadcast
				}
			}
		});
	}
}