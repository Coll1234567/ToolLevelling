package me.jishuna.toollevelling.upgrades;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import me.jishuna.toollevelling.api.upgrades.CustomUpgrade;
import me.jishuna.toollevelling.api.upgrades.UpgradeData;

public class MultiplyUpgrade extends CustomUpgrade {
	private static final String KEY = "multiply";

	private Set<Material> blacklist;
	private double chancePerLevel;

	public MultiplyUpgrade(Plugin owner) {
		super(owner, KEY, loadConfig(owner, KEY));

		addEventHandler(BlockDropItemEvent.class, this::onBlockBreak);
	}

	@Override
	protected void loadData(YamlConfiguration upgradeConfig) {
		super.loadData(upgradeConfig);

		this.blacklist = new HashSet<>();
		
		this.chancePerLevel = upgradeConfig.getDouble("chance-per-level", 10);

		for (String mat : upgradeConfig.getStringList("blacklisted-materials")) {
			Material mateiral = Material.matchMaterial(mat.toUpperCase());

			if (mateiral != null) {
				this.blacklist.add(mateiral);
			}
		}
	}

	private void onBlockBreak(BlockDropItemEvent event, UpgradeData data) {
		int level = data.getLevel();
		double chance = getRandom().nextDouble() * 100;

		if (chance > this.chancePerLevel * level)
			return;
		
		for (Item item : event.getItems()) {
			ItemStack itemstack = item.getItemStack();
			Material material = itemstack.getType();

			if (this.blacklist.contains(material))
				continue;

			itemstack.setAmount(itemstack.getAmount() * 2);
			item.setItemStack(itemstack);

			data.getPlayer().spawnParticle(Particle.VILLAGER_HAPPY, event.getBlock().getLocation().add(0.5, 0.5, 0.5),
					10, 0.25, 0.25, 0.25, 0);

		}
	}

}
