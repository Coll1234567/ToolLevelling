package me.jishuna.toollevelling.upgrades;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

import me.jishuna.toollevelling.api.upgrades.CustomUpgrade;
import me.jishuna.toollevelling.api.upgrades.UpgradeData;
import net.minecraft.server.v1_16_R3.BlockPosition;

public class VeinUpgrade extends CustomUpgrade {
	private static final String KEY = "vein_miner";

	private int max_range;
	private boolean ignore = false;

	public VeinUpgrade(Plugin owner) {
		super(owner, KEY, loadConfig(owner, KEY));

		addEventHandler(BlockBreakEvent.class, this::onBlockBreak);
	}

	@Override
	protected void loadData(YamlConfiguration upgradeConfig) {
		super.loadData(upgradeConfig);

		this.max_range = upgradeConfig.getInt("max-range", 10);
	}

	private void onBlockBreak(BlockBreakEvent event, UpgradeData data) {
		if (this.ignore)
			return;

		Set<Location> blockLocations = new HashSet<>();
		Player player = data.getPlayer();
		Block block = event.getBlock();
		Material material = block.getType();
		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
			recurseBlocks(blockLocations, block, material, 0);

			Bukkit.getScheduler().runTask(getPlugin(), () -> {
				this.ignore = true;

				blockLocations.forEach(location -> ((CraftPlayer) player).getHandle().playerInteractManager.breakBlock(
						new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ())));

				this.ignore = false;
			});
		});

	}

	private void recurseBlocks(Set<Location> locationSet, Block block, Material material, int depth) {
		if (depth > this.max_range)
			return;

		for (int x = -1; x <= 1; x++)
			for (int y = -1; y <= 1; y++)
				for (int z = -1; z <= 1; z++) {
					Block b = block.getRelative(x, y, z);

					if (b.getType() == material && !locationSet.contains(b.getLocation())) {
						locationSet.add(b.getLocation());
						recurseBlocks(locationSet, b, material, depth + 1);
					}
				}

	}
}
