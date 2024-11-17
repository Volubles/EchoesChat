package com.voluble.echoeschat.listeners;

import com.voluble.echoeschat.ChatChannel;
import com.voluble.echoeschat.managers.ChannelManager;
import com.voluble.echoeschat.managers.EmoteColorManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;
import java.util.UUID;

public class PlayerJoinListener implements Listener {
	private final ChannelManager channelManager;
	private final EmoteColorManager emoteColorManager;
	private final Map<UUID, String> playerEmoteColors;

	public PlayerJoinListener(ChannelManager channelManager, EmoteColorManager emoteColorManager, Map<UUID, String> playerEmoteColors) {
		this.channelManager = channelManager;
		this.emoteColorManager = emoteColorManager;
		this.playerEmoteColors = playerEmoteColors;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		// Assign player to the default channel
		ChatChannel defaultChannel = channelManager.getDefaultChannel();
		if (defaultChannel != null) {
			channelManager.setPlayerChannel(event.getPlayer(), defaultChannel);
		}

		// Initialize the player's emote color if not already set
		UUID playerUUID = event.getPlayer().getUniqueId();
		if (!playerEmoteColors.containsKey(playerUUID)) {
			String defaultColor = emoteColorManager.getDefaultColor().getColor();
			playerEmoteColors.put(playerUUID, defaultColor);
		}
	}
}
