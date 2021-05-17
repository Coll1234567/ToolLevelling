package me.jishuna.toollevelling.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.jishuna.toollevelling.ToolLevelling;
import me.jishuna.toollevelling.api.tools.ToolType;

public class ItemMenuListener implements Listener {

	private ToolLevelling plugin;

	public ItemMenuListener(ToolLevelling plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!player.isSneaking() || event.getAction() == Action.LEFT_CLICK_AIR
				|| event.getAction() == Action.LEFT_CLICK_BLOCK)
			return;

		ItemStack item = event.getItem();

		if (item == null || this.plugin.getToolTypeManager().getToolType(item.getType()) == ToolType.NONE)
			return;

		this.plugin.getInventoryManager().openToolMenu(player, item);
	}

}
