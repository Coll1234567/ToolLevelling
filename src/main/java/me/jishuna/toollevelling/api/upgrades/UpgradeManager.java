package me.jishuna.toollevelling.api.upgrades;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.jishuna.commonlib.FileUtils;
import me.jishuna.commonlib.WeightedRandom;
import me.jishuna.toollevelling.PluginKeys;
import me.jishuna.toollevelling.ToolLevelling;
import me.jishuna.toollevelling.api.event.UpgradeSetupEvent;
import me.jishuna.toollevelling.api.tools.ToolType;
import me.jishuna.toollevelling.upgrades.MagnetUpgrade;
import me.jishuna.toollevelling.upgrades.MultiplyUpgrade;
import me.jishuna.toollevelling.upgrades.ScavengerUpgrade;
import me.jishuna.toollevelling.upgrades.SmeltingUpgrade;

public class UpgradeManager {
	private final ToolLevelling plugin;

	private final Map<ToolType, WeightedRandom<Upgrade>> upgradeMap = new EnumMap<>(ToolType.class);
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

		for (Enchantment enchant : Enchantment.values()) {

			FileUtils.copyResource(this.plugin, "upgrades/vanilla/" + enchant.getKey().getKey().toLowerCase() + ".yml")
					.ifPresent(file -> {

						YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
						defaultUpgrades.add(new EnchantmentUpgrade(enchant, config));
					});
		}

		defaultUpgrades.add(new ScavengerUpgrade(this.plugin));
		defaultUpgrades.add(new SmeltingUpgrade(this.plugin));
		defaultUpgrades.add(new MagnetUpgrade(this.plugin));
		defaultUpgrades.add(new MultiplyUpgrade(this.plugin));

		return defaultUpgrades;
	}

	public Optional<Upgrade> getUpgrade(String key) {
		return Optional.ofNullable(this.upgrades.get(key));
	}

	public Map<CustomUpgrade, Integer> getCustomUpgrades(ItemStack item) {
		Map<CustomUpgrade, Integer> upgradeMap = new HashMap<>();

		PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

		PersistentDataContainer upgradeContainer = container.get(PluginKeys.UPGRADE_COMPOUND.getKey(),
				PersistentDataType.TAG_CONTAINER);

		if (upgradeContainer == null)
			return upgradeMap;

		for (NamespacedKey upgradeKey : upgradeContainer.getKeys()) {
			int level = upgradeContainer.getOrDefault(upgradeKey, PersistentDataType.INTEGER, 1);
			getUpgrade(upgradeKey.getKey()).ifPresent(upgrade -> upgradeMap.put((CustomUpgrade) upgrade, level));
		}

		return upgradeMap;
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
