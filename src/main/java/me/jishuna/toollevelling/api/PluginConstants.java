package me.jishuna.toollevelling.api;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.entity.EntityType;

import me.jishuna.toollevelling.api.tools.ToolType;

public class PluginConstants {

	public static final Set<String> TOOL_TYPE_NAMES = Arrays.asList(ToolType.values()).stream().map(ToolType::toString)
			.collect(Collectors.toSet());

	public static final Set<String> ENTITY_TYPE_NAMES = Arrays.asList(EntityType.values()).stream()
			.map(EntityType::toString).collect(Collectors.toSet());
}
