package me.jishuna.toollevelling.api.upgrades;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.jishuna.commonlib.WeightedRandom;
import me.jishuna.toollevelling.ToolLevelling;
import me.jishuna.toollevelling.api.event.UpgradeSetupEvent;
import me.jishuna.toollevelling.api.tools.ToolType;
import me.jishuna.toollevelling.upgrades.EnchantmentUpgrade;

public class UpgradeManager {
	private final ToolLevelling plugin;

	private final EnumMap<ToolType, WeightedRandom<Upgrade>> upgradeMap = new EnumMap<>(ToolType.class);
	private final Map<String, Upgrade> upgrades = new HashMap<>();

	public UpgradeManager(ToolLevelling plugin) {
		this.plugin = plugin;
	}

	public void reloadUpgrades() {
		this.upgrades.clear();

		UpgradeSetupEvent event = new UpgradeSetupEvent();
		event.getUpgradesToAdd().addAll(this.getDefaultUpgrades());
		Bukkit.getPluginManager().callEvent(event);

		event.getUpgradesToAdd().forEach(upgrade -> {
			if (upgrade.isEnabled()) {
				this.upgrades.put(upgrade.getKey(), upgrade);

				for (ToolType type : upgrade.getToolTypes()) {
					WeightedRandom<Upgrade> weightedRandom = this.upgradeMap.computeIfAbsent(type,
							key -> new WeightedRandom<>());
					weightedRandom.add(upgrade.getWeight(), upgrade);
				}
			}
		});
	}

	private List<Upgrade> getDefaultUpgrades() {
		List<Upgrade> defaultUpgrades = new ArrayList<>();

		YamlConfiguration upgradeConfig = this.plugin.getUpgradeConfig();

		for (Enchantment enchant : Enchantment.values()) {
			if (upgradeConfig.isConfigurationSection(enchant.getKey().getKey())) {
				defaultUpgrades.add(new EnchantmentUpgrade(enchant, upgradeConfig));
			}
		}

		return defaultUpgrades;
	}

	public Optional<Upgrade> getUpgrade(String key) {
		return Optional.ofNullable(this.upgrades.get(key));
	}

	public Optional<Upgrade> getRandomUpgrade(ToolType type) {
		WeightedRandom<Upgrade> weightedRandom = this.upgradeMap.get(type);

		if (weightedRandom == null)
			return Optional.empty();

		return Optional.of(weightedRandom.poll());
	}

	public Optional<Upgrade> getRandomUpgradeSmart(ToolType type, ItemStack item, int tries) {
		WeightedRandom<Upgrade> weightedRandom = this.upgradeMap.get(type);

		if (weightedRandom == null)
			return Optional.empty();

		Upgrade upgrade = null;

		Optional<Upgrade> conflicOptional;

		boolean success = true;

		int i = 0;
		while (i < tries) {
			i++;

			upgrade = weightedRandom.poll();

			success = true;

			if (upgrade.getLevel(item) >= upgrade.getMaxLevel()) {
				success = false;
				continue;
			}

			for (String key : upgrade.getConflicts()) {
				conflicOptional = getUpgrade(key);

				if (conflicOptional.isPresent() && conflicOptional.get().getLevel(item) > 0) {
					success = false;
					break;
				}
			}

			if (success)
				break;
		}

		if (success) {
			return Optional.ofNullable(upgrade);
		} else {
			return Optional.empty();
		}
	}

}
