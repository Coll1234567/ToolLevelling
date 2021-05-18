package me.jishuna.toollevelling.upgrades;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import me.jishuna.commonlib.WeightedRandom;
import me.jishuna.toollevelling.api.upgrades.CustomUpgrade;
import me.jishuna.toollevelling.api.upgrades.UpgradeData;

public class ScavengerUpgrade extends CustomUpgrade {
	private static final String KEY = "scavenger";

	private final WeightedRandom<Material> dropRandom = new WeightedRandom<>();
	private double chancePerLevel;

	public ScavengerUpgrade(Plugin owner) {
		super(owner, KEY, loadConfig(owner, KEY));

		this.dropRandom.add(70, Material.IRON_NUGGET);
		this.dropRandom.add(30, Material.GOLD_NUGGET);

		addEventHandler(BlockBreakEvent.class, this::onBlockBreak);
	}

	@Override
	protected void loadData(YamlConfiguration upgradeConfig) {
		super.loadData(upgradeConfig);
		
		this.chancePerLevel = upgradeConfig.getDouble("chance-per-level", 10);
	}

	private void onBlockBreak(BlockBreakEvent event, UpgradeData data) {
		Block block = event.getBlock();

		if (block.getType() != Material.STONE)
			return;

		int level = data.getLevel();
		double chance = getRandom().nextDouble() * 100;

		if (chance > this.chancePerLevel * level)
			return;

		ItemStack item = new ItemStack(dropRandom.poll(), getRandom().nextInt(3) + 1);

		block.getWorld().dropItemNaturally(block.getLocation(), item);
	}

}
