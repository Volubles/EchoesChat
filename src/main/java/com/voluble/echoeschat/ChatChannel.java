package com.voluble.echoeschat;

import com.voluble.echoeschat.utils.HexColorUtil;
import com.voluble.echoeschat.utils.PlaceholderUtil;
import org.bukkit.entity.Player;

import java.util.List;

public class ChatChannel {
	private final String name;
	private final String format;
	private final int range;
	private final String readPermission;
	private final String writePermission;
	private final boolean isDefault;
	private final boolean isEnabled;
	private final List<String> commands;
	private final String prefix;
	private final boolean quotes;
	private final boolean autoFormat;
	private final boolean capitalizeAll;

	public ChatChannel(String name, String format, int range, String readPermission, String writePermission, boolean isDefault, boolean isEnabled, List<String> commands, String prefix, boolean quotes, boolean autoFormat, boolean capitalizeAll, boolean allowsEmotes) {
		this.name = name.toLowerCase();
		this.format = format;
		this.range = range;
		this.readPermission = readPermission;
		this.writePermission = writePermission;
		this.isDefault = isDefault;
		this.isEnabled = isEnabled;
		this.commands = commands;
		this.prefix = prefix;
		this.quotes = quotes;
		this.autoFormat = autoFormat;
		this.capitalizeAll = capitalizeAll;
	}

	// Getters for all fields
	public String getName() {
		return name;
	}

	public String getFormat(Player player) {
		// Apply hex colors and parse placeholders for the format
		String formattedFormat = HexColorUtil.applyHexColors(format);
		formattedFormat = PlaceholderUtil.parsePlaceholders(player, formattedFormat == null ? "" : formattedFormat);

		return formattedFormat;
	}


	public int getRange() {
		return range;
	}

	public String getReadPermission() {
		return readPermission;
	}

	public String getWritePermission() {
		return writePermission;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public List<String> getCommands() {
		return commands;
	}

	public String getPrefix() {
		// Apply hex colors for the prefix
		String formattedPrefix = prefix == null ? "" : HexColorUtil.applyHexColors(prefix);

		// Add reset code only if the prefix is not empty
		return formattedPrefix.isEmpty() ? "" : formattedPrefix + "§r";
	}


	public boolean usesQuotes() {
		return quotes;
	}
	public boolean isAutoFormat() {
		return autoFormat;
	}
	public boolean isCapitalizeAll() {
		return capitalizeAll;
	}
}