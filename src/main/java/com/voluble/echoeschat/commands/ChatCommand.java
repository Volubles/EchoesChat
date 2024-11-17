package com.voluble.echoeschat.commands;

import com.voluble.echoeschat.EchoesChat;
import com.voluble.echoeschat.managers.EmoteColorManager;
import com.voluble.echoeschat.managers.EmoteColorManager.EmoteColorConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ChatCommand implements CommandExecutor {
	private final EchoesChat plugin;
	private final EmoteColorManager emoteColorManager;
	private final Map<UUID, String> playerEmoteColors;

	public ChatCommand(EchoesChat plugin, EmoteColorManager emoteColorManager, Map<UUID, String> playerEmoteColors) {
		this.plugin = plugin;
		this.emoteColorManager = emoteColorManager;
		this.playerEmoteColors = playerEmoteColors;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can use this command.");
			return false;
		}

		Player player = (Player) sender;

		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			if (!player.hasPermission("echoeschat.reload")) {
				player.sendMessage(ChatColor.RED + "You do not have permission to reload the plugin.");
				return false;
			}

			// Ensure all configurations and managers are reloaded properly
			plugin.reloadConfig();
			plugin.getChannelManager().reloadChannels(plugin.getConfig());
			plugin.getEmoteColorManager().reloadColors(plugin.getConfig());
			plugin.loadDataConfig();
			plugin.loadPlayerEmoteColors();

			player.sendMessage(ChatColor.GREEN + "EchoesChat has been reloaded!");
			return true;
		}

		if (args.length == 0) {
			player.sendMessage(getAvailableColorsMessage(player));
			return true;
		}

		if (args.length == 1) {
			String colorName = args[0].toLowerCase();
			EmoteColorConfig colorConfig = emoteColorManager.getEmoteColor(colorName);

			if (colorConfig == null) {
				player.sendMessage(ChatColor.RED + "Invalid emote color. Use /chat to see available options.");
				return false;
			}

			if (!player.hasPermission(colorConfig.getPermission())) {
				player.sendMessage(ChatColor.RED + colorConfig.getDeniedMessage());
				return false;
			}

			playerEmoteColors.put(player.getUniqueId(), colorName);
			player.sendMessage(ChatColor.GREEN + "Your emote color has been set to "
					+ ChatColor.translateAlternateColorCodes('&', colorConfig.getColor())
					+ colorName + ChatColor.RESET + "!");
			return true;
		}

		player.sendMessage(ChatColor.RED + "Usage: /chat [emotecolor|reload]");
		return false;
	}

	private String getAvailableColorsMessage(Player player) {
		StringBuilder message = new StringBuilder(ChatColor.GOLD + "Available Emote Colors: " + ChatColor.RESET);

		for (Map.Entry<String, EmoteColorConfig> entry : emoteColorManager.getAllColors().entrySet()) {
			String colorName = entry.getKey();
			EmoteColorConfig colorConfig = entry.getValue();
			String colorCode = colorConfig.getColor();

			if (colorCode == null || colorName == null) continue;

			if (player.hasPermission(colorConfig.getPermission())) {
				message.append(ChatColor.translateAlternateColorCodes('&', colorCode))
						.append(colorName)
						.append(ChatColor.RESET)
						.append(", ");
			}
		}

		if (message.length() > 2) {
			message.setLength(message.length() - 2);
		}

		return message.toString();
	}
}
