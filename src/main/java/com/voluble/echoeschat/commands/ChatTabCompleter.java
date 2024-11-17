package com.voluble.echoeschat.commands;

import com.voluble.echoeschat.managers.EmoteColorManager;
import com.voluble.echoeschat.managers.EmoteColorManager.EmoteColorConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ChatTabCompleter implements TabCompleter {

	private final EmoteColorManager emoteColorManager;

	public ChatTabCompleter(EmoteColorManager emoteColorManager) {
		this.emoteColorManager = emoteColorManager;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!(sender instanceof Player)) {
			return Collections.emptyList(); // No tab completion for non-players
		}

		Player player = (Player) sender;

		// Handle /chat command
		if (args.length == 1) {
			// Suggest "emotecolor" as the first argument
			if ("emotecolor".startsWith(args[0].toLowerCase())) {
				return Collections.singletonList("emotecolor");
			}
		}

		// Handle /chat emotecolor command
		if (args.length == 2 && args[0].equalsIgnoreCase("emotecolor")) {
			List<String> suggestions = new ArrayList<>();

			// Add all emote colors the player has permission for
			for (Map.Entry<String, EmoteColorConfig> entry : emoteColorManager.getAllColors().entrySet()) {
				String colorName = entry.getKey();
				EmoteColorConfig colorConfig = entry.getValue();

				if (player.hasPermission(colorConfig.getPermission()) && colorName.startsWith(args[1].toLowerCase())) {
					suggestions.add(colorName);
				}
			}

			return suggestions;
		}

		return Collections.emptyList(); // No other arguments are handled
	}
}
