package com.voluble.echoeschat.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

import java.util.List;

public class InteractiveTextUtil {

	/**
	 * Creates a hoverable and clickable text component.
	 *
	 * @param baseText   The base text to display.
	 * @param hoverLines The lines to show on hover.
	 * @param clickAction The command to execute on click, or null for none.
	 * @param player The player for placeholder parsing.
	 * @return The interactive text component.
	 */
	public static Component createInteractiveComponent(String baseText, List<String> hoverLines, String clickAction, Player player) {
		// Parse placeholders in the base text
		String parsedBaseText = PlaceholderUtil.parsePlaceholders(player, baseText);

		// Build hover text component
		Component hoverComponent = Component.empty();
		if (hoverLines != null) {
			for (String line : hoverLines) {
				hoverComponent = hoverComponent.append(Component.text(PlaceholderUtil.parsePlaceholders(player, line)))
						.append(Component.newline());
			}
		}

		// Build the interactive component
		Component interactiveComponent = Component.text(parsedBaseText)
				.hoverEvent(HoverEvent.showText(hoverComponent));

		if (clickAction != null && !clickAction.isEmpty()) {
			interactiveComponent = interactiveComponent.clickEvent(ClickEvent.runCommand(clickAction));
		}

		return interactiveComponent;
	}
}