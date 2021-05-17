package me.jishuna.toollevelling.api.experience;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.jishuna.toollevelling.PluginKeys;
import me.jishuna.toollevelling.api.PluginConstants;
import me.jishuna.toollevelling.api.tools.ToolType;
import me.jishuna.toollevelling.api.tools.ToolTypeManager;

public class ExperienceManager {

	private final Map<ToolType, ExperienceData> experienceMap = new HashMap<>();
	private final ToolTypeManager toolTypeManager;

	public ExperienceManager(ToolTypeManager toolTypeManager) {
		this.toolTypeManager = toolTypeManager;
	}

	public void reload(ConfigurationSection section) {
		for (String key : section.getKeys(false)) {
			key = key.toUpperCase();
			ConfigurationSection toolTypeSection = section.getConfigurationSection(key);

			if (!PluginConstants.TOOL_TYPE_NAMES.contains(key))
				continue;

			ToolType type = ToolType.valueOf(key);

			for (String entry : toolTypeSection.getKeys(false)) {
				float value = (float) toolTypeSection.getDouble(entry, 0f);

				if (value == 0f)
					continue;

				ExperienceData experienceData = this.experienceMap.computeIfAbsent(type,
						mapKey -> new ExperienceData());

				entry = entry.toUpperCase();

				if (PluginConstants.ENTITY_TYPE_NAMES.contains(entry)) {
					experienceData.registerEntity(EntityType.valueOf(entry), value);
				} else {
					Material material = Material.matchMaterial(entry);

					if (material != null) {
						experienceData.registerMaterial(material, value);
					}
				}
			}
		}
	}

	public float getExperience(ToolType type, EntityType entityType) {
		ExperienceData data = this.experienceMap.get(type);

		if (data == null)
			return 0f;

		return data.getExperience(entityType);
	}

	public float getExperience(ToolType type, Material material) {
		ExperienceData data = this.experienceMap.get(type);

		if (data == null)
			return 0f;

		return data.getExperience(material);
	}

	public void increaseExperience(ItemStack item, EntityType type) {
		ToolType toolType = toolTypeManager.getToolType(item.getType());
		if (toolType == ToolType.NONE)
			return;

		float toAdd = getExperience(toolType, type);
		if (toAdd <= 0)
			return;

		updateExperience(item, toAdd);
	}

	public void increaseExperience(ItemStack item, Material material) {
		ToolType toolType = toolTypeManager.getToolType(item.getType());
		if (toolType == ToolType.NONE)
			return;

		float toAdd = getExperience(toolType, material);
		if (toAdd <= 0)
			return;

		updateExperience(item, toAdd);
	}

	private void updateExperience(ItemStack item, float amount) {
		ItemMeta im = item.getItemMeta();
		PersistentDataContainer container = im.getPersistentDataContainer();

		int level = container.getOrDefault(PluginKeys.LEVEL.getKey(), PersistentDataType.INTEGER, 0);
		double experience = container.getOrDefault(PluginKeys.EXPERIENCE.getKey(), PersistentDataType.DOUBLE, 0d);

		double nextLevel;
		if (!container.has(PluginKeys.NEXT_LEVEL.getKey(), PersistentDataType.DOUBLE)) {
			nextLevel = 1000 + (level * 20);
			container.set(PluginKeys.NEXT_LEVEL.getKey(), PersistentDataType.DOUBLE, nextLevel);
		} else {
			nextLevel = container.get(PluginKeys.NEXT_LEVEL.getKey(), PersistentDataType.DOUBLE);
		}

		experience += amount;

		if (experience >= nextLevel) {
			while (experience > nextLevel) {
				level++;
				experience = experience - nextLevel;
				nextLevel = 1000 + (level * 20);

				container.set(PluginKeys.POINTS.getKey(), PersistentDataType.INTEGER,
						container.getOrDefault(PluginKeys.POINTS.getKey(), PersistentDataType.INTEGER, 0) + 1);
			}

			container.set(PluginKeys.LEVEL.getKey(), PersistentDataType.INTEGER, level);
			container.set(PluginKeys.EXPERIENCE.getKey(), PersistentDataType.DOUBLE, experience);
			container.set(PluginKeys.NEXT_LEVEL.getKey(), PersistentDataType.DOUBLE, nextLevel);
		} else {
			container.set(PluginKeys.EXPERIENCE.getKey(), PersistentDataType.DOUBLE, experience);
		}

		item.setItemMeta(im);
	}

}
