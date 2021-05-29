package me.jishuna.toollevelling.listeners;

import java.util.Map.Entry;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import me.jishuna.toollevelling.api.upgrades.CustomUpgrade;
import me.jishuna.toollevelling.api.upgrades.UpgradeData;
import me.jishuna.toollevelling.api.upgrades.UpgradeManager;

public class EntityListeners implements Listener {

	private UpgradeManager upgradeManager;

	public EntityListeners(UpgradeManager upgradeManager) {
		this.upgradeManager = upgradeManager;
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager().getType() != EntityType.PLAYER)
			return;
		
		Player player = (Player) event.getDamager();
		ItemStack item = player.getEquipment().getItemInMainHand();

		if (item == null || item.getType().isAir())
			return;

		for (Entry<CustomUpgrade, Integer> entries : this.upgradeManager.getCustomUpgrades(item).entrySet()) {
			entries.getKey().getEventHandlers(EntityDamageByEntityEvent.class)
					.forEach(consumer -> consumer.consume(event, new UpgradeData(player, entries.getValue())));
		}

	}

}
