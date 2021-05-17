package me.jishuna.toollevelling;

import java.io.File;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;

import me.jishuna.commonlib.FileUtils;
import me.jishuna.toollevelling.api.experience.ExperienceManager;
import me.jishuna.toollevelling.api.inventory.CustomInventoryManager;
import me.jishuna.toollevelling.api.tools.ToolTypeManager;
import me.jishuna.toollevelling.api.upgrades.UpgradeManager;
import me.jishuna.toollevelling.listeners.ExperienceListener;
import me.jishuna.toollevelling.listeners.ItemMenuListener;
import me.jishuna.toollevelling.packets.PacketAdapterSetSlot;

public class ToolLevelling extends JavaPlugin {

	private YamlConfiguration config;
	private YamlConfiguration experienceConfig;
	private YamlConfiguration messageConfig;
	private YamlConfiguration upgradeConfig;

	private UpgradeManager upgradeManager;
	private ToolTypeManager toolTypeManager;
	private ExperienceManager experienceManager;

	private CustomInventoryManager inventoryManager;

	private final PacketAdapterSetSlot slotPacketListener = new PacketAdapterSetSlot(this, ListenerPriority.NORMAL);

	@Override
	public void onEnable() {
		loadConfiguration();
		PluginKeys.initialize(this);

		this.upgradeManager = new UpgradeManager(this);
		this.upgradeManager.reloadUpgrades();

		this.toolTypeManager = new ToolTypeManager();
		this.toolTypeManager.reload(this.config.getConfigurationSection("tool-types"));

		this.experienceManager = new ExperienceManager(this.toolTypeManager);
		this.experienceManager.reload(this.experienceConfig);
		
		this.inventoryManager = new CustomInventoryManager(this);

		Bukkit.getPluginManager().registerEvents(new ExperienceListener(this.experienceManager), this);
		Bukkit.getPluginManager().registerEvents(new ItemMenuListener(this), this);
		Bukkit.getPluginManager().registerEvents(inventoryManager, this);

		registerPacketListeners();
		this.slotPacketListener.cacheLore(this.messageConfig);
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

	public YamlConfiguration getMessageConfig() {
		return messageConfig;
	}

	public YamlConfiguration getUpgradeConfig() {
		return upgradeConfig;
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
		messageOptional.ifPresent(file -> this.messageConfig = YamlConfiguration.loadConfiguration(file));

		Optional<File> upgradeOptional = FileUtils.copyResource(this, "upgrades.yml");
		upgradeOptional.ifPresent(file -> this.upgradeConfig = YamlConfiguration.loadConfiguration(file));
	}

	private void registerPacketListeners() {
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();

		manager.addPacketListener(this.slotPacketListener);
	}

}
