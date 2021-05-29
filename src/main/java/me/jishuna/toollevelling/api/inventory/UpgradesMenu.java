package me.jishuna.toollevelling.api.inventory;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;

import me.jishuna.commonlib.CustomInventory;
import me.jishuna.commonlib.ItemBuilder;
import me.jishuna.toollevelling.ToolLevelling;
import me.jishuna.toollevelling.api.upgrades.Upgrade;

public class UpgradesMenu extends CustomInventory {

	public UpgradesMenu(ToolInventory inventory, Map<Upgrade, Integer> upgrades) {
		super(null, 54, "Test");
		addClickConsumer(event -> event.setCancelled(true));

		for (Entry<Upgrade, Integer> upgrade : upgrades.entrySet()) {
			addItem(upgrade.getKey().asItem(inventory.getPlugin(), Material.BOOK, upgrade.getValue()));
		}

		ToolLevelling plugin = inventory.getPlugin();

		addButton(45,
				new ItemBuilder(Material.ARROW).withName(plugin.getMessageConfig().getString("back-item.name"))
						.withLore(plugin.getMessageConfig().getStringList("back-item.lore")).build(),
				event -> inventory.show(event.getWhoClicked()));
	}

}
