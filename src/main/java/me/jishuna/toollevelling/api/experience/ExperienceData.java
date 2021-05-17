package me.jishuna.toollevelling.api.experience;

import java.util.EnumMap;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class ExperienceData {

	private final EnumMap<EntityType, Float> entityMap = new EnumMap<>(EntityType.class);
	private final EnumMap<Material, Float> materialMap = new EnumMap<>(Material.class);

	public void registerMaterial(Material material, float amount) {
		this.materialMap.put(material, amount);
	}

	public void registerEntity(EntityType entityType, float amount) {
		this.entityMap.put(entityType, amount);
	}

	public float getExperience(Material material) {
		return this.materialMap.getOrDefault(material, 0f);
	}

	public float getExperience(EntityType entityType) {
		return this.entityMap.getOrDefault(entityType, 0f);
	}
}
