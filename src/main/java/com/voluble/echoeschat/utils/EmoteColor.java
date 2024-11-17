package com.voluble.echoeschat.utils;

public class EmoteColor {
	private final String code; // Hex or Minecraft color code
	private final String permission; // Permission to use this color
	private final String deniedMessage; // Custom message if permission is denied

	public EmoteColor(String code, String permission, String deniedMessage) {
		this.code = code;
		this.permission = permission;
		this.deniedMessage = deniedMessage;
	}

	/**
	 * Checks if the given color code is a valid hexadecimal color.
	 *
	 * @param colorCode The color code to validate.
	 * @return true if the color code is valid, false otherwise.
	 */
	public static boolean isValidColor(String colorCode) {
		// A valid color code must start with '#' followed by exactly 6 hexadecimal digits
		if (colorCode == null || !colorCode.startsWith("#")) {
			return false;
		}

		String hex = colorCode.substring(1);
		return hex.matches("^[a-fA-F0-9]{6}$");
	}

	public String getCode() {
		return code;
	}

	public String getPermission() {
		return permission;
	}

	public String getDeniedMessage() {
		return deniedMessage;
	}

}
