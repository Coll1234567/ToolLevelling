package me.jishuna.toollevelling;

import java.io.File;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import me.jishuna.commonlib.FileUtils;
import me.jishuna.commonlib.MessageConfig;
import me.jishuna.toollevelling.api.experience.ExperienceManager;
import me.jishuna.toollevelling.api.inventory.CustomInventoryManager;
import me.jishuna.toollevelling.api.tools.ToolTypeManager;
import me.jishuna.toollevelling.api.upgrades.UpgradeManager;
import me.jishuna.toollevelling.api.utils.ItemUpdater;
import me.jishuna.toollevelling.listeners.BlockListeners;
import me.jishuna.toollevelling.listeners.EntityListeners;
import me.jishuna.toollevelling.listeners.ExperienceListener;
import me.jishuna.toollevelling.listeners.ItemMenuListener;

public class ToolLevelling extends JavaPlugin {

	private YamlConfiguration config;
	private YamlConfiguration experienceConfig;

	private MessageConfig messageConfig;

	private UpgradeManager upgradeManager;
	private ToolTypeManager toolTypeManager;
	private ExperienceManager experienceManager;

	private CustomInventoryManager inventoryManager;

	@Override
	public void onEnable() {
		loadConfiguration();
		PluginKeys.initialize(this);
		
		ItemUpdater.cacheLore(this.messageConfig);

		this.upgradeManager = new UpgradeManager(this);
		this.upgradeManager.reloadUpgrades();

		this.toolTypeManager = new ToolTypeManager();
		this.toolTypeManager.reload(this.config.getConfigurationSection("tool-types"));

		this.experienceManager = new ExperienceManager(this.toolTypeManager);
		this.experienceManager.reload(this.experienceConfig);

		this.inventoryManager = new CustomInventoryManager(this);

		Bukkit.getPluginManager().registerEvents(new ExperienceListener(this), this);
		Bukkit.getPluginManager().registerEvents(new ItemMenuListener(this), this);
		Bukkit.getPluginManager().registerEvents(inventoryManager, this);

		Bukkit.getPluginManager().registerEvents(new BlockListeners(this.upgradeManager), this);
		Bukkit.getPluginManager().registerEvents(new EntityListeners(this.upgradeManager), this);
	}

	@Override
	public void onDisable() {
	}

	public YamlConfiguration getConfig() {
		return config;
	}

	public YamlConfiguration getExperienceConfig() {
		return experienceConfig;
	}

	public MessageConfig getMessageConfig() {
		return messageConfig;
	}

	public ToolTypeManager getToolTypeManager() {
		return toolTypeManager;
	}

	public UpgradeManager getUpgradeManager() {
		return upgradeManager;
	}

	public ExperienceManager getExperienceManager() {
		return experienceManager;
	}

	public CustomInventoryManager getInventoryManager() {
		return inventoryManager;
	}

	private void loadConfiguration() {
		if (!this.getDataFolder().exists())
			this.getDataFolder().mkdirs();

		Optional<File> configOptional = FileUtils.copyResource(this, "config.yml");
		configOptional.ifPresent(file -> this.config = YamlConfiguration.loadConfiguration(file));

		Optional<File> experienceOptional = FileUtils.copyResource(this, "experience.yml");
		experienceOptional.ifPresent(file -> this.experienceConfig = YamlConfiguration.loadConfiguration(file));

		Optional<File> messageOptional = FileUtils.copyResource(this, "messages.yml");
		messageOptional
				.ifPresent(file -> this.messageConfig = new MessageConfig(YamlConfiguration.loadConfiguration(file)));
	}

}
