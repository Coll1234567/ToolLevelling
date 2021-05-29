package me.jishuna.toollevelling.api.upgrades;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import me.jishuna.commonlib.ItemBuilder;
import me.jishuna.commonlib.MessageConfig;
import me.jishuna.commonlib.StringUtils;
import me.jishuna.toollevelling.ToolLevelling;
import me.jishuna.toollevelling.api.PluginConstants;
import me.jishuna.toollevelling.api.tools.ToolType;
import net.md_5.bungee.api.ChatColor;

public abstract class Upgrade {
	private final String key;
	private boolean enabled = false;
	private String name;
	private int maxLevel;
	private final Set<ToolType> toolTypes = new HashSet<>();
	private final Set<String> conflicts = new HashSet<>();
	private List<String> description = new ArrayList<>();
	private int weight;

	public Upgrade(String key, YamlConfiguration upgradeConfig) {
		this.key = key;

		if (upgradeConfig != null)
			loadData(upgradeConfig);
	}

	protected void loadData(YamlConfiguration upgradeConfig) {
		this.weight = upgradeConfig.getInt("weight", 100);
		this.maxLevel = upgradeConfig.getInt("max-level", 5);
		this.enabled = upgradeConfig.getBoolean("enabled", true);

		this.name = ChatColor.translateAlternateColorCodes('&', upgradeConfig.getString("name", ""));

		this.conflicts.addAll(upgradeConfig.getStringList("conflicting-upgrades"));

		for (String typeKey : upgradeConfig.getStringList("tool-types")) {
			typeKey = typeKey.toUpperCase();

			if (!PluginConstants.TOOL_TYPE_NAMES.contains(typeKey))
				continue;

			this.toolTypes.add(ToolType.valueOf(typeKey));
		}

		String description = ChatColor.translateAlternateColorCodes('&', upgradeConfig.getString("description", ""));

		for (String configKey : upgradeConfig.getKeys(false)) {
			description = description.replace("%" + configKey + "%", upgradeConfig.getString(configKey));
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

	public ItemStack asItem(ToolLevelling plugin, Material material, Integer level) {
		ItemBuilder builder = new ItemBuilder(material).withName(getName());
		if (level != null) {
			builder.addLore(ChatColor.GOLD + "Level: " + ChatColor.GREEN + level, "");
		}
		builder.addLore(getDescription());
		builder.addLore("");
		builder.addLore(ChatColor.GOLD + "Max Level: " + ChatColor.GREEN + getMaxLevel());

		if (!getConflicts().isEmpty()) {
			MessageConfig config = plugin.getMessageConfig();
			builder.addLore("", config.getString("upgrades.conflicts-with"));
		}

		for (String conflictKey : getConflicts()) {
			plugin.getUpgradeManager().getUpgrade(conflictKey)
					.ifPresent(conflictUpgrade -> builder.addLore(ChatColor.GRAY + " - " + conflictUpgrade.getName()));
		}
		return builder.build();

	}

	public abstract void onLevelup(ItemStack item);

	public abstract int getLevel(ItemStack item);

}
