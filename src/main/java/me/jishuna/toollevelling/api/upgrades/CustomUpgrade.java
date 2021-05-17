package me.jishuna.toollevelling.api.upgrades;

import java.util.Collection;
import java.util.Random;
import java.util.function.BiConsumer;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import me.jishuna.toollevelling.PluginKeys;
import me.jishuna.toollevelling.api.event.EventWrapper;

public abstract class CustomUpgrade extends Upgrade {

	private final Random random = new Random();
	private final double chancePerLevel;
	private final Plugin plugin;
	private final NamespacedKey upgradeKey;
	private final Multimap<Class<? extends Event>, EventWrapper<? extends Event>> handlerMap = ArrayListMultimap
			.create();

	public CustomUpgrade(Plugin owner, String key, YamlConfiguration upgradeConfig) {
		this(owner, key, upgradeConfig.getConfigurationSection(key));
	}

	public CustomUpgrade(Plugin owner, String key, ConfigurationSection upgradeSection) {
		super(key, upgradeSection);

		this.plugin = owner;
		this.upgradeKey = new NamespacedKey(owner, key);

		this.chancePerLevel = upgradeSection.getDouble("chance-per-level", 10);
	}

	@Override
	public void onLevelup(ItemStack item) {

		ItemMeta meta = item.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();

		PersistentDataContainer upgradeContainer = container.get(PluginKeys.UPGRADE_COMPOUND.getKey(),
				PersistentDataType.TAG_CONTAINER);

		if (upgradeContainer == null) {
			upgradeContainer = container.getAdapterContext().newPersistentDataContainer();
		}

		int level = upgradeContainer.getOrDefault(this.upgradeKey, PersistentDataType.INTEGER, 0);
		upgradeContainer.set(this.upgradeKey, PersistentDataType.INTEGER, level + 1);

		container.set(PluginKeys.UPGRADE_COMPOUND.getKey(), PersistentDataType.TAG_CONTAINER, upgradeContainer);

		item.setItemMeta(meta);
	}

	@Override
	public int getLevel(ItemStack item) {
		PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

		PersistentDataContainer upgradeContainer = container.get(PluginKeys.UPGRADE_COMPOUND.getKey(),
				PersistentDataType.TAG_CONTAINER);

		if (upgradeContainer == null)
			return 0;

		return upgradeContainer.getOrDefault(this.upgradeKey, PersistentDataType.INTEGER, 0);
	}

	public <T extends Event> void addEventHandler(Class<T> type, BiConsumer<T, UpgradeData> consumer) {
		this.handlerMap.put(type, new EventWrapper<>(type, consumer));
	}

	public <T extends Event> Collection<EventWrapper<? extends Event>> getEventHandlers(Class<T> type) {
		return this.handlerMap.get(type);
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public NamespacedKey getUpgradeKey() {
		return upgradeKey;
	}

	public double getChancePerLevel() {
		return chancePerLevel;
	}

	public Random getRandom() {
		return random;
	}

}
