package me.jishuna.toollevelling.api.tools;

import java.util.EnumMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import me.jishuna.toollevelling.api.PluginConstants;

public class ToolTypeManager {

	private final Multimap<ToolType, Material> toolTypeMap = ArrayListMultimap.create();
	private final EnumMap<Material, ToolType> inverseToolTypeMap = new EnumMap<>(Material.class);

	public void reload(ConfigurationSection section) {
		for (String key : section.getKeys(false)) {
			key = key.toUpperCase();

			if (!PluginConstants.TOOL_TYPE_NAMES.contains(key))
				continue;

			ToolType type = ToolType.valueOf(key);

			for (String materialName : section.getStringList(key)) {
				Material material = Material.matchMaterial(materialName.toUpperCase());

				if (material != null) {
					this.toolTypeMap.put(type, material);
					this.inverseToolTypeMap.put(material, type);
				}
			}
		}
	}

	public boolean isMaterialOfType(ToolType type, Material material) {
		return this.toolTypeMap.get(type).contains(material);
	}

	public ToolType getToolType(Material material) {
		ToolType type = this.inverseToolTypeMap.get(material);
		return type == null ? ToolType.NONE : type;
	}
}
