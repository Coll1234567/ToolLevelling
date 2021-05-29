package me.jishuna.toollevelling.api.upgrades;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import me.jishuna.commonlib.FileUtils;
import me.jishuna.toollevelling.PluginKeys;
import me.jishuna.toollevelling.api.event.EventWrapper;
import redempt.crunch.functional.EvaluationEnvironment;

public abstract class CustomUpgrade extends Upgrade {

	private final Random random = new Random();
	private final Plugin plugin;
	private final NamespacedKey upgradeKey;
	private final Multimap<Class<? extends Event>, EventWrapper<? extends Event>> handlerMap = ArrayListMultimap
			.create();
	private static final EvaluationEnvironment evalEnvironment = new EvaluationEnvironment();
	
	static {
		evalEnvironment.setVariableNames("level");
	}

	public CustomUpgrade(Plugin owner, String key, YamlConfiguration upgradeConfig) {
		super(key, upgradeConfig);

		this.plugin = owner;
		this.upgradeKey = new NamespacedKey(owner, key);
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

	public Random getRandom() {
		return random;
	}
	
	public static EvaluationEnvironment getEvalenvironment() {
		return evalEnvironment;
	}

	public static YamlConfiguration loadConfig(Plugin owner, String key) {
		Optional<File> optional = FileUtils.copyResource(owner, "upgrades/custom/" + key + ".yml");

		if (optional.isPresent()) {
			return YamlConfiguration.loadConfiguration(optional.get());
		}
		return null;
	}

}
