package com.voluble.echoeschat.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexColorUtil {

	// Pattern to match hex color codes (&#RRGGBB) and standard Minecraft color codes (&a)
	private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#[A-Fa-f0-9]{6}");
	private static final Pattern STANDARD_COLOR_PATTERN = Pattern.compile("&[0-9a-fA-F]");

	/**
	 * Applies hex colors and standard Minecraft color codes to a message.
	 *
	 * @param message The input message with hex color codes (e.g., &#FF5733) or standard color codes (e.g., &a).
	 * @return The message with applied colors.
	 */
	public static String applyHexColors(String message) {
		if (message == null || message.isEmpty()) {
			return message; // Return as-is if the input is null or empty
		}

		// First, apply hex color codes (&#RRGGBB)
		message = applyHexColorCodes(message);

		// Then, translate standard Minecraft color codes (&a, &b, etc.)
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	/**
	 * Applies hex color codes (&#RRGGBB) to the message.
	 *
	 * @param message The input message with hex color codes.
	 * @return The message with hex colors applied.
	 */
	private static String applyHexColorCodes(String message) {
		Matcher matcher = HEX_COLOR_PATTERN.matcher(message);
		StringBuffer buffer = new StringBuffer();

		while (matcher.find()) {
			String hexCode = matcher.group(); // e.g., &#FF5733
			try {
				// Remove the '&#' prefix and apply the hex color
				matcher.appendReplacement(buffer, ChatColor.of(hexCode.substring(1)).toString());
			} catch (IllegalArgumentException e) {
				// Log invalid hex color codes if needed
				System.out.println("Invalid hex color code detected: " + hexCode);
				matcher.appendReplacement(buffer, ""); // Replace invalid codes with an empty string
			}
		}
		matcher.appendTail(buffer);

		return buffer.toString();
	}
}
