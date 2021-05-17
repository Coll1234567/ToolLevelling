package me.jishuna.toollevelling.api.upgrades;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import me.jishuna.commonlib.StringUtils;
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
	private List<String> description = new ArrayList<>();
	private final int weight;

	public Upgrade(String key, YamlConfiguration upgradeConfig) {
		this(key, upgradeConfig.getConfigurationSection(key));
	}

	public Upgrade(String key, ConfigurationSection upgradeSection) {
		this.key = key;

		this.weight = upgradeSection.getInt("weight", 100);
		this.maxLevel = upgradeSection.getInt("max-level", 5);
		this.enabled = upgradeSection.getBoolean("enabled", true);

		this.name = ChatColor.translateAlternateColorCodes('&', upgradeSection.getString("name", ""));

		this.conflicts.addAll(upgradeSection.getStringList("conflicting-upgrades"));

		for (String typeKey : upgradeSection.getStringList("tool-types")) {
			typeKey = typeKey.toUpperCase();

			if (!PluginConstants.TOOL_TYPE_NAMES.contains(typeKey))
				continue;

			this.toolTypes.add(ToolType.valueOf(typeKey));
		}

		String description = ChatColor.translateAlternateColorCodes('&', upgradeSection.getString("description", ""));

		for (String configKey : upgradeSection.getKeys(false)) {
			description = description.replace("%" + configKey + "%", upgradeSection.getString(configKey));
		}

		List<String> desc = new ArrayList<>();

		for (String line : description.split("\\\\n")) {
			desc.addAll(StringUtils.splitString(line, 30));
		}
		this.description = desc;
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

	public List<String> getDescription() {
		return description;
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
