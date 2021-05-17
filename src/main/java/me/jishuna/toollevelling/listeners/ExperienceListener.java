package me.jishuna.toollevelling.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.jishuna.toollevelling.ToolLevelling;
import me.jishuna.toollevelling.api.utils.ItemUpdater;

public class ExperienceListener implements Listener {

	private final ToolLevelling plugin;

	public ExperienceListener(ToolLevelling plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof LivingEntity))
			return;

		if (event.getEntity().getKiller() == null)
			return;

		Player player = event.getEntity().getKiller();
		ItemStack item = player.getEquipment().getItemInMainHand();

		if (item != null && !item.getType().isAir()) {
			this.plugin.getExperienceManager().increaseExperience(item, event.getEntityType());
			ItemUpdater.updateItem(this.plugin, item, false);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getEquipment().getItemInMainHand();

		if (item != null && !item.getType().isAir()) {
			this.plugin.getExperienceManager().increaseExperience(item, event.getBlock().getType());
			ItemUpdater.updateItem(this.plugin, item, false);
		}
	}

}
