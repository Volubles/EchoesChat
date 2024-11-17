package com.voluble.echoeschat;

import java.util.List;

public class ChatChannel {
	private final String name;
	private final String format;
	private final int range;
	private final String permission;
	private final boolean isDefault;
	private final boolean isEnabled;
	private final List<String> commands;
	private final String prefix;
	private final boolean quotes;
	private final boolean autoFormat;
	private final boolean capitalizeAll;
private final boolean allowsEmotes;

	public ChatChannel(String name, String format, int range, String permission, boolean isDefault, boolean isEnabled, List<String> commands, String prefix, boolean quotes, boolean autoFormat, boolean capitalizeAll, boolean allowsEmotes) {
		this.name = name;
		this.format = format;
		this.range = range;
		this.permission = permission;
		this.isDefault = isDefault;
		this.isEnabled = isEnabled;
		this.commands = commands;
		this.prefix = prefix;
		this.quotes = quotes;
		this.autoFormat = autoFormat;
		this.capitalizeAll = capitalizeAll;
		this.allowsEmotes = allowsEmotes;
	}

	// Getters for all fields
	public String getName() {
		return name;
	}

	public String getFormat() {
		return format;
	}

	public int getRange() {
		return range;
	}

	public String getPermission() {
		return permission;
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
		return prefix == null ? "" : prefix; // Return an empty string if prefix is null
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
	public boolean allowsEmotes() {
		return allowsEmotes;
	}
}