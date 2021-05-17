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
	private final WeightedRandom<Material> dropRandom = new WeightedRandom<>();

	public ScavengerUpgrade(Plugin owner, YamlConfiguration upgradeConfig) {
		super(owner, "scavenger", upgradeConfig);

		this.dropRandom.add(70, Material.IRON_NUGGET);
		this.dropRandom.add(30, Material.GOLD_NUGGET);

		addEventHandler(BlockBreakEvent.class, this::onBlockBreak);
	}

	private void onBlockBreak(BlockBreakEvent event, UpgradeData data) {
		Block block = event.getBlock();

		if (block.getType() != Material.STONE)
			return;

		int level = data.getLevel();
		double chance = getRandom().nextDouble() * 100;

		if (chance > getChancePerLevel() * level)
			return;

		ItemStack item = new ItemStack(dropRandom.poll(), getRandom().nextInt(3) + 1);

		block.getWorld().dropItemNaturally(block.getLocation(), item);
	}

}
