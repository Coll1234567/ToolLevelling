package me.jishuna.toollevelling.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.jishuna.toollevelling.api.experience.ExperienceManager;

public class ExperienceListener implements Listener {

	private final ExperienceManager experienceManager;

	public ExperienceListener(ExperienceManager experienceManager) {
		this.experienceManager = experienceManager;
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof LivingEntity))
			return;

		if (event.getEntity().getKiller() == null)
			return;

		Player player = event.getEntity().getKiller();
		ItemStack item = player.getEquipment().getItemInMainHand();

		if (item != null && !item.getType().isAir())
			this.experienceManager.increaseExperience(item, event.getEntityType());
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getEquipment().getItemInMainHand();

		if (item != null && !item.getType().isAir())
			this.experienceManager.increaseExperience(item, event.getBlock().getType());
	}

}
