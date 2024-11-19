package com.voluble.echoeschat.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
public class PlaceholderUtil {

	/**
	 * Parses PlaceholderAPI placeholders in a string.
	 *
	 * @param player The player for whom placeholders should be parsed.
	 * @param input  The string containing placeholders.
	 * @return The string with placeholders replaced, or the input unchanged if PAPI is not enabled.
	 */
	public static String parsePlaceholders(Player player, String input) {
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			return PlaceholderAPI.setPlaceholders(player, input);
		}
		return input;
	}
}
