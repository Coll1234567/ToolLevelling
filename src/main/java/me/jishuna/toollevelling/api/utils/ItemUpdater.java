package me.jishuna.toollevelling.api.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.jishuna.commonlib.MessageConfig;
import me.jishuna.commonlib.StringSet;
import me.jishuna.commonlib.StringSetType;
import me.jishuna.commonlib.StringUtils;
import me.jishuna.toollevelling.PluginKeys;
import me.jishuna.toollevelling.ToolLevelling;
import me.jishuna.toollevelling.api.tools.ToolType;
import me.jishuna.toollevelling.api.upgrades.Upgrade;
import net.md_5.bungee.api.ChatColor;

public class ItemUpdater {
	private static final StringSetType STRING_SET_TYPE = new StringSetType();

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.#");
	private static final List<String> LORE_CACHE = new ArrayList<>();

	private static final Map<String, Long> CACHE_MAP = new HashMap<>();

	public static void cacheLore(MessageConfig config) {
		LORE_CACHE.clear();

		for (String line : config.getStringList("item-display")) {
			LORE_CACHE.add(ChatColor.translateAlternateColorCodes('&', line));
		}

		Collections.reverse(LORE_CACHE);
	}

	// TODO this is called often, can we optimize it more?
	public static void updateItem(ToolLevelling plugin, ItemStack item, boolean force) {
		if (item.getType().isAir())
			return;

		if (plugin.getToolTypeManager().getToolType(item.getType()) == ToolType.NONE)
			return;

		ItemMeta meta = item.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();

		String uuid = container.get(PluginKeys.UUID.getKey(), PersistentDataType.STRING);

		if (uuid == null) {
			uuid = UUID.randomUUID().toString();
			container.set(PluginKeys.UUID.getKey(), PersistentDataType.STRING, uuid);
		}

		boolean cached = CACHE_MAP.computeIfAbsent(uuid, key -> 0l) > System.currentTimeMillis() && !force;

		if (!cached) {
			CACHE_MAP.put(uuid,
					System.currentTimeMillis() + plugin.getConfig().getInt("cache-time-seconds", 10) * 1000);

			updateItem(plugin, meta, container);
			item.setItemMeta(meta);
		}
	}

	private static void updateItem(ToolLevelling plugin, ItemMeta meta, PersistentDataContainer container) {
		List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

		clearOldLore(container, lore);

		StringSet stringSet = new StringSet();

		int level = container.getOrDefault(PluginKeys.LEVEL.getKey(), PersistentDataType.INTEGER, 0);
		int points = container.getOrDefault(PluginKeys.POINTS.getKey(), PersistentDataType.INTEGER, 0);
		double experience = container.getOrDefault(PluginKeys.EXPERIENCE.getKey(), PersistentDataType.DOUBLE, 0d);
		double nextLevel = container.getOrDefault(PluginKeys.NEXT_LEVEL.getKey(), PersistentDataType.DOUBLE, 0d);

		if (experience == 0 && level == 0)
			return;

		PersistentDataContainer upgradeContainer = container.get(PluginKeys.UPGRADE_COMPOUND.getKey(),
				PersistentDataType.TAG_CONTAINER);

		for (int i = 0; i < LORE_CACHE.size(); i++) {
			String line = LORE_CACHE.get(i);

			// TODO Ugly, replace it
			line = line.replace("%level%", Integer.toString(level));
			line = line.replace("%points%", Integer.toString(points));
			line = line.replace("%experience%", DECIMAL_FORMAT.format(experience));
			line = line.replace("%nextlevel%", DECIMAL_FORMAT.format(nextLevel));
			line = line.replace("%percentage%", DECIMAL_FORMAT.format((experience / nextLevel) * 100));

			lore.add(0, line);
			stringSet.add(line);
		}

		if (upgradeContainer != null) {

			for (NamespacedKey upgradeKey : upgradeContainer.getKeys()) {
				Optional<Upgrade> upgradeOptional = plugin.getUpgradeManager().getUpgrade(upgradeKey.getKey());

				if (!upgradeOptional.isPresent())
					continue;

				Upgrade upgrade = upgradeOptional.get();
				String line = upgrade.getName() + " " + StringUtils
						.toRomanNumeral(upgradeContainer.getOrDefault(upgradeKey, PersistentDataType.INTEGER, 1));

				lore.add(0, line);
				stringSet.add(line);
			}
		}
		container.set(PluginKeys.LORE_CACHE.getKey(), STRING_SET_TYPE, stringSet);
		meta.setLore(lore);
	}

	private static void clearOldLore(PersistentDataContainer container, List<String> lore) {
		StringSet set = container.get(PluginKeys.LORE_CACHE.getKey(), STRING_SET_TYPE);
		if (set == null)
			return;

		Iterator<String> iterator = set.iterator();

		while (iterator.hasNext()) {
			String line = iterator.next();

			if (set.contains(line)) {
				lore.remove(line);
			}
		}

	}

}
