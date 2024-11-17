package com.voluble.echoeschat.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class EmoteColorManager {

	private final Map<String, EmoteColorConfig> emoteColors = new HashMap<>();
	private static final String DEFAULT_COLOR_NAME = "default";

	public EmoteColorManager(FileConfiguration config) {
		loadEmoteColors(config);
	}

	private void loadEmoteColors(FileConfiguration config) {
		if (config.contains("emoteColors")) {
			for (String key : config.getConfigurationSection("emoteColors").getKeys(false)) {
				String color = config.getString("emoteColors." + key + ".color");
				String permission = config.getString("emoteColors." + key + ".permission", "");
				String deniedMessage = config.getString("emoteColors." + key + ".deniedMessage", "You cannot use this color.");

				if (isValidColor(color)) {
					emoteColors.put(key.toLowerCase(), new EmoteColorConfig(color, permission, deniedMessage));
				} else {
					System.out.println(ChatColor.RED + "Invalid emote color configuration: " + key + " with color: " + color);
				}
			}
		}

		// Ensure a default color is always defined
		if (!emoteColors.containsKey(DEFAULT_COLOR_NAME)) {
			emoteColors.put(DEFAULT_COLOR_NAME, new EmoteColorConfig("#FFFFFF", "", "You cannot use this color."));
			System.out.println(ChatColor.YELLOW + "Default emote color set to white (#FFFFFF).");
		}
	}

	public void reloadColors(FileConfiguration config) {
		emoteColors.clear(); // Clear the old values

		ConfigurationSection emoteColorsSection = config.getConfigurationSection("emoteColors");
		if (emoteColorsSection != null) {
			for (String key : emoteColorsSection.getKeys(false)) {
				String colorCode = emoteColorsSection.getString(key + ".color");
				String permission = emoteColorsSection.getString(key + ".permission");
				String deniedMessage = emoteColorsSection.getString(key + ".deniedMessage");

				if (colorCode != null && permission != null) {
					emoteColors.put(key, new EmoteColorConfig(colorCode, permission, deniedMessage));
				}
			}
		}
	}

	private boolean isValidColor(String color) {
		if (color == null) return false;
		return color.startsWith("#") && color.length() == 7 || color.matches("&[0-9a-fA-F]");
	}

	public String getColor(String colorName) {
		EmoteColorConfig config = getEmoteColor(colorName);
		return (config != null) ? config.getColor() : null;
	}

	public String getPermission(String colorName) {
		EmoteColorConfig config = getEmoteColor(colorName);
		return (config != null) ? config.getPermission() : null;
	}

	public String getDeniedMessage(String colorName) {
		EmoteColorConfig config = getEmoteColor(colorName);
		return (config != null) ? config.getDeniedMessage() : null;
	}

	public boolean isColorDefined(String colorName) {
		return emoteColors.containsKey(colorName.toLowerCase());
	}

	public EmoteColorConfig getEmoteColor(String colorName) {
		return emoteColors.get(colorName.toLowerCase());
	}

	public Map<String, EmoteColorConfig> getAllColors() {
		return emoteColors;
	}

	public EmoteColorConfig getDefaultColor() {
		return emoteColors.get(DEFAULT_COLOR_NAME);
	}

	public static class EmoteColorConfig {
		private final String color;
		private final String permission;
		private final String deniedMessage;

		public EmoteColorConfig(String color, String permission, String deniedMessage) {
			this.color = color;
			this.permission = permission;
			this.deniedMessage = deniedMessage;
		}

		public String getColor() {
			return color;
		}

		public String getPermission() {
			return permission;
		}

		public String getDeniedMessage() {
			return deniedMessage;
		}
	}
}
