package com.voluble.echoeschat.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;

import java.lang.reflect.Field;

public class CommandRegistrar {
	private static CommandMap commandMap;

	static {
		try {
			// Access the CommandMap from the Bukkit server
			Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			field.setAccessible(true);
			commandMap = (CommandMap) field.get(Bukkit.getServer());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Registers a command dynamically with the specified name and namespace.
	 *
	 * @param name      The name of the command to register.
	 * @param namespace The namespace to use for the command (set to null for no namespace).
	 * @param command   The BukkitCommand instance to register.
	 */
	public static void registerCommand(String name, String namespace, BukkitCommand command) {
		if (commandMap != null) {
			commandMap.register(namespace == null ? "" : namespace, command);
		}
	}
}
