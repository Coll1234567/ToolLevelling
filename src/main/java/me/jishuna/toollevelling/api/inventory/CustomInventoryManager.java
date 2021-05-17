package me.jishuna.toollevelling.api.inventory;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import me.jishuna.commonlib.CustomInventory;
import me.jishuna.toollevelling.ToolLevelling;

public class CustomInventoryManager implements Listener {
	private final HashMap<InventoryView, CustomInventory> inventoryMap = new HashMap<>();

	private final HashMap<UUID, ToolInventory> toolInventories = new HashMap<>();

	private final ToolLevelling plugin;

	public CustomInventoryManager(ToolLevelling plugin) {
		this.plugin = plugin;
	}

	public void openGui(HumanEntity player, CustomInventory inventory) {
		this.inventoryMap.put(inventory.open(player), inventory);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getClickedInventory() == null)
			return;

		CustomInventory inventory = this.inventoryMap.get(event.getView());

		if (inventory != null) {
			inventory.consumeClickEvent(event);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getInventory() == null)
			return;

		CustomInventory inventory = inventoryMap.get(event.getView());

		if (inventory != null) {
			inventory.consumeCloseEvent(event);

			this.inventoryMap.remove(event.getView());
		}
	}

	public void openToolMenu(Player player, ItemStack item) {
		ToolInventory inventory = this.toolInventories.computeIfAbsent(player.getUniqueId(),
				key -> new ToolInventory(this.plugin));

		inventory.setItem(item);
		inventory.show(player);
	}
}
