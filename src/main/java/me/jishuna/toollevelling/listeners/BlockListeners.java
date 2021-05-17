package me.jishuna.toollevelling.listeners;

import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import me.jishuna.toollevelling.api.upgrades.CustomUpgrade;
import me.jishuna.toollevelling.api.upgrades.UpgradeData;
import me.jishuna.toollevelling.api.upgrades.UpgradeManager;

public class BlockListeners implements Listener {

	private UpgradeManager upgradeManager;

	public BlockListeners(UpgradeManager upgradeManager) {
		this.upgradeManager = upgradeManager;
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getEquipment().getItemInMainHand();

		if (item == null || item.getType().isAir())
			return;

		for (Entry<CustomUpgrade, Integer> entries : this.upgradeManager.getCustomUpgrades(item).entrySet()) {
			entries.getKey().getEventHandlers(BlockBreakEvent.class)
					.forEach(consumer -> consumer.consume(event, new UpgradeData(player, entries.getValue())));
		}

	}
}
