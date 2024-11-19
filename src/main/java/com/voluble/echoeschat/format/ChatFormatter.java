package com.voluble.echoeschat.format;

import com.voluble.echoeschat.ChatChannel;
import com.voluble.echoeschat.managers.EmoteColorManager;
import com.voluble.echoeschat.managers.EmoteColorManager.EmoteColorConfig;
import com.voluble.echoeschat.utils.HexColorUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatFormatter {
	private final EmoteColorManager emoteColorManager;
	private final Map<UUID, String> playerEmoteColors;

	public ChatFormatter(EmoteColorManager emoteColorManager, Map<UUID, String> playerEmoteColors) {
		this.emoteColorManager = emoteColorManager;
		this.playerEmoteColors = playerEmoteColors;
	}

	public String formatMessage(Player player, String message, ChatChannel channel) {
		// Apply ALL CAPS to the entire message if required by the channel
		if (channel.isCapitalizeAll()) {
			message = message.toUpperCase();
		}

		if (!channel.usesQuotes()) {
			// Channel doesn't use quotes; process the message as plain text
			String formattedMessage = message.trim();

			if (channel.isAutoFormat()) {
				// Capitalize the first letter and ensure punctuation
				if (!channel.isCapitalizeAll()) {
					formattedMessage = capitalizeFirstLetter(formattedMessage);
				}
				if (!endsWithValidPunctuation(formattedMessage)) {
					formattedMessage += ".";
				}
			}

			return formatWithChannel(player, formattedMessage, channel);
		}

		// Handle quotes if required by the channel
		if (!message.contains("\"")) {
			message = "\"" + message.trim() + "\"";
		}

		return processQuotedMessage(player, message, channel);
	}

	private String processQuotedMessage(Player player, String message, ChatChannel channel) {
		StringBuilder formattedMessage = new StringBuilder();
		boolean insideQuotes = false;
		int currentIndex = 0;
		boolean firstQuotedProcessed = false;

		// Determine quote positions for auto-formatting
		List<Integer> quotePositions = new ArrayList<>();
		int quotePos = message.indexOf("\"");
		while (quotePos != -1) {
			quotePositions.add(quotePos);
			quotePos = message.indexOf("\"", quotePos + 1);
		}

		int lastQuotedStart = -1;
		if (quotePositions.size() >= 2 && quotePositions.size() % 2 == 0) {
			lastQuotedStart = quotePositions.get(quotePositions.size() - 2) + 1;
		}

		while (currentIndex < message.length()) {
			int nextQuoteIndex = message.indexOf("\"", currentIndex);

			if (nextQuoteIndex == -1) {
				// Process remaining text
				String segment = message.substring(currentIndex);
				if (!segment.isEmpty()) {
					if (insideQuotes) {
						formattedMessage.append(processQuotedSegment(segment, channel, firstQuotedProcessed, currentIndex, lastQuotedStart));
						firstQuotedProcessed = true;
					} else {
						if (formattedMessage.length() > 0 && !formattedMessage.toString().endsWith(" ")) {
							formattedMessage.append(" "); // Ensure a space before appending
						}
						formattedMessage.append(formatEmote(player, segment.trim(), channel));
					}
				}
				break;
			} else {
				// Process segment before the quote
				String segment = message.substring(currentIndex, nextQuoteIndex).trim();
				if (!segment.isEmpty()) {
					if (insideQuotes) {
						formattedMessage.append(processQuotedSegment(segment, channel, firstQuotedProcessed, currentIndex, lastQuotedStart));
						firstQuotedProcessed = true;
					} else {
						if (formattedMessage.length() > 0 && !formattedMessage.toString().endsWith(" ")) {
							formattedMessage.append(" "); // Ensure a space before appending
						}
						formattedMessage.append(formatEmote(player, segment, channel));
					}
				}

				// Add the quote, ensuring proper spacing
				if (!insideQuotes && formattedMessage.length() > 0 && !formattedMessage.toString().endsWith(" ")) {
					formattedMessage.append(" "); // Add a space before the opening quote
				}
				formattedMessage.append("\"");
				insideQuotes = !insideQuotes;
				currentIndex = nextQuoteIndex + 1;
			}
		}

		return formatWithChannel(player, formattedMessage.toString().trim(), channel);
	}




	private String processQuotedSegment(String segment, ChatChannel channel, boolean firstQuotedProcessed, int currentIndex, int lastQuotedStart) {
		segment = segment.trim();

		if (channel.isAutoFormat()) {
			if (!channel.isCapitalizeAll() && !firstQuotedProcessed) {
				segment = capitalizeFirstLetter(segment);
			}
			if (lastQuotedStart != -1 && currentIndex >= lastQuotedStart && !endsWithValidPunctuation(segment)) {
				segment += ".";
			}
		}

		return segment;
	}

	private String formatEmote(Player player, String emote, ChatChannel channel) {
		if (emote.isEmpty()) return "";

		// Apply ALL CAPS to emotes if 'capitalizeAll' is enabled
		if (channel.isCapitalizeAll()) {
			emote = emote.toUpperCase();
		}

		// Retrieve the player's selected emote color
		String colorCode = getPlayerEmoteColor(player); // Retrieve hex or regular color
		if (colorCode == null) {
			colorCode = "&7"; // Default to gray
		}

		// Translate color codes (& for standard, &# for hex)
		String formattedColor = HexColorUtil.applyHexColors(colorCode);

		// Return the formatted emote
		return formattedColor + emote + ChatColor.RESET;
	}

	private String formatWithChannel(Player player, String message, ChatChannel channel) {
		return channel.getFormat(player)
				.replace("{prefix}", channel.getPrefix().isEmpty() ? "" : channel.getPrefix() + " ")
				.replace("{player}", player.getDisplayName())
				.replace("{message}", message);
	}

	private boolean endsWithValidPunctuation(String text) {
		return text.endsWith(".") || text.endsWith("!") || text.endsWith("?");
	}

	private String capitalizeFirstLetter(String text) {
		if (text == null || text.isEmpty()) return text;
		return Character.toUpperCase(text.charAt(0)) + text.substring(1);
	}

	private String getPlayerEmoteColor(Player player) {
		// Get the player's UUID
		UUID playerUUID = player.getUniqueId();

		// Check if the player has a specific emote color set
		String colorName = playerEmoteColors.get(playerUUID);
		if (colorName != null) {
			// Retrieve the EmoteColorConfig for the color
			EmoteColorConfig colorConfig = emoteColorManager.getEmoteColor(colorName);
			if (colorConfig != null) {
				return colorConfig.getColor(); // Return the color code (e.g., hex or & color)
			}
		}

		// Fallback to the default emote color
		EmoteColorConfig defaultColorConfig = emoteColorManager.getDefaultColor();
		if (defaultColorConfig != null) {
			return defaultColorConfig.getColor(); // Default color code
		}

		// Final fallback: return gray color code
		return "&7";
	}
}