package com.voluble.echoeschat.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexColorUtil {

	// Pattern to match hex color codes (&#RRGGBB) and Minecraft color codes (&x)
	private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(&#[A-Fa-f0-9]{6})|(&[0-9a-fA-F])");

	/**
	 * Applies hex colors and standard color codes to a message using Minecraft's ChatColor.
	 *
	 * @param message The input message with hex color codes (e.g., &#FF5733) or standard color codes (e.g., &a).
	 * @return The message with applied colors.
	 */
	public static String applyHexColors(String message) {
		if (message == null || message.isEmpty()) {
			return message; // Return as-is if the input is null or empty
		}

		Matcher matcher = HEX_COLOR_PATTERN.matcher(message);
		StringBuffer buffer = new StringBuffer();

		while (matcher.find()) {
			String colorCode = matcher.group();

			try {
				if (colorCode.startsWith("&#")) {
					// Handle hex color codes (e.g., &#FF5733)
					matcher.appendReplacement(buffer, ChatColor.of(colorCode.substring(1)).toString());
				} else if (colorCode.startsWith("&")) {
					// Handle standard Minecraft color codes (e.g., &a)
					matcher.appendReplacement(buffer, ChatColor.translateAlternateColorCodes('&', colorCode));
				}
			} catch (IllegalArgumentException e) {
				// Skip invalid color codes and log a warning if needed
				System.out.println("Invalid color code detected: " + colorCode);
				matcher.appendReplacement(buffer, ""); // Replace invalid codes with an empty string
			}
		}
		matcher.appendTail(buffer);

		return buffer.toString();
	}
}
