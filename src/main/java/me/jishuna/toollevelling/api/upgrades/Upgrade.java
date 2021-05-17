package me.jishuna.toollevelling.api.upgrades;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import me.jishuna.toollevelling.api.PluginConstants;
import me.jishuna.toollevelling.api.tools.ToolType;
import net.md_5.bungee.api.ChatColor;

public abstract class Upgrade {
	private final String key;
	private final boolean enabled;
	private final String name;
	private final int maxLevel;
	private final Set<ToolType> toolTypes = new HashSet<>();
	private final Set<String> conflicts = new HashSet<>();
	private final int weight;

	public Upgrade(String key, YamlConfiguration upgradeConfig) {
		this.key = key;

		ConfigurationSection section = upgradeConfig.getConfigurationSection(key);

		this.weight = section.getInt("weight", 100);
		this.maxLevel = section.getInt("max-level", 5);
		this.enabled = section.getBoolean("enabled", true);

		this.name = ChatColor.translateAlternateColorCodes('&', section.getString("name", ""));
		
		this.conflicts.addAll(section.getStringList("conflicting-upgrades"));

		for (String typeKey : section.getStringList("tool-types")) {
			typeKey = typeKey.toUpperCase();

			if (!PluginConstants.TOOL_TYPE_NAMES.contains(typeKey))
				continue;

			this.toolTypes.add(ToolType.valueOf(typeKey));
		}
	}

	public String getKey() {
		return key;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public String getName() {
		return name;
	}

	public int getMaxLevel() {
		return maxLevel;
	}
	
	public Set<ToolType> getToolTypes() {
		return toolTypes;
	}

	public Set<String> getConflicts() {
		return conflicts;
	}

	public int getWeight() {
		return weight;
	}
	
	public abstract void onLevelup(ItemStack item);
	
	public abstract int getLevel(ItemStack item);

}
