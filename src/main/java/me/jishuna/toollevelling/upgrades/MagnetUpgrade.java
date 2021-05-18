package me.jishuna.toollevelling.upgrades;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import me.jishuna.toollevelling.api.upgrades.CustomUpgrade;
import me.jishuna.toollevelling.api.upgrades.UpgradeData;

public class MagnetUpgrade extends CustomUpgrade {
	private static final String KEY = "magnet";
	private int delay;

	public MagnetUpgrade(Plugin owner) {
		super(owner, KEY, loadConfig(owner, KEY));

		addEventHandler(BlockDropItemEvent.class, this::onBlockBreak);
	}

	@Override
	protected void loadData(YamlConfiguration upgradeConfig) {
		super.loadData(upgradeConfig);
		
		this.delay = upgradeConfig.getInt("pickup-delay", 0);
	}

	private void onBlockBreak(BlockDropItemEvent event, UpgradeData data) {
		for (Item item : event.getItems()) {
			item.teleport(data.getPlayer());
			item.setPickupDelay(this.delay);
			item.setVelocity(new Vector(0, 0, 0));
		}
	}

}
