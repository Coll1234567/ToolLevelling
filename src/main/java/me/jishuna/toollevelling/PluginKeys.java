package me.jishuna.toollevelling;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public enum PluginKeys {
	LEVEL("level"), EXPERIENCE("experience"), NEXT_LEVEL("next_level"), POINTS("upgrade_points"), UPGRADE_COMPOUND("upgrades");

	private final String name;
	private NamespacedKey key;

	private PluginKeys(String name) {
		this.name = name;
	}

	public static void initialize(Plugin plugin) {
		for (PluginKeys plguinKey : PluginKeys.values()) {
			plguinKey.key = new NamespacedKey(plugin, plguinKey.name);
		}
	}

	public NamespacedKey getKey() {
		return this.key;
	}

}
